package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import util.DateUtil;
import util.FileUtil;
import util.ITest;
import util.StringUtil;

public class Log {

	private static final String SERVER_LOGS_DRECTORY = "../server/logs";
	private static final String LOG_EXTENSION = ".txt";
	private static final String LOG_DELETE_THREAD = "LogDeletionErrand";
	private static final String CLEAR_LOGS_TIME = "23:59";

	private static String s_stAppLogDirectory;
	private static String s_stServerLogDirectory;

	public static void setAppLogDirectory(String stAppLogDirectory) {
		s_stAppLogDirectory = stAppLogDirectory;
		clearLogs(s_stAppLogDirectory);
	}

	public static void setServerLogDirectory(String stHomeDirectory) {
		s_stServerLogDirectory = StringUtil.getDirectoryPath(stHomeDirectory, SERVER_LOGS_DRECTORY);
	}

	public static void clearLogs(String stLogDirectory) {
		FileUtil.deleteFilesInDirectory(stLogDirectory, new ITest() {
			@Override
			public boolean test(Object object) {
				return true;
			}
		}, false);
	}

	public static void scheduleLogDeletionErrand() {
		IErrand logDeletionErrand = new IErrand() {
			public void run() {
				String stThreadName = LOG_DELETE_THREAD + "_"
						+ DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
				Thread.currentThread().setName(stThreadName);
				clearLogs(s_stAppLogDirectory);
				clearLogs(s_stServerLogDirectory);
			}

			public String getErrandName() {
				return LOG_DELETE_THREAD;
			}
		};

		ErrandManager.scheduleDailyErrand(logDeletionErrand, DateUtil.getNextDailyErrandTime(CLEAR_LOGS_TIME));
	}

	public static void logThread(String stMessage) {
		logThread(stMessage, true);
	}

	public static void logThread(Throwable t) {
		logThread(t.getClass().getName() + ": " + t.getMessage());
		for (StackTraceElement ste : t.getStackTrace()) {
			logThread(StringUtil.getTab() + ste.toString(), false);
		}
	}

	private static void logThread(String stMessage, boolean bApendTimeStamp) {
		if (s_stAppLogDirectory == null) {
			System.out.println(stMessage);
			return;
		}

		String stThreadName = Thread.currentThread().getName();
		File fThreadLog = new File(s_stAppLogDirectory + stThreadName + LOG_EXTENSION);

		if (bApendTimeStamp)
			stMessage = appendTimeStamp(stMessage);

		writeToFile(fThreadLog, stMessage);
	}

	private static String appendTimeStamp(String stMessage) {
		return DateUtil.getCurrentTimeStamp() + ": " + stMessage;
	}

	private static void writeToFile(File fDestination, String stMessage) {
		try (FileOutputStream fos = new FileOutputStream(fDestination, true);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bsw = new BufferedWriter(osw);) {

			bsw.write(stMessage + StringUtil.getNewLine());
			System.out.println(stMessage);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
