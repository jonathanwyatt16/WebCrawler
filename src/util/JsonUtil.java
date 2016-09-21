package util;

import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import app.Log;

public class JsonUtil {

	public static JsonObject getJsonObject(String stJsonString) {
		StringReader stringReader = new StringReader(stJsonString);
		JsonReader jsonReader = Json.createReader(stringReader);
		
		return jsonReader.readObject();
	}

	public static JsonArray getJsonArray(ResultSet rSet) throws IOException, SQLException {
		ResultSetMetaData rsMetaData = rSet.getMetaData();
		int nColumns = rsMetaData.getColumnCount();
		String[] aStColumnNames = new String[nColumns];
		Integer[] aNColumnTypes = new Integer[nColumns];

		for (int nIdxCol = 0; nIdxCol < nColumns; nIdxCol++) {
			String stColumnLabel = rsMetaData.getColumnLabel(nIdxCol + 1);

			if (stColumnLabel == null || stColumnLabel.length() == 0)
				stColumnLabel = "No Column Alias Given";

			aStColumnNames[nIdxCol] = stColumnLabel;
			aNColumnTypes[nIdxCol] = rsMetaData.getColumnType(nIdxCol + 1);
		}

		Log.logThread("ColNames: " + Arrays.toString(aStColumnNames));
		Log.logThread("ColTypes: " + Arrays.toString(aNColumnTypes));

		JsonArrayBuilder queryBuilder = Json.createArrayBuilder();

		while (rSet.next()) {
			JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
			for (int nIdxCol = 1; nIdxCol <= nColumns; nIdxCol++) {
				String stColumnName = aStColumnNames[nIdxCol - 1];

				switch (aNColumnTypes[nIdxCol - 1]) {
				case java.sql.Types.SMALLINT:
					rowBuilder.add(stColumnName, rSet.getShort(nIdxCol));
					break;
				case java.sql.Types.INTEGER:
					rowBuilder.add(stColumnName, rSet.getInt(nIdxCol));
					break;
				case java.sql.Types.BIGINT:
					rowBuilder.add(stColumnName, rSet.getLong(nIdxCol));
					break;
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.NUMERIC:
					rowBuilder.add(stColumnName, rSet.getDouble(nIdxCol));
					break;
				case java.sql.Types.TIMESTAMP:
					Timestamp timeStamp = rSet.getTimestamp(nIdxCol);
					rowBuilder.add(stColumnName, timeStamp == null ? "NULL" : timeStamp.toString());
					break;
				case java.sql.Types.VARCHAR:
					String stVarChar = rSet.getString(nIdxCol);
					rowBuilder.add(stColumnName, stVarChar == null ? "NULL" : stVarChar);
					break;
				case java.sql.Types.NVARCHAR:
					String stNVarChar = rSet.getNString(nIdxCol);
					rowBuilder.add(stColumnName, stNVarChar == null ? "NULL" : stNVarChar);
					break;
				case java.sql.Types.BOOLEAN:
					rowBuilder.add(stColumnName, rSet.getBoolean(nIdxCol));
					break;
				default:
					throw new SQLException(
							"Unknown column type " + aNColumnTypes[nIdxCol - 1] + " for column " + stColumnName);
				}
			}
			queryBuilder.add(rowBuilder.build());
		}

		return queryBuilder.build();
	}
}
