package server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import app.Config;
import app.Log;
import util.DateUtil;

public class WebCrawlerServletListener implements ServletContextListener {

	public static final String LISTENER_THREAD = "ServletListener";

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			String stThreadName = LISTENER_THREAD + "_" + DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
			Thread.currentThread().setName(stThreadName);
			Log.logThread("Initializing WebCralwerServletListener.");

			Config.initializeApp();

			Log.logThread("Done initializing WebCralwerServletListener.");
		} catch (Exception e) {
			Log.logThread(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		String stThreadName = LISTENER_THREAD + "_" + DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
		Thread.currentThread().setName(stThreadName);
		Log.logThread("WebCrawlerServletListener destroyed.");
	}
}
