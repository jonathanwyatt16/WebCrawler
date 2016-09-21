package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import app.Log;
import exceptions.ConfigException;

public abstract class DBTuple {

	protected static final String SELECT_ID_PREFIX = "SelectId";
	protected static final String SELECT_COUNT_PREFIX = "SelectCount";
	protected static final String INSERT_PREFIX = "Insert";
	protected static final String UPDATE_PREFIX = "Update";
	protected static final String DELETE_PREFIX = "Delete";

	protected String m_stTableName;

	protected DBTuple() {
		m_stTableName = this.getClass().getSimpleName();
	}

	protected DBTuple(String stTableName) {
		m_stTableName = stTableName;
	}

	protected abstract Object[] getTupleVals() throws SQLException, ConfigException;

	protected abstract Object[] getUniqueKey() throws SQLException, ConfigException;

	public void insert() throws SQLException, ConfigException {
		Log.logThread("Inserting " + this);

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery insertQuery = new AppDBQuery(INSERT_PREFIX + m_stTableName, prepareObjects(true));

		dbConn.executeUpdate(insertQuery);
	}

	public void update() throws SQLException, ConfigException {
		Log.logThread("Updating " + this);

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qUpdate = new AppDBQuery(UPDATE_PREFIX + m_stTableName, prepareObjects(false));

		dbConn.executeUpdate(qUpdate);
	}

	public void insertOrUpdate() throws SQLException, ConfigException {
		if (!insertIfPossible())
			update();
	}

	public boolean insertIfPossible() throws ConfigException, SQLException {
		int nCount = selectCount();
		if (nCount == 0) {
			insert();
			return true;
		}

		return false;
	}

	public int selectCount() throws ConfigException, SQLException {
		Log.logThread("Selecting count of " + m_stTableName + " with key " + Arrays.toString(getUniqueKey()));
		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qFindCount = new AppDBQuery(SELECT_COUNT_PREFIX + m_stTableName, getUniqueKey());
		ResultSet rCount = dbConn.executeQuery(qFindCount);

		rCount.next();
		int nId = rCount.getInt(1);
		Log.logThread("Count = " + nId);

		return nId;
	}

	public int findId() throws SQLException, ConfigException {
		Log.logThread("Finding ID for " + this);

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qFindId = new AppDBQuery(SELECT_ID_PREFIX + m_stTableName, getUniqueKey());
		ResultSet rId = dbConn.executeQuery(qFindId);

		rId.next();
		int nId = rId.getInt(1);
		Log.logThread("Found id = " + nId);

		return nId;
	}

	public void delete() throws SQLException, ConfigException {
		Log.logThread("Deleting " + this);

		DBConnection dbConn = DBConnection.getDBConnection();
		AppDBQuery qDelete = new AppDBQuery(DELETE_PREFIX + m_stTableName, getUniqueKey());

		dbConn.executeUpdate(qDelete);
	}

	private Object[] prepareObjects(boolean bUniqueKeyFirst) throws SQLException, ConfigException {
		Object[] aObjTupleVals = getTupleVals();
		Object[] aObjUniqueKey = getUniqueKey();

		if (aObjTupleVals == null)
			return aObjUniqueKey;

		if (aObjUniqueKey == null)
			return aObjTupleVals;

		Object[] aObjVals = new Object[aObjTupleVals.length + aObjUniqueKey.length];

		int nIdxAdjust = bUniqueKeyFirst ? 0 : aObjTupleVals.length;
		for (int nIdx = 0; nIdx < aObjUniqueKey.length; nIdx++) {
			aObjVals[nIdx + nIdxAdjust] = aObjUniqueKey[nIdx];
		}

		nIdxAdjust = bUniqueKeyFirst ? aObjUniqueKey.length : 0;
		for (int nIdx = 0; nIdx < aObjTupleVals.length; nIdx++) {
			aObjVals[nIdx + nIdxAdjust] = aObjTupleVals[nIdx];
		}

		return aObjVals;
	}
}
