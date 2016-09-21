package server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.Config;
import app.Log;
import util.DateUtil;
import webcrawler.WebCrawlRunScheduler;

@SuppressWarnings("serial")
public class WebCrawlerServlet extends HttpServlet {

	private static final String INIT_THREAD = "ServletInit";
	private static final String POST_THREAD = "Post";

	@Override
	public void init() {
		try {
			String stThreadname = INIT_THREAD + "_" + DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
			Thread.currentThread().setName(stThreadname);
			Log.logThread("Initializing WebCrawlerServlet");

			WebCrawlRunScheduler.scheduleWebCrawler(Config.getMainConfig());

			Log.logThread("Done initializing WebCrawlerServlet.");
		} catch (Exception e) {
			Log.logThread(e);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String stThreadName = POST_THREAD + "_" + DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
			Thread.currentThread().setName(stThreadName);
			Log.logThread("Doing Post.");

			HttpPostResponder responder = HttpPostResponder.getResponder(request);
			responder.respond(response);

			Log.logThread("Done doing Post.");
		}

		catch (Throwable t) {
			Log.logThread(t);
		}
	}
}
