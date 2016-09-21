package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;

import app.Log;
import db.DBConnection;
import db.UserDBQuery;
import exceptions.QueryException;

public class SpreadsheetReport extends ServerTempFile {

	private static final String REPORT_NAME_IDENTIFIER = "report_name";
	private static final String REPORT_TABS_IDENTIFIER = "report_tabs";
	private static final String TAB_NAME_IDENTIFIER = "tab_name";
	private static final String TAB_QUERIES_IDENTIFIER = "tab_queries";
	private static final String QUERY_NAME_IDENTIFIER = "query_name";
	private static final String QUERY_VALUE_IDENTIFIER = "query_value";

	private static final String DEFAULT_SPREADSHEET_EXTENSION = ".xlsx";
	private static final String DEFAULT_SPREADSHEET_AUTHOR = "WebCrawler";

	private static final int CELL_STYLE_QUERY_NAME = 0;
	private static final int CELL_STYLE_QUERY_COLUMN = 1;
	private static final int CELL_STYLE_QUERY_VALUE = 2;

	private static final int QUERY_NAME_CELLS_MERGED = 25;

	private JsonObject m_jsonReportFormat;
	private XSSFWorkbook m_workbook;
	private CellStyle[] m_cellStyles;

	public SpreadsheetReport(JsonObject jsonReportFormat) throws IOException {
		super(jsonReportFormat.getString(REPORT_NAME_IDENTIFIER) + DEFAULT_SPREADSHEET_EXTENSION);
		m_jsonReportFormat = jsonReportFormat;
	}

	public void generateReport() throws IOException, QueryException, SQLException {
		m_workbook = new XSSFWorkbook();
		setReportProperties();
		createCellStyles();

		DBConnection dbConn = DBConnection.getDBConnection();
		JsonArray reportTabs = m_jsonReportFormat.getJsonArray(REPORT_TABS_IDENTIFIER);
		for (JsonValue jsonValReportTab : reportTabs) {
			JsonObject jsonObjReportTab = (JsonObject) jsonValReportTab;

			String stTabName = jsonObjReportTab.getString(TAB_NAME_IDENTIFIER);
			Sheet currentTab = m_workbook.createSheet(stTabName);
			Log.logThread("Created tab " + stTabName);

			int nRow = 0;
			JsonArray tabQueries = jsonObjReportTab.getJsonArray(TAB_QUERIES_IDENTIFIER);
			for (JsonValue jsonValTabQuery : tabQueries) {
				JsonObject jsonObjTabQuery = (JsonObject) jsonValTabQuery;

				String stQueryName = jsonObjTabQuery.getString(QUERY_NAME_IDENTIFIER);
				Row currentRow = currentTab.createRow(nRow++);
				Cell currentCell = currentRow.createCell(0);
				currentCell.setCellValue(stQueryName);
				currentCell.setCellStyle(m_cellStyles[CELL_STYLE_QUERY_NAME]);
				currentTab.addMergedRegion(new CellRangeAddress(nRow - 1, nRow - 1, 0, QUERY_NAME_CELLS_MERGED));
				Log.logThread("Added query name for: " + stQueryName);

				String stQueryValue = jsonObjTabQuery.getString(QUERY_VALUE_IDENTIFIER);
				UserDBQuery query = new UserDBQuery(stQueryValue);

				if (!query.isAllowable()) {
					throw new QueryException("Forbidden query: " + stQueryName);
				}

				ResultSet rSet = dbConn.executeQuery(query);
				ResultSetMetaData rsMetaData = rSet.getMetaData();

				currentRow = currentTab.createRow(nRow++);
				int nColumns = rsMetaData.getColumnCount();
				Integer[] aNColumnTypes = new Integer[nColumns];

				for (int nIdxCol = 0; nIdxCol < nColumns; nIdxCol++) {
					aNColumnTypes[nIdxCol] = rsMetaData.getColumnType(nIdxCol + 1);
					String stColumnLabel = rsMetaData.getColumnLabel(nIdxCol + 1);
					currentCell = currentRow.createCell(nIdxCol);
					currentCell.setCellValue(stColumnLabel);
					currentCell.setCellStyle(m_cellStyles[CELL_STYLE_QUERY_COLUMN]);
					Log.logThread("Added header: " + stColumnLabel);
				}

				while (rSet.next()) {
					currentRow = currentTab.createRow(nRow++);

					for (int nIdxCol = 1; nIdxCol <= nColumns; nIdxCol++) {
						currentCell = currentRow.createCell(nIdxCol - 1);
						currentCell.setCellStyle(m_cellStyles[CELL_STYLE_QUERY_VALUE]);

						switch (aNColumnTypes[nIdxCol - 1]) {
						case java.sql.Types.SMALLINT:
						case java.sql.Types.INTEGER:
						case java.sql.Types.BIGINT:
						case java.sql.Types.DECIMAL:
						case java.sql.Types.DOUBLE:
						case java.sql.Types.NUMERIC:
							currentCell.setCellValue(rSet.getDouble(nIdxCol));
							break;
						case java.sql.Types.TIMESTAMP:
							Timestamp timeStamp = rSet.getTimestamp(nIdxCol);
							currentCell.setCellValue(timeStamp.toString());
							break;
						case java.sql.Types.VARCHAR:
							String stVarChar = rSet.getString(nIdxCol);
							currentCell.setCellValue(stVarChar);
							break;
						case java.sql.Types.NVARCHAR:
							String stNVarChar = rSet.getNString(nIdxCol);
							currentCell.setCellValue(stNVarChar);
							break;
						case java.sql.Types.BOOLEAN:
							currentCell.setCellValue(rSet.getBoolean(nIdxCol));
							break;
						default:
							throw new SQLException("Unknown column type " + aNColumnTypes[nIdxCol - 1]
									+ " for column number " + nIdxCol);
						}
					}
				}
				currentTab.createRow(nRow++);
			}
		}
		try (FileOutputStream fos = new FileOutputStream(getAbsolutePath())) {
			m_workbook.write(fos);
		}
	}

	private void setReportProperties() {
		POIXMLProperties xmlProps = m_workbook.getProperties();

		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator(DEFAULT_SPREADSHEET_AUTHOR);

		CTProperties extendedProps = xmlProps.getExtendedProperties().getUnderlyingProperties();
		extendedProps.setApplication(DEFAULT_SPREADSHEET_AUTHOR);
	}

	private void createCellStyles() {
		CellStyle queryNameStyle = m_workbook.createCellStyle();
		Font queryNameFont = m_workbook.createFont();
		queryNameFont.setItalic(true);
		queryNameFont.setFontHeightInPoints((short) 12);
		queryNameStyle.setAlignment(CellStyle.ALIGN_LEFT);
		queryNameStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		queryNameStyle.setFont(queryNameFont);

		CellStyle queryValueStyle = m_workbook.createCellStyle();
		queryValueStyle.cloneStyleFrom(queryNameStyle);
		Font queryValueFont = m_workbook.createFont();
		queryValueFont.setFontHeightInPoints((short) 12);
		queryValueStyle.setFont(queryValueFont);
		queryValueStyle.setBorderTop(CellStyle.BORDER_THIN);
		queryValueStyle.setBorderRight(CellStyle.BORDER_THIN);
		queryValueStyle.setBorderLeft(CellStyle.BORDER_THIN);
		queryValueStyle.setBorderBottom(CellStyle.BORDER_THIN);

		CellStyle queryColumnStyle = m_workbook.createCellStyle();
		queryColumnStyle.cloneStyleFrom(queryValueStyle);
		Font queryColumnFont = m_workbook.createFont();
		queryColumnFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		queryColumnFont.setFontHeightInPoints((short) 12);
		queryColumnStyle.setFont(queryColumnFont);

		m_cellStyles = new CellStyle[] { queryNameStyle, queryColumnStyle, queryValueStyle};
	}
}
