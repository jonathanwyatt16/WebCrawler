package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;

import db.DBConnection;
import exceptions.ConfigException;
import server.ServerTempFile;
import util.StringUtil;

public class Config {
	private static final String CFG_DIRECTORY = "conf";
	private static final String CFG_MAIN_FILE = "WebCrawler";
	private static final String CFG_LOG_DIRECTORY = "LogDirectory";
	private static final String CFG_DB_STRING = "DBConnection";

	private static final String CFG_EXTENSION = ".cfg";
	private static final String CFG_PREFIX = "#";
	private static final String CFG_DELIMIT = "=";
	private static final String CFG_LOAD_ALL_CONFIGS = "LoadAllConfigs";

	private static String s_stHomeDirectory;
	private static String s_stCfgDirectory;
	private static Config s_cfgMain;

	private String m_stCfgName;
	private LinkedList<KeyValuePair> m_cfgVals;

	public Config(String stCfgName) throws ConfigException {
		m_stCfgName = stCfgName;
		m_cfgVals = loadConfigFile(m_stCfgName);
	}

	public static void initializeApp() throws SQLException, ConfigException, IOException {
		s_stHomeDirectory = new File(System.getProperty("user.dir")).getParent();

		setConfigDirectory(s_stHomeDirectory);
		s_cfgMain = new Config(CFG_MAIN_FILE);

		String stLogDirectory = StringUtil.getDirectoryPath(s_cfgMain.getCfgVal(CFG_LOG_DIRECTORY));
		Log.setAppLogDirectory(stLogDirectory);
		Log.setServerLogDirectory(s_stHomeDirectory);
		Log.scheduleLogDeletionErrand();

		ServerTempFile.setSTFDirectory(s_stHomeDirectory);
		ServerTempFile.scheduleSTFDeletionErrand();

		DBConnection.setJdbcString(s_cfgMain.getCfgVal(CFG_DB_STRING));
	}

	public static Config getMainConfig() throws ConfigException {
		if (s_cfgMain == null)
			s_cfgMain = new Config(CFG_MAIN_FILE);

		return s_cfgMain;
	}

	public static void setConfigDirectory(String stHomeDirectory) {
		s_stCfgDirectory = StringUtil.getDirectoryPath(stHomeDirectory, CFG_DIRECTORY);
	}

	public static String loadConfig(String stCfgPath, String stCfgName) throws ConfigException {
		return readConfigFile(stCfgPath, stCfgName, null).m_value;
	}

	private static LinkedList<KeyValuePair> loadConfigFile(String stCfgPath) throws ConfigException {
		LinkedList<KeyValuePair> llConfigs = new LinkedList<KeyValuePair>();
		readConfigFile(stCfgPath, CFG_LOAD_ALL_CONFIGS, llConfigs);

		return llConfigs;
	}

	private static KeyValuePair readConfigFile(String stCfgPath, String stCfg, LinkedList<KeyValuePair> llConfigs)
			throws ConfigException {
		try (FileInputStream fis = new FileInputStream(
				StringUtil.getFilePath(s_stCfgDirectory, stCfgPath) + CFG_EXTENSION);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);) {

			String stLine = "";
			while ((stLine = br.readLine()) != null) {
				KeyValuePair kvpCfg = parseCfgLine(stLine);
				if (kvpCfg != null) {
					if (stCfg.equals(CFG_LOAD_ALL_CONFIGS))
						llConfigs.add(kvpCfg);
					else if (kvpCfg.m_key.equals(stCfg))
						return kvpCfg;
				}
			}
			return null;
		} catch (IOException e) {
			Log.logThread(e.toString());
			throw new ConfigException("IOException while loading config file " + stCfgPath + ". " + e.getMessage());
		}
	}

	private static KeyValuePair parseCfgLine(String stLine) throws ConfigException {
		if (!stLine.startsWith(CFG_PREFIX))
			return null;

		stLine = stLine.substring(CFG_PREFIX.length());

		if (!stLine.contains(CFG_DELIMIT)) {
			throw new ConfigException("Line does not contain " + CFG_DELIMIT + ": " + stLine);
		}

		String stKey = stLine.substring(0, stLine.indexOf(CFG_DELIMIT));
		String stValue = stLine.substring(stLine.indexOf(CFG_DELIMIT) + 1);

		return new KeyValuePair(stKey, stValue);
	}

	public String getCfgVal(String cfgName) throws ConfigException {
		int idx = m_cfgVals.indexOf(new KeyValuePair(cfgName, null));

		if (idx == -1) {
			Log.logThread(Arrays.toString(getPropKeys()));
			throw new ConfigException("Config " + cfgName + " could not be found in file " + m_stCfgName);
		}

		return m_cfgVals.get(idx).m_value;
	}

	public String[] getPropKeys() {
		String[] propKeys = new String[m_cfgVals.size()];
		for (int idx = 0; idx < propKeys.length; idx++)
			propKeys[idx] = m_cfgVals.get(idx).m_key;
		return propKeys;
	}

	private static class KeyValuePair {
		private String m_key, m_value;

		private KeyValuePair(String key, String value) {
			m_key = key;
			m_value = value;
		}

		public boolean equals(Object otherObj) {
			KeyValuePair otherKvp = (KeyValuePair) otherObj;
			return m_key.equals(otherKvp.m_key);
		}
	}
}
