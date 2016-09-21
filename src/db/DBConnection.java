package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

import app.Log;

public class DBConnection {

	private static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	private static String s_stConnectionString;

	private Connection m_conn;

	private DBConnection() throws SQLException {
		m_conn = DriverManager.getConnection(s_stConnectionString);
	}

	public static void setJdbcString(String stJdbcString) throws SQLException {
		try {
			Class.forName(JDBC_DRIVER).newInstance();
			s_stConnectionString = stJdbcString;

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Log.logThread(e);
			throw new SQLException("Error setting JDBC string " + stJdbcString);
		}
	}

	public static DBConnection getDBConnection() throws SQLException {
		return new DBConnection();
	}

	public void executeUpdate(DBQuery query) throws SQLException {
		PreparedStatement updateStmt = prepareStatement(query);
		updateStmt.executeUpdate();
	}

	public ResultSet executeQuery(DBQuery query) throws SQLException {
		PreparedStatement queryStmt = prepareStatement(query);
		return queryStmt.executeQuery();
	}

	private PreparedStatement prepareStatement(DBQuery query) throws SQLException {
		Log.logThread("Preparing statement for query " + query.getName() + " = " + query.getQuery());
		PreparedStatement stmt = m_conn.prepareStatement(query.getQuery());
		Object[] aObjVals = query.getVals();

		if (aObjVals == null)
			return stmt;

		Log.logThread("Vals: " + Arrays.toString(aObjVals));
		for (int nIdx = 1; nIdx <= aObjVals.length; nIdx++) {
			Object objVal = aObjVals[nIdx - 1];

			if (objVal == null)
				stmt.setObject(nIdx, null);

			else if (objVal instanceof Short)
				stmt.setShort(nIdx, (Short) objVal);

			else if (objVal instanceof Integer)
				stmt.setInt(nIdx, (Integer) objVal);

			else if (objVal instanceof Long)
				stmt.setLong(nIdx, (Long) objVal);

			else if (objVal instanceof Double)
				stmt.setDouble(nIdx, (double) objVal);

			else if (objVal instanceof String)
				stmt.setString(nIdx, (String) objVal);

			else if (objVal instanceof Timestamp)
				stmt.setTimestamp(nIdx, (Timestamp) objVal);

			else
				throw new SQLException("Invalid value type for " + objVal);
		}
		return stmt;
	}
}
