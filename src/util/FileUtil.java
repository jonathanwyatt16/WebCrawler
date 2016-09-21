package util;

import java.io.File;
import java.nio.file.Files;

import app.Log;

public class FileUtil {

	public static void deleteFilesInDirectory(String stDirectory, ITest eligibilityTest, boolean bDeleteRoot) {
		File fDirectory = new File(stDirectory);
		File[] files = fDirectory.listFiles();

		if (files == null) {
			Log.logThread("No files to delete in " + fDirectory.getAbsolutePath());
			return;
		}

		for (File file : files) {
			try {
				if (eligibilityTest.test(file)) {
					Log.logThread("Deleting " + file.getAbsolutePath());

					if (file.isDirectory()) {
						deleteFilesInDirectory(file.getAbsolutePath(), eligibilityTest, true);
					} else {
						Files.delete(file.toPath());
					}

				} else {
					Log.logThread(file.getAbsolutePath() + " is not eligible for deletion.");
				}
			} catch (Throwable t) {
				Log.logThread("Unable to delete " + file.getAbsolutePath());
				Log.logThread(t);
			}
		}

		if (bDeleteRoot) {
			try {
				Files.delete(fDirectory.toPath());
			} catch (Throwable t) {
				Log.logThread("Unable to delete directory " + fDirectory.getAbsolutePath());
				Log.logThread(t);
			}
		}
	}
}
