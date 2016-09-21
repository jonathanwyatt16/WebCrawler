package jira;

import java.sql.SQLException;

import app.Log;
import db.DBTuple;
import exceptions.ConfigException;

public class JiraEmployee extends DBTuple {

	private static final String INACTIVE = "\\(Inactive\\)";

	private String m_stFullName;
	private String m_stAlias;

	public JiraEmployee(String stName) {
		m_stFullName = stName.replaceAll(INACTIVE, "").trim();
		m_stAlias = getAbbrev();
	}

	private String getAbbrev() {
		try {
			String stLowerCaseName = m_stFullName.toLowerCase();

			if (stLowerCaseName.contains("zlti"))
				return null;

			String[] aStNames = stLowerCaseName.split(" ");

			if (aStNames.length < 2) {
				return null;
			}

			String stAbbrev = aStNames[0].substring(0, 1);
			stAbbrev += aStNames[aStNames.length - 1];

			return stAbbrev;
		}

		catch (Exception e) {
			Log.logThread("Error finding abbreviation for " + m_stFullName);
			Log.logThread(e);
			return null;
		}
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		return new Object[] { m_stAlias };
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_stFullName };
		return aObjUniqueKey;
	}

	public String toString() {
		return m_stFullName + " (" + m_stAlias + ")";
	}
}
