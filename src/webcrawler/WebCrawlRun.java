package webcrawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import util.DateUtil;
import app.IErrand;
import app.Log;
import db.DBConnection;
import db.AppDBQuery;
import db.DBTuple;
import exceptions.ConfigException;
import exceptions.CrawlPagesException;
import exceptions.GetPagesException;
import exceptions.HtmlPageOpenException;
import util.StringUtil;
import webcrawler.WebCrawlRunScheduler.WebCrawlerType;

public abstract class WebCrawlRun extends DBTuple implements ICrawlRun, IErrand {

	private static final String TABLE_WEB_CRAWL_RUN = "WebCrawlRun";
	private static final String CFG_SELECT_LATEST_SUCCESSFUL_CRAWL = "SelectLatestSuccessWebCrawlRun";
	private static final String CFG_SELECT_PAGE_SUCCESS = "SelectPageCrawlSuccess";
	private static final String CFG_SELECT_PAGE_ERROR = "SelectPageCrawlError";

	private static final int DB_MAX_LENGTH_COMMENT = 255;

	protected WebBrowser m_browser;
	protected WebCrawlerType m_type;
	protected List<CrawlablePage> m_pagesToCrawl;
	protected DateUtil m_dStart, m_dEnd, m_dPreviousCrawl;

	protected int m_nId, m_nPreviousId;
	protected int m_nStatus, m_nPreviousStatus;
	protected int m_nPagesFound, m_nPageCrawlSuccess, m_nPageCrawlError;

	protected String m_stComment;

	protected WebCrawlRun(WebCrawlerType type) throws SQLException, ConfigException {
		super(TABLE_WEB_CRAWL_RUN);
		m_type = type;
	}

	protected abstract void getPagesToCrawl() throws GetPagesException;

	public void run() {
		try {
			String stThreadName = getErrandName() + DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
			Thread.currentThread().setName(stThreadName);

			m_dStart = new DateUtil();
			m_nStatus = STATUS_RUNNING;
			m_stComment = COMMENT_RUNNING;
			insert();
			m_nId = findId();

			findLatestSuccessfulCrawl();
			getPagesToCrawl();
			m_nPagesFound = m_pagesToCrawl.size();
			update();

			crawlPages();
			m_nStatus = m_nPageCrawlSuccess == m_nPagesFound ? STATUS_SUCCESS : STATUS_SUCCESS_WITH_ERROR;
			m_stComment = COMMENT_DONE;

		} catch (Exception e) {
			Log.logThread(e);
			m_nStatus = STATUS_ERROR;
			m_stComment = e.getMessage();
		}

		finally {
			m_dEnd = new DateUtil();

			try {
				update();
			} catch (SQLException | ConfigException e) {
				Log.logThread(e);
			}

			onWebCrawlRunEnd();
		}
	}

	private void findLatestSuccessfulCrawl() {
		try {
			DBConnection dbconn = DBConnection.getDBConnection();
			AppDBQuery qPreviousCrawl = new AppDBQuery(CFG_SELECT_LATEST_SUCCESSFUL_CRAWL);
			ResultSet rPreviousCrawl = dbconn.executeQuery(qPreviousCrawl);

			rPreviousCrawl.next();
			Timestamp tPreviousCrawl = rPreviousCrawl.getTimestamp(1);

			m_dPreviousCrawl = new DateUtil(tPreviousCrawl);
			m_nPreviousId = rPreviousCrawl.getInt(2);
			m_nPreviousStatus = rPreviousCrawl.getInt(3);

		} catch (SQLException | ConfigException e) {
			Log.logThread("No previous run found.");
		}
	}

	protected LinkedList<CrawlablePage> getPreviousErrorPages() throws SQLException, ConfigException {
		if (m_nPreviousStatus == STATUS_SUCCESS)
			return null;

		return PageCrawlRun.getErrorPages(m_nPreviousId, m_type);
	}

	private final void crawlPages() throws CrawlPagesException {
		try {
			Iterator<CrawlablePage> iterPages = m_pagesToCrawl.iterator();

			while (iterPages.hasNext()) {
				CrawlablePage pageToCrawl = iterPages.next();
				iterPages.remove();

				PageCrawlRunner crawlRunner = PageCrawlRunner.getPageCrawlRunner(pageToCrawl, m_nId);
				Log.logThread("Submitting PageCrawlRunner for page " + pageToCrawl);
				PageCrawlThreadManager.submitPageCrawlRunner(crawlRunner);

				if (!iterPages.hasNext()) {
					PageCrawlThreadManager.allPageCrawlRunnersFinished(true);
				}

				updateStats();
			}

		} catch (HtmlPageOpenException | ConfigException | SQLException e) {
			Log.logThread(e);
			throw new CrawlPagesException(e.getMessage());
		}
	}

	private void updateStats() throws SQLException, ConfigException {
		Log.logThread("Updating WebCrawlRun stats.");
		DBConnection dbConn = DBConnection.getDBConnection();

		AppDBQuery qSuccess = new AppDBQuery(CFG_SELECT_PAGE_SUCCESS, m_nId);
		ResultSet rSuccess = dbConn.executeQuery(qSuccess);
		rSuccess.next();
		m_nPageCrawlSuccess = rSuccess.getInt(1);

		AppDBQuery qError = new AppDBQuery(CFG_SELECT_PAGE_ERROR, m_nId);
		ResultSet rError = dbConn.executeQuery(qError);
		rError.next();
		m_nPageCrawlError = rError.getInt(1);

		update();
	}

	private void onWebCrawlRunEnd() {
		Log.logThread("Done with WebCrawlRun " + m_nId);

		m_browser = null;
		m_pagesToCrawl = null;
		m_dStart = null;
		m_dEnd = null;
		m_dPreviousCrawl = null;

		m_nId = 0;
		m_nPreviousId = 0;
		m_nPagesFound = 0;
		m_nPageCrawlSuccess = 0;
		m_nPageCrawlError = 0;
	}

	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[7];

		aObjTupleVals[0] = m_dEnd == null ? null : m_dEnd.toTimestamp();
		aObjTupleVals[1] = m_type.toString();
		aObjTupleVals[2] = m_nStatus;
		aObjTupleVals[3] = StringUtil.ensureMaxLength(m_stComment, DB_MAX_LENGTH_COMMENT);
		aObjTupleVals[4] = m_nPagesFound;
		aObjTupleVals[5] = m_nPageCrawlSuccess;
		aObjTupleVals[6] = m_nPageCrawlError;

		return aObjTupleVals;
	}

	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_dStart.toTimestamp() };
		return aObjUniqueKey;
	}
}
