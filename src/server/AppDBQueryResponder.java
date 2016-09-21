package server;

import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.json.JsonArray;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.Log;
import db.AppDBQuery;
import db.DBConnection;
import exceptions.HttpResponseException;
import util.JsonUtil;

public class AppDBQueryResponder extends HttpPostResponder {

	private static final String QUERY_NAME_IDENTIFIER = "appQueryName";

	protected AppDBQueryResponder(HttpServletRequest request) {
		super(request);
	}

	@Override
	public void respond(HttpServletResponse response) throws HttpResponseException {
		try {
			response.setContentType(IHttpCallResponder.CONTENT_TYPE_JSON);

			String stQuery = ((String[]) m_mapParam.get(QUERY_NAME_IDENTIFIER))[0];
			AppDBQuery appQuery = new AppDBQuery(stQuery);

			DBConnection dbConn = DBConnection.getDBConnection();
			ResultSet rUserQuery = dbConn.executeQuery(appQuery);

			JsonArray jsonQuery = JsonUtil.getJsonArray(rUserQuery);

			PrintWriter responseWriter = response.getWriter();
			responseWriter.write(jsonQuery.toString());
			responseWriter.close();

		} catch (Exception e) {
			Log.logThread(e);
			throw new HttpResponseException(e.getMessage());
		}
	}

}
