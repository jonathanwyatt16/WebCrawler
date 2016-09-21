package jira;

import java.sql.SQLException;

import app.Log;
import db.DBTuple;
import exceptions.ConfigException;
import util.StringUtil;

public class JiraTicket extends DBTuple implements Comparable<JiraTicket> {

	public static final String ID_SEPARATOR = "-";
	private static final int DB_MAX_LENGTH_DESCRIPTION = 1000;

	private String m_stKey;
	private String m_stTitle;
	private String m_stType;
	private String m_stPriority;
	private String m_stCustomer;
	private String m_stOS;
	private String m_stMailServer;
	private String m_stZLVersion;
	private String m_stZLBuild;
	private String m_stStatus;
	private String m_stResolution;
	private String m_stAssignee;
	private String m_stReporter;
	private String m_stCreatedDate;
	private String m_stUpdatedDate;
	private String m_stResolvedDate;
	private String m_stDescription;
	private int m_nLatestCrawlId;

	public JiraTicket(String stId) {
		m_stKey = stId;
	}

	public int getNumber() {
		String stNumber = m_stKey.substring(m_stKey.indexOf(ID_SEPARATOR) + 1);
		return Integer.parseInt(stNumber);
	}

	public String getId() {
		return m_stKey;
	}

	public boolean equals(Object otherObj) {
		JiraTicket otherTicket = (JiraTicket) otherObj;
		return m_stKey.equals(otherTicket.m_stKey);
	}

	public int hashCode() {
		return m_stKey.hashCode();
	}

	public int compareTo(JiraTicket otherTicket) {
		Integer nThisNumber = getNumber();
		Integer nOtherNumber = otherTicket.getNumber();

		return nThisNumber.equals(nOtherNumber) ? m_stKey.compareTo(otherTicket.m_stKey)
				: nThisNumber.compareTo(nOtherNumber);
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[19];

		aObjTupleVals[0] = new JiraProject(m_stKey.substring(0, m_stKey.indexOf(ID_SEPARATOR))).findId();
		aObjTupleVals[1] = Integer.parseInt(m_stKey.substring(m_stKey.indexOf(ID_SEPARATOR) + 1));
		aObjTupleVals[2] = m_stTitle;
		aObjTupleVals[3] = m_stType;
		aObjTupleVals[4] = m_stPriority;

		if (m_stCustomer == null)
			aObjTupleVals[5] = null;
		else {
			JiraCustomer jcCustomer = new JiraCustomer(m_stCustomer);
			jcCustomer.insertIfPossible();
			aObjTupleVals[5] = jcCustomer.findId();
		}

		aObjTupleVals[6] = m_stOS;
		aObjTupleVals[7] = m_stMailServer;
		aObjTupleVals[8] = m_stZLVersion;
		aObjTupleVals[9] = m_stZLBuild == null ? null : Integer.parseInt(m_stZLBuild);
		aObjTupleVals[10] = m_stStatus;
		aObjTupleVals[11] = m_stResolution;

		JiraEmployee jeAssignee = new JiraEmployee(m_stAssignee);
		jeAssignee.insertIfPossible();
		aObjTupleVals[12] = jeAssignee.findId();

		JiraEmployee jeReporter = new JiraEmployee(m_stReporter);
		jeReporter.insertIfPossible();
		aObjTupleVals[13] = jeReporter.findId();
		aObjTupleVals[14] = new JiraDate(m_stCreatedDate).toTimestamp();
		aObjTupleVals[15] = new JiraDate(m_stUpdatedDate).toTimestamp();
		aObjTupleVals[16] = m_stResolvedDate == null ? null : new JiraDate(m_stResolvedDate).toTimestamp();
		aObjTupleVals[17] = m_nLatestCrawlId;
		aObjTupleVals[18] = StringUtil.ensureMaxLength(m_stDescription, DB_MAX_LENGTH_DESCRIPTION);

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_stKey };
		return aObjUniqueKey;
	}

	public void setTitle(String stTitle) {
		m_stTitle = stTitle;
		Log.logThread("Set title = " + m_stTitle);
	}

	public void setType(String stType) {
		m_stType = stType;
		Log.logThread("Set type = " + m_stType);
	}

	public void setPriority(String stPriority) {
		m_stPriority = stPriority;
		Log.logThread("Set priority = " + m_stPriority);
	}

	public void setCustomer(String stCustomer) {
		m_stCustomer = stCustomer;
		Log.logThread("Set customer = " + m_stCustomer);
	}

	public void setOS(String stOS) {
		m_stOS = stOS;
		Log.logThread("Set OS = " + m_stOS);
	}

	public void setMailServer(String stMailServer) {
		m_stMailServer = stMailServer;
		Log.logThread("Set mail server = " + m_stMailServer);
	}

	public void setZLVersion(String stZLVersion) {
		m_stZLVersion = stZLVersion;
		Log.logThread("Set ZL version = " + m_stZLVersion);
	}

	public void setZLBuild(String stZLBuild) {
		stZLBuild = stZLBuild.replaceAll(",", "");
		if (StringUtil.isAllDigits(stZLBuild)) {
			m_stZLBuild = stZLBuild;
			Log.logThread("Set ZL build = " + m_stZLBuild);
		}

		else
			Log.logThread("Invalid ZL build: " + stZLBuild);
	}

	public void setStatus(String stStatus) {
		m_stStatus = stStatus;
		Log.logThread("Set status = " + m_stStatus);
	}

	public void setResolution(String stResolution) {
		m_stResolution = stResolution;
		Log.logThread("Set resolution = " + m_stResolution);
	}

	public void setAssignee(String stAssignee) {
		m_stAssignee = stAssignee;
		Log.logThread("Set assignee = " + m_stAssignee);
	}

	public void setReporter(String stReporter) {
		m_stReporter = stReporter;
		Log.logThread("Set reporter = " + m_stReporter);
	}

	public void setCreatedUpdatedResolved(String stDate) {
		if (m_stCreatedDate == null) {
			m_stCreatedDate = stDate;
			Log.logThread("Set created date = " + m_stCreatedDate);
		} else if (m_stUpdatedDate == null) {
			m_stUpdatedDate = stDate;
			Log.logThread("Set updated date = " + m_stUpdatedDate);
		} else if (m_stResolvedDate == null) {
			m_stResolvedDate = stDate;
			Log.logThread("Set resolved date = " + m_stResolvedDate);
		}
	}

	public void setDescription(String stDescription) {
		m_stDescription = stDescription;
		Log.logThread("Set description = " + m_stDescription);
	}

	public void setLatestCrawlId(int nCrawlId) {
		m_nLatestCrawlId = nCrawlId;
		Log.logThread("Set latest crawl id = " + nCrawlId);
	}

	public String toString() {
		StringBuilder sbTicket = new StringBuilder();
		sbTicket.append("Ticket id = " + m_stKey);
		sbTicket.append(". Ticket title = " + m_stTitle);
		sbTicket.append(". Ticket type = " + m_stType);
		sbTicket.append(". Ticket priority = " + m_stPriority);
		sbTicket.append(". Ticket customer = " + m_stCustomer);
		sbTicket.append(". Ticket OS = " + m_stOS);
		sbTicket.append(". Ticket mail server = " + m_stMailServer);
		sbTicket.append(". Ticket ZL version = " + m_stZLVersion);
		sbTicket.append(". Ticket ZL build = " + m_stZLBuild);
		sbTicket.append(". Ticket status = " + m_stStatus);
		sbTicket.append(". Ticket resolution = " + m_stResolution);
		sbTicket.append(". Ticket assignee = " + m_stAssignee);
		sbTicket.append(". Ticket reporter = " + m_stReporter);
		sbTicket.append(". Ticket created date = " + m_stCreatedDate);
		sbTicket.append(". Ticket updated date = " + m_stUpdatedDate);
		sbTicket.append(". Ticket resolved date = " + m_stResolvedDate);
		sbTicket.append(". Ticket description = " + m_stDescription);
		sbTicket.append(". Latest crawl id = " + m_nLatestCrawlId);

		return sbTicket.toString();
	}
}
