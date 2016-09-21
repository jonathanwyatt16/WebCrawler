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

public class JiraTicketComment extends DBTuple {

	private static final String CFG_DELETE_COMMENTS = "DeleteComments";
	private static final int DB_MAX_LENGTH_COMMENT = 1000;

	private int m_nId;
	private int m_nTicketId;
	private JiraEmployee m_employee;
	private DateUtil m_date;
	private String m_stText;

	public JiraTicketComment(int nCommentId, int nTicketId, JiraEmployee commentEmployee, DateUtil dComment,
			String stCommentText) {
		m_nId = nCommentId;
		m_nTicketId = nTicketId;
		m_employee = commentEmployee;
		m_date = dComment;
		m_stText = stCommentText;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[4];
		aObjTupleVals[0] = m_nTicketId;

		m_employee.insertIfPossible();
		aObjTupleVals[1] = m_employee.findId();
		aObjTupleVals[2] = m_date.toTimestamp();
		aObjTupleVals[3] = StringUtil.ensureMaxLength(m_stText, DB_MAX_LENGTH_COMMENT);

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_nId };
		return aObjUniqueKey;
	}

	public String toString() {
		StringBuilder sbComment = new StringBuilder();
		sbComment.append("Comment id = " + m_nId);
		sbComment.append(". Comment ticket id = " + m_nTicketId);
		sbComment.append(". Comment employee = " + m_employee);
		sbComment.append(". Comment date = " + m_date);
		sbComment.append(". Comment text = " + m_stText);
		return sbComment.toString();
	}

	public static void createComments(ArrayList<Integer> alCommentIds, int nTicketId,
			ArrayList<JiraEmployee> alCommentEmployees, ArrayList<JiraDate> alCommentDates,
			ArrayList<String> alCommentTexts) throws IncompleteDataException, SQLException, ConfigException {

		int nCommentsFound = alCommentIds.size();
		if (nCommentsFound != alCommentEmployees.size() || nCommentsFound != alCommentDates.size()
				|| nCommentsFound != alCommentTexts.size())
			throw new IncompleteDataException("Don't have all of the necessary comment components");

		deleteOldComments(nTicketId);

		Log.logThread("Creating " + nCommentsFound + " comments.");

		for (int nIdx = 0; nIdx < nCommentsFound; nIdx++) {
			JiraTicketComment newComment = new JiraTicketComment(alCommentIds.get(nIdx), nTicketId,
					alCommentEmployees.get(nIdx), alCommentDates.get(nIdx), alCommentTexts.get(nIdx));
			Log.logThread((nIdx + 1) + " " + newComment.toString());
			newComment.insertOrUpdate();
		}
	}

	private static void deleteOldComments(int nTicketId) throws SQLException, ConfigException {
		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qRemoveComments = new AppDBQuery(CFG_DELETE_COMMENTS, nTicketId);
		dbConn.executeUpdate(qRemoveComments);
	}
}
