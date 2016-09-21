package app;

import java.util.Timer;
import java.util.TimerTask;

import util.DateUtil;

public abstract class ErrandManager {
	private static Timer s_timer = new Timer();

	public static void scheduleErrand(IErrand errand, long lIntervalMillis, DateUtil... aDFirstRun) {
		TimerTaskWrapper timerTaskWrapper = new TimerTaskWrapper(errand);

		for (DateUtil dFirstRun : aDFirstRun) {
			Log.logThread(
					"Scheduling " + errand.getErrandName() + " to run every " + lIntervalMillis + " ms. starting at " + dFirstRun);
			s_timer.scheduleAtFixedRate(timerTaskWrapper, dFirstRun.toDate(), lIntervalMillis);
		}
	}

	public static void scheduleDailyErrand(final IErrand errand, DateUtil... dNextErrand) {
		scheduleErrand(errand, DateUtil.MILLIS_IN_DAY, dNextErrand);
	}
	
	private static class TimerTaskWrapper extends TimerTask {
		private IErrand m_errand;
		
		TimerTaskWrapper(IErrand errand) {
			m_errand = errand;
		}

		@Override
		public void run() {
			m_errand.run();
		}
	}
}
