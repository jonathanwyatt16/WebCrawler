package server;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.json.JsonArray;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import app.Log;
import db.DBConnection;
import db.QueryAudit;
import db.UserDBQuery;
import exceptions.HttpResponseException;
import exceptions.QueryException;
import util.DateUtil;
import util.JsonUtil;

public class UserDBQueryResponder extends HttpPostResponder {

	private static final String QUERY_IDENTIFIER = "userQuery";

	public UserDBQueryResponder(HttpServletRequest request) {
		super(request);
	}

	@Override
	public void respond(HttpServletResponse response) throws HttpResponseException {
		try {
			Long lStart = System.currentTimeMillis();
			String stQuery = ((String[]) m_mapParam.get(QUERY_IDENTIFIER))[0];
			QueryAudit qAudit = new QueryAudit(new DateUtil(), stQuery, m_request.getRemoteAddr());

			UserDBQuery userQuery = new UserDBQuery(stQuery);
			if (!userQuery.isAllowable()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden query.");
				qAudit.setStatus("Forbidden.");
				qAudit.insert();
				throw new QueryException("Forbidden query: " + stQuery);
			}

			DBConnection dbConn = DBConnection.getDBConnection();
			ResultSet rUserQuery = null;
			try {
				rUserQuery = dbConn.executeQuery(userQuery);
			} catch (SQLServerException sse) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, sse.getMessage());
				qAudit.setStatus("Error: " + sse.getMessage());
				qAudit.insert();
				throw new SQLException("Error query: " + stQuery);
			}

			JsonArray jsonQuery = JsonUtil.getJsonArray(rUserQuery);
			String stJson = jsonQuery.toString();
			Log.logThread("Query JSON: " + stJson);

			qAudit.setStatus("Took " + (System.currentTimeMillis() - lStart) + " ms.");
			qAudit.insert();

			response.setContentType(IHttpCallResponder.CONTENT_TYPE_JSON);
			PrintWriter responseWriter = response.getWriter();
			responseWriter.write(stJson);
			responseWriter.close();
		} catch (Exception e) {
			Log.logThread(e);
			throw new HttpResponseException(e.getMessage());
		}
	}
}
