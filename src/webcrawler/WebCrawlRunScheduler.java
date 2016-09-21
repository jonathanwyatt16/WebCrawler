package webcrawler;

import java.sql.SQLException;

import app.Config;
import app.ErrandManager;
import exceptions.ConfigException;
import exceptions.HtmlPageOpenException;
import jira.JiraCrawlRun;
import util.DateUtil;

public class WebCrawlRunScheduler {

	private static final String CFG_CRAWLER_TYPE = "WebCrawlerType";
	private static final String CFG_MAX_PAGE_CRAWL_THREADS = "MaxPageCrawlThreads";
	private static final String WEB_CRAWL_RUN_ERRAND_TIMES = "WebCrawlerRunTimes";

	public enum WebCrawlerType {
		JIRA
	};

	public static void scheduleWebCrawler(Config cfgMain) throws ConfigException, HtmlPageOpenException, SQLException {
		String stCrawlerType = cfgMain.getCfgVal(CFG_CRAWLER_TYPE);
		WebCrawlerType webCrawlerType = WebCrawlerType.valueOf(stCrawlerType);

		WebCrawlRun webCrawlRun = null;
		switch (webCrawlerType) {
		case JIRA:
			webCrawlRun = new JiraCrawlRun();
			break;

		default:
			throw new ConfigException("Invalid WebCrawer type: " + webCrawlerType);
		}

		int nMaxPageCrawlThreads = Integer.parseInt(cfgMain.getCfgVal(CFG_MAX_PAGE_CRAWL_THREADS));
		PageCrawlThreadManager.setMaxPageCrawlThreadCount(nMaxPageCrawlThreads);

		String stErrandTimes = cfgMain.getCfgVal(WEB_CRAWL_RUN_ERRAND_TIMES);
		String[] aStErrandTimes = stErrandTimes.split(",");
		
		for (String stErrandTime : aStErrandTimes) {
			DateUtil dNextErrand = DateUtil.getNextDailyErrandTime(stErrandTime);
			ErrandManager.scheduleDailyErrand(webCrawlRun, dNextErrand);
		}
	}
}
