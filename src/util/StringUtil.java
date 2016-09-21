package util;

public class StringUtil {

	private static String s_stFileSeparator = System.getProperty("file.separator");
	private static String s_stNewLine = System.getProperty("line.separator");

	public static String getFilePath(String... aStFiles) {
		StringBuilder sbFilePath = new StringBuilder();

		for (int nIdxFile = 0; nIdxFile < aStFiles.length; nIdxFile++) {
			String stFile = aStFiles[nIdxFile];
			sbFilePath.append(stFile);
			
			if (nIdxFile != aStFiles.length - 1 && !stFile.endsWith(s_stFileSeparator)) {
				sbFilePath.append(s_stFileSeparator);
			}
		}

		return sbFilePath.toString();
	}

	public static String getDirectoryPath(String... aStDirectories) {
		String stFilePath = getFilePath(aStDirectories);
		return stFilePath.endsWith(s_stFileSeparator) ? stFilePath : stFilePath + s_stFileSeparator;
	}

	public static String ensureMaxLength(String st, int nMaxLength) {
		if (st != null && st.length() > nMaxLength)
			st = st.substring(0, nMaxLength);

		return st;
	}

	public static boolean isAllDigits(String st) {
		for (int nIdx = 0; nIdx < st.length(); nIdx++) {
			if (!Character.isDigit(st.charAt(nIdx)))
				return false;
		}

		return true;
	}

	public static String[] splitAtFirstInstance(String st, String stSplitValue) {
		int nIdxSplitStart = st.indexOf(stSplitValue);
		int nIdxSplitEnd = nIdxSplitStart + stSplitValue.length();

		return new String[] { st.substring(0, nIdxSplitStart), st.substring(nIdxSplitEnd) };
	}

	public static String getNewLine() {
		return s_stNewLine;
	}

	public static String getTab() {
		return "\t";
	}
}
