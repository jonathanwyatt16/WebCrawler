package jira;

import java.sql.SQLException;

import db.DBTuple;
import exceptions.ConfigException;

public class JiraCustomer extends DBTuple {

	private String m_stName;

	public JiraCustomer(String stName) {
		m_stName = stName;
	}

	@Override
	protected Object[] getTupleVals() throws SQLException, ConfigException {
		return null;
	}

	@Override
	protected Object[] getUniqueKey() {
		Object[] aObjUniqueKey = { m_stName };
		return aObjUniqueKey;
	}

	public String toString() {
		return m_stName;
	}

}
