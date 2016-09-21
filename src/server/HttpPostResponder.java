package server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import app.Log;
import exceptions.HttpResponseException;

public abstract class HttpPostResponder implements IHttpCallResponder {
	private static final String RESPONSE_USER_DB_QUERY = "userDBQuery";
	private static final String RESPONSE_APP_DB_QUERY = "appDBQuery";
	private static final String RESPONSE_EXCEL_REPORT = "spreadsheetReport";

	protected HttpServletRequest m_request;
	protected Map<String, Object> m_mapParam;

	@SuppressWarnings("unchecked")
	protected HttpPostResponder(HttpServletRequest request) {
		m_request = request;
		m_mapParam = request.getParameterMap();
	}

	@SuppressWarnings("unchecked")
	public static HttpPostResponder getResponder(HttpServletRequest request) throws HttpResponseException {
		Map<String, Object> mapParam = request.getParameterMap();
		String stRequest = ((String[]) mapParam.get(REQUEST_IDENTIFIER))[0];
		Log.logThread("Post request = " + stRequest);

		if (stRequest == null)
			throw new HttpResponseException("Post request not specified.");

		switch (stRequest) {
		case (RESPONSE_USER_DB_QUERY):
			return new UserDBQueryResponder(request);

		case (RESPONSE_APP_DB_QUERY):
			return new AppDBQueryResponder(request);
		
		case (RESPONSE_EXCEL_REPORT):
			return new SpreadsheetReportResponder(request);

		default:
			throw new HttpResponseException("Invalid HTTP post responder: " + stRequest);
		}
	}

}
