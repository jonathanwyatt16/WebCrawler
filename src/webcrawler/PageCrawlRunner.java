package webcrawler;

import java.sql.SQLException;

import app.ManagedThreadRunner;
import exceptions.ConfigException;
import exceptions.HtmlPageOpenException;
import jira.JiraTicketPageCrawlRun;

public final class PageCrawlRunner extends ManagedThreadRunner {

	private PageCrawlRunner(PageCrawlRun pageCrawlRun) {
		super(pageCrawlRun, pageCrawlRun.getPageTitle() + "_" + pageCrawlRun.getWcrId());
	}

	public static PageCrawlRunner getPageCrawlRunner(CrawlablePage pageToCrawl, int nWebCrawlRunId)
			throws ConfigException, HtmlPageOpenException, SQLException {
		PageCrawlRun ticketRun = null;

		switch (pageToCrawl.getType()) {
		case JIRA:
			ticketRun = new JiraTicketPageCrawlRun(pageToCrawl, nWebCrawlRunId);
			break;

		default:
			throw new ConfigException("Invalid PageCrawlRunner type.");
		}
		pageToCrawl.setPageCrawlRun(ticketRun);

		return new PageCrawlRunner(ticketRun);
	}

	@Override
	public void onThreadFinish() {
		PageCrawlThreadManager.pageCrawlRunnerFinished(this);
	}
}
