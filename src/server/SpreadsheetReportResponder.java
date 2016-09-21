package server;

import java.io.PrintWriter;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.Log;
import db.QueryAudit;
import exceptions.HttpResponseException;
import util.DateUtil;
import util.JsonUtil;

public class SpreadsheetReportResponder extends HttpPostResponder {

	private static final String REPORT_IDENTIFIER = "reportFormat";

	protected SpreadsheetReportResponder(HttpServletRequest request) {
		super(request);
	}

	@Override
	public void respond(HttpServletResponse response) throws HttpResponseException {
		try {
			Long lStart = System.currentTimeMillis();
			DateUtil dNow = new DateUtil();

			String stJsonReportFormat = ((String[]) m_mapParam.get(REPORT_IDENTIFIER))[0];
			JsonObject jsonReportFormat = JsonUtil.getJsonObject(stJsonReportFormat);

			SpreadsheetReport spreadsheetReport = new SpreadsheetReport(jsonReportFormat);
			spreadsheetReport.generateReport();

			QueryAudit qAudit = new QueryAudit(dNow, spreadsheetReport.getFileName(), m_request.getRemoteAddr(),
					"Took " + (System.currentTimeMillis() - lStart) + " ms.");
			qAudit.insert();

			response.setContentType(IHttpCallResponder.CONTENT_TYPE_TEXT);
			PrintWriter responseWriter = response.getWriter();
			responseWriter.write(spreadsheetReport.getRelativePath());
			responseWriter.close();
		} catch (Exception e) {
			Log.logThread(e);
			throw new HttpResponseException(e.getMessage());
		}
	}
}
