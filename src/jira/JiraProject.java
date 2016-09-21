package jira;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import app.Log;
import db.DBConnection;
import db.AppDBQuery;
import db.DBTuple;
import exceptions.ConfigException;

public class JiraProject extends DBTuple {

	private static final String SELECT_ALL_ABBREVS = "SelectAllJiraProjectAbbrevs";

	String m_name;
	String m_abbrev;
	JiraEmployee m_lead;

	public JiraProject(String stName, String stAbbrev, JiraEmployee lead) {
		super();
		m_name = stName;
		m_abbrev = stAbbrev;
		m_lead = lead;
	}

	public JiraProject(String stAbbrev) {
		this(null, stAbbrev, null);
	}

	public static LinkedList<String> getAllProjectAbbrevs() throws SQLException, ConfigException {
		Log.logThread("Getting all project abbreviations.");

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qGetAbbrevs = new AppDBQuery(SELECT_ALL_ABBREVS);

		ResultSet rProjId = dbConn.executeQuery(qGetAbbrevs);
		LinkedList<String> lProjAbbrevs = new LinkedList<String>();

		while (rProjId.next()) {
			lProjAbbrevs.add(rProjId.getString(1));
		}

		return lProjAbbrevs;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[2];
		aObjTupleVals[0] = m_name;

		m_lead.insertIfPossible();
		aObjTupleVals[1] = m_lead.findId();

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_abbrev };
		return aObjUniqueKey;
	}

	public String toString() {
		return m_name + ", " + m_abbrev + ", " + m_lead;
	}

	public String getAbbrev() {
		return m_abbrev;
	}
}
