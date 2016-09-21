package webcrawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import util.DateUtil;
import app.Log;
import db.DBConnection;
import db.AppDBQuery;
import db.DBTuple;
import exceptions.ConfigException;
import util.StringUtil;
import webcrawler.WebCrawlRunScheduler.WebCrawlerType;

public abstract class PageCrawlRun extends DBTuple implements ICrawlRun {

	private static final String TABLE_PAGE_CRAWL_RUN = "PageCrawlRun";
	private static final String CFG_ERROR_PAGES = "SelectErrorPages";
	private static final int DB_MAX_LENGTH_COMMENT = 255;

	protected WebBrowser m_browser;
	protected WebCrawlerType m_type;
	protected CrawlablePage m_crawlable;

	protected int m_nId;
	protected int m_nWcrId;
	protected int m_nStatus;
	protected DateUtil m_dStart, m_dEnd;
	protected String m_stComment;

	protected PageCrawlRun(CrawlablePage crawlablePage, int nWebCrawlRunId) throws SQLException, ConfigException {
		super(TABLE_PAGE_CRAWL_RUN);
		m_crawlable = crawlablePage;
		m_nWcrId = nWebCrawlRunId;
		m_nStatus = STATUS_NOT_STARTED;
		m_stComment = COMMENT_NOT_STARTED;
		insert();
		m_nId = findId();
	}

	public static LinkedList<CrawlablePage> getErrorPages(int nWebCrawlRun, WebCrawlerType crawlerType)
			throws SQLException, ConfigException {
		Log.logThread("Selecting errored pages for wcrId " + nWebCrawlRun);

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qErrorPages = new AppDBQuery(CFG_ERROR_PAGES, nWebCrawlRun);

		LinkedList<CrawlablePage> llErrorPages = new LinkedList<CrawlablePage>();
		ResultSet rErrorPages = dbConn.executeQuery(qErrorPages);
		int nErrorPages = 0;
		while (rErrorPages.next()) {
			String stPageId = rErrorPages.getString(1);
			Log.logThread("Found error page " + ++nErrorPages + ": " + stPageId);
			llErrorPages.add(CrawlablePage.getCrawlablePage(stPageId, crawlerType));
		}

		return llErrorPages;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[4];

		aObjTupleVals[0] = m_dStart == null ? null : m_dStart.toTimestamp();
		aObjTupleVals[1] = m_dEnd == null ? null : m_dEnd.toTimestamp();
		aObjTupleVals[2] = m_nStatus;
		aObjTupleVals[3] = StringUtil.ensureMaxLength(m_stComment, DB_MAX_LENGTH_COMMENT);

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() throws SQLException, ConfigException {
		Object[] aObjUniqueKey = { m_nWcrId, getPageTitle() };
		return aObjUniqueKey;
	}

	public int getId() {
		return m_nId;
	}

	public int getWcrId() {
		return m_nWcrId;
	}

	protected String getPageTitle() {
		return m_crawlable.getTitle();
	}

	public String toString() {
		return "PageCrawlRun for " + getPageTitle();
	}
}
