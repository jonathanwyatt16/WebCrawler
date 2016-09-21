package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import app.ErrandManager;
import app.IErrand;
import app.Log;
import util.DateUtil;
import util.FileUtil;
import util.ITest;
import util.StringUtil;

public abstract class ServerTempFile {

	private static final String SERVER_RELATIVE_PATH = "server\\webapps\\WebCrawler\\temp";
	private static final String WEB_CLIENT_RELATIVE_PATH = ".\\temp";
	private static final String STF_DELETION_THREAD = "STFDeletion";

	private static final int STF_DELETION_INTERVAL_MINS = 15;
	private static final int STF_DELETION_THRESHOLD_MINS = 10;
	private static final int MAX_TEMP_DIR_COUNTER = 1000;

	private static String s_stSTFDirectory;
	private static int s_nTempDirCounter = 0;

	protected String m_tempDirectoryName;
	protected String m_stFileName;

	public ServerTempFile(String stFileName) throws IOException {
		m_stFileName = stFileName;
		m_tempDirectoryName = createTempDirectory();
	}

	public static synchronized String createTempDirectory() throws IOException {
		String stTempDirectoryName = System.nanoTime() + "_" + s_nTempDirCounter++ % MAX_TEMP_DIR_COUNTER;
		String stTempDirectoryPath = StringUtil.getDirectoryPath(s_stSTFDirectory, stTempDirectoryName);
		Files.createDirectory(Paths.get(stTempDirectoryPath));

		return stTempDirectoryName;
	}

	public static void setSTFDirectory(String stHomeDirectory) {
		s_stSTFDirectory = StringUtil.getDirectoryPath(stHomeDirectory, SERVER_RELATIVE_PATH);
		Log.logThread("Set server temp file directory = " + s_stSTFDirectory);
	}

	public static void scheduleSTFDeletionErrand() {
		IErrand stfDeletionErrand = new IErrand() {
			public void run() {
				String stThreadName = STF_DELETION_THREAD + "_"
						+ DateUtil.getCurrentTimeStamp(DateUtil.LOG_FILE_DATE_FORMAT);
				Thread.currentThread().setName(stThreadName);

				Log.logThread("Running server temp file deletion errand.");
				FileUtil.deleteFilesInDirectory(s_stSTFDirectory, new ITest() {

					@Override
					public boolean test(Object object) {
						File file = (File) object;

						if (!file.isDirectory())
							return true;

						String stDirName = file.getName();
						Long lDirCreate = Long.parseLong(stDirName.substring(0, stDirName.indexOf("_")));
						Long lThreshold = System.nanoTime() - (STF_DELETION_THRESHOLD_MINS * DateUtil.NANOS_IN_MINUTE);

						return lDirCreate <= lThreshold;

					}
				}, false);

				Log.logThread("Done running server temp file deletion errand.");
			}

			public String getErrandName() {
				return STF_DELETION_THREAD;
			}
		};

		ErrandManager.scheduleErrand(stfDeletionErrand, STF_DELETION_INTERVAL_MINS * DateUtil.MILLIS_IN_MINUTE,
				new DateUtil());
	}

	public String getFileName() {
		return m_stFileName;
	}

	public String getAbsolutePath() {
		return StringUtil.getFilePath(s_stSTFDirectory, m_tempDirectoryName, m_stFileName);
	}

	public String getRelativePath() {
		return StringUtil.getFilePath(WEB_CLIENT_RELATIVE_PATH, m_tempDirectoryName, m_stFileName);
	}
}
