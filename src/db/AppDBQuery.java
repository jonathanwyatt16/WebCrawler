package db;

import java.sql.SQLException;

import app.Config;
import exceptions.ConfigException;
import util.StringUtil;

public class AppDBQuery extends DBQuery{

	private static final String DB_DIRECTORY = "wcdb";
	private static final String QUERY_CFG_FILE = "WCDB_AppQueries";

	public AppDBQuery(String stQueryName, Object... objVals) throws ConfigException, SQLException {
		m_stName = stQueryName;
		m_aObjVals = objVals;
		m_stQuery = Config.loadConfig(StringUtil.getFilePath(DB_DIRECTORY, QUERY_CFG_FILE), m_stName);
	}

	public AppDBQuery(String stQueryName) throws ConfigException, SQLException {
		this(stQueryName, (Object[]) null);
	}

}
