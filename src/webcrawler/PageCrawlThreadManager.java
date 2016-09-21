package webcrawler;

import app.ThreadGroupManager;

public class PageCrawlThreadManager extends ThreadGroupManager {
	private static PageCrawlThreadManager s_manager = new PageCrawlThreadManager();

	private PageCrawlThreadManager() {
		super();
	}

	public static void setMaxPageCrawlThreadCount(int nThreads) {
		s_manager.setMaxThreadCount(nThreads);
	}

	public static void submitPageCrawlRunner(PageCrawlRunner pageCrawlRunner) {
		s_manager.submitThread(pageCrawlRunner);
	}

	public static void pageCrawlRunnerFinished(PageCrawlRunner pageCrawlRunner) {
		s_manager.threadFinished(pageCrawlRunner);
	}

	public static boolean allPageCrawlRunnersFinished(boolean bBlockUntilAllFinished) {
		return s_manager.allThreadsFinished(bBlockUntilAllFinished);
	}
}
