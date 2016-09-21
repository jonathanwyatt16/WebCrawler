package jira;

import java.sql.SQLException;
import java.util.ArrayList;

import util.DateUtil;
import app.Log;
import db.DBConnection;
import db.AppDBQuery;
import db.DBTuple;
import exceptions.ConfigException;
import exceptions.IncompleteDataException;
import util.StringUtil;

public class JiraTicketWorkLog extends DBTuple {

	private static final String CFG_DELETE_WORK_LOGS = "DeleteWorkLogs";
	private static final int DB_MAX_LENGTH_COMMENT = 1000;

	private int m_nId;
	private int m_nTicketId;
	private JiraEmployee m_employee;
	private DateUtil m_date;
	private String m_stTimeSpent;
	private String m_stComment;

	public JiraTicketWorkLog(int nWorkLogId, int nTicketId, JiraEmployee workLogEmployee, JiraDate workLogDate,
			String stTimeSpent, String stWorkLogComment) {
		m_nId = nWorkLogId;
		m_nTicketId = nTicketId;
		m_employee = workLogEmployee;
		m_date = workLogDate;
		m_stTimeSpent = stTimeSpent;
		m_stComment = stWorkLogComment;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[5];
		aObjTupleVals[0] = m_nTicketId;

		m_employee.insertIfPossible();
		aObjTupleVals[1] = m_employee.findId();
		aObjTupleVals[2] = m_date.toTimestamp();
		aObjTupleVals[3] = getMinutes(m_stTimeSpent);
		aObjTupleVals[4] = StringUtil.ensureMaxLength(m_stComment, DB_MAX_LENGTH_COMMENT);

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_nId };
		return aObjUniqueKey;
	}

	private int getMinutes(String stTimeSpent) {
		int nMinutes = 0;

		String[] aStTimeUnits = stTimeSpent.split(", ");

		for (String stTimeUnit : aStTimeUnits) {
			String[] aStTimeAndUnit = stTimeUnit.split(" ");
			int nTime = Integer.parseInt(aStTimeAndUnit[0]);
			String stUnit = aStTimeAndUnit[1];

			if (stUnit.contains("week")) {
				nMinutes += nTime * DateUtil.MINUTES_IN_WEEK;
			}

			else if (stUnit.contains("day")) {
				nMinutes += nTime * DateUtil.MINUTES_IN_DAY;
			}

			else if (stUnit.contains("hour")) {
				nMinutes += nTime * DateUtil.MINUTES_IN_HOUR;
			}

			else if (stUnit.contains("minute")) {
				nMinutes += nTime;
			}
		}
		return nMinutes;
	}

	public String toString() {
		StringBuilder sbWorkLog = new StringBuilder();
		sbWorkLog.append("Work log id = " + m_nId);
		sbWorkLog.append(". Work log ticket id = " + m_nTicketId);
		sbWorkLog.append(". Work log employee = " + m_employee);
		sbWorkLog.append(". Work log date = " + m_date);
		sbWorkLog.append(". Work log time spent = " + m_stTimeSpent);
		sbWorkLog.append(". Work log text = " + m_stComment);
		return sbWorkLog.toString();
	}

	public static void createWorkLogs(ArrayList<Integer> alWorkLogIds, int nTicketId,
			ArrayList<JiraEmployee> alWorkLogEmployees, ArrayList<JiraDate> alWorkLogDates,
			ArrayList<String> alWorkLogTimesSpent, ArrayList<String> alWorkLogComments)
					throws IncompleteDataException, SQLException, ConfigException {

		int nWorkLogsFound = alWorkLogIds.size();
		if (nWorkLogsFound != alWorkLogEmployees.size() || nWorkLogsFound != alWorkLogDates.size()
				|| nWorkLogsFound != alWorkLogTimesSpent.size() || nWorkLogsFound != alWorkLogComments.size())
			throw new IncompleteDataException("Don't have all of necessary work log components");

		deleteOldWorkLogs(nTicketId);

		Log.logThread("Creating " + nWorkLogsFound + " work logs.");

		for (int nIdx = 0; nIdx < nWorkLogsFound; nIdx++) {
			JiraTicketWorkLog newWorkLog = new JiraTicketWorkLog(alWorkLogIds.get(nIdx), nTicketId,
					alWorkLogEmployees.get(nIdx), alWorkLogDates.get(nIdx), alWorkLogTimesSpent.get(nIdx),
					alWorkLogComments.get(nIdx));
			Log.logThread((nIdx + 1) + " " + newWorkLog.toString());
			newWorkLog.insertOrUpdate();
		}
	}

	private static void deleteOldWorkLogs(int nTicketId) throws ConfigException, SQLException {
		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qRemoveWorkLogs = new AppDBQuery(CFG_DELETE_WORK_LOGS, nTicketId);
		dbConn.executeUpdate(qRemoveWorkLogs);
	}
}
