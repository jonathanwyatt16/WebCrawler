package jira;

import java.sql.SQLException;
import java.util.ArrayList;

import db.DBConnection;
import db.AppDBQuery;
import db.DBTuple;
import exceptions.ConfigException;

public class JiraTicketWatcher extends DBTuple {

	private static final String CFG_DELETE_WATCHERS = "DeleteWatchers";

	private int m_nEmployeeId;
	private int m_nTicketId;

	public JiraTicketWatcher(int nEmployeeId, int nTicketId) {
		m_nEmployeeId = nEmployeeId;
		m_nTicketId = nTicketId;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		return null;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_nEmployeeId, m_nTicketId };
		return aObjUniqueKey;
	}

	public String toString() {
		return "Employee " + m_nEmployeeId + " watching ticket id " + m_nTicketId;
	}

	public static void createWatchers(ArrayList<JiraEmployee> alWatchers, int nTicketId)
			throws SQLException, ConfigException {
		deleteOldWatchers(nTicketId);

		for (JiraEmployee employee : alWatchers) {
			employee.insertIfPossible();
			JiraTicketWatcher newWatcher = new JiraTicketWatcher(employee.findId(), nTicketId);
			newWatcher.insert();
		}
	}

	private static void deleteOldWatchers(int nTicketId) throws SQLException, ConfigException {
		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qRemoveWatchers = new AppDBQuery(CFG_DELETE_WATCHERS, nTicketId);
		dbConn.executeUpdate(qRemoveWatchers);
	}

}
