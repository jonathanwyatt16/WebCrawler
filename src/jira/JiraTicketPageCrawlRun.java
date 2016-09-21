package jira;

import java.sql.SQLException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import util.DateUtil;
import app.Log;
import exceptions.ConfigException;
import exceptions.HtmlPageOpenException;
import exceptions.NonexistentPageException;
import webcrawler.CrawlablePage;
import webcrawler.PageCrawlRun;

public class JiraTicketPageCrawlRun extends PageCrawlRun {

	public JiraTicketPageCrawlRun(CrawlablePage crawlablePage, int nWebCrawlRunId)
			throws ConfigException, HtmlPageOpenException, SQLException {
		super(crawlablePage, nWebCrawlRunId);
	}

	@Override
	public synchronized void run() {

		try {
			m_dStart = new DateUtil();
			m_nStatus = STATUS_RUNNING;
			m_stComment = COMMENT_RUNNING;
			update();

			m_browser = new JiraBrowser();
			m_browser.logIn();
			m_crawlable.init();

			JiraLoginPage loginPage = (JiraLoginPage) m_browser.getCurrentPage();
			HtmlPage ticketPage = loginPage.searchForTicket(m_crawlable.getTitle());
			m_crawlable.setHtmlPage(ticketPage, true, false);

			m_crawlable.crawl();
			m_browser = null;
			m_nStatus = STATUS_SUCCESS;
			m_stComment = COMMENT_DONE;
		}

		catch (NonexistentPageException e) {
			m_nStatus = STATUS_SUCCESS_WITH_ERROR;
			m_stComment = e.getMessage();
		}

		catch (Exception e) {
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
		}
	}
}
