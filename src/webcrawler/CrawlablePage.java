package webcrawler;

import exceptions.ConfigException;
import exceptions.CrawlException;
import jira.JiraTicketPage;
import webcrawler.WebCrawlRunScheduler.WebCrawlerType;

public abstract class CrawlablePage extends Page {

	protected WebCrawlerType m_type;
	protected PageCrawlRun m_crawlRun;

	public CrawlablePage(WebCrawlerType type, boolean bLoadConfig) throws ConfigException {
		super(bLoadConfig);
		m_type = type;
	}

	public static CrawlablePage getCrawlablePage(String stId, WebCrawlerType crawlerType) throws ConfigException {
		switch (crawlerType) {
		case JIRA:
			return new JiraTicketPage(stId);
		}
		throw new ConfigException("Invalid crawler type " + crawlerType);
	}

	public WebCrawlerType getType() {
		return m_type;
	}

	public void setPageCrawlRun(PageCrawlRun crawlRun) {
		m_crawlRun = crawlRun;
	}

	public abstract void init() throws ConfigException;

	public abstract void crawl() throws CrawlException;
}
