package db;

import java.sql.SQLException;

import util.DateUtil;
import util.StringUtil;
import db.DBTuple;
import exceptions.ConfigException;

public class QueryAudit extends DBTuple {
	
	private static final int DB_MAX_LENGTH_QUERY = 1000;

	private DateUtil m_date;
	private String m_stQuery;
	private String m_stIP;
	private String m_stStatus;
	
	public QueryAudit(DateUtil date, String stQuery, String stIP, String stStatus) {
		m_date = date;
		m_stQuery = stQuery;
		m_stIP = stIP;
		m_stStatus = stStatus;
	}
	
	public QueryAudit(DateUtil date, String stQuery, String stIP) {
		this(date, stQuery, stIP, null);
	}

	public void setStatus(String stStatus) {
		m_stStatus = stStatus;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		Object[] aObjTupleVals = new Object[4];

		aObjTupleVals[0] = m_date.toTimestamp();
		aObjTupleVals[1] = m_stIP;
		aObjTupleVals[2] = StringUtil.ensureMaxLength(m_stQuery, DB_MAX_LENGTH_QUERY);
		aObjTupleVals[3] = m_stStatus;

		return aObjTupleVals;
	}

	@Override
	protected Object[] getUniqueKey() throws SQLException, ConfigException {
		return null;
	}

	public String toString() {
		return "QueryAudit from " + m_date;
	}
}
