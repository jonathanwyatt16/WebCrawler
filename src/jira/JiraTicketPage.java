package jira;

import java.sql.SQLException;
import java.util.ArrayList;

import app.Log;
import exceptions.ConfigException;
import exceptions.CrawlException;
import exceptions.NonexistentPageException;
import exceptions.NotImplementedException;
import webcrawler.CrawlablePage;
import webcrawler.PageElement;
import webcrawler.WebCrawlRunScheduler.WebCrawlerType;

public class JiraTicketPage extends CrawlablePage implements Comparable<JiraTicketPage> {

	private static final String CFG_TICKET_TITLE = "Title";
	private static final String CFG_TICKET_TYPE = "Type";
	private static final String CFG_TICKET_PRIORITY = "Priority";
	private static final String CFG_TICKET_CUSTOMER = "Customer";
	private static final String CFG_TICKET_AVRD_CUSTOMER = "AVRDCustomerName";
	private static final String CFG_TICKET_OPERATING_SYSTEM = "OperatingSystem";
	private static final String CFG_TICKET_MAIL_SERVER = "MailServer";
	private static final String CFG_TICKET_ZL_VERSION = "ZLVersion";
	private static final String CFG_TICKET_ZL_BUILD = "ZLBuildNumber";
	private static final String CFG_TICKET_STATUS = "Status";
	private static final String CFG_TICKET_RESOLUTION = "Resolution";
	private static final String CFG_TICKET_ASSIGNEE = "Assignee";
	private static final String CFG_TICKET_REPORTER = "Reporter";
	private static final String CFG_TICKET_CREATED_UPDATED_RESOLVED = "CreatedUpdatedResolved";
	private static final String CFG_TICKET_DESCRIPTION = "Description";
	private static final String CFG_WATCHER = "Watchers";
	private static final String CFG_COMMENT_ID = "CommentId";
	private static final String CFG_COMMENT_EMPLOYEE = "CommentEmployee";
	private static final String CFG_COMMENT_DATE = "CommentDate";
	private static final String CFG_COMMENT_TEXT = "CommentText";
	private static final String CFG_WORK_LOG_ID = "WorkLogId";
	private static final String CFG_WORK_LOG_EMPLOYEE = "WorkLogEmployee";
	private static final String CFG_WORK_LOG_DATE = "WorkLogDate";
	private static final String CFG_WORK_LOG_TIME_SPENT = "WorkLogTimeSpent";
	private static final String CFG_WORK_LOG_COMMENT = "WorkLogComment";

	private JiraTicket m_ticket;

	public JiraTicketPage(String stId) throws ConfigException {
		super(WebCrawlerType.JIRA, false);
		m_stTitle = stId;
		m_ticket = new JiraTicket(m_stTitle);
	}

	@Override
	public void init() throws ConfigException {
		loadPageCfg();
	}

	@Override
	public void crawl() throws CrawlException {
		Log.logThread("Crawling " + m_stTitle);
		try {
			if (m_elements.size() == 0) {
				throw new NonexistentPageException(m_stTitle + " does not exist: " + m_page.asText());
			}

			int nIdxElem = 0;
			String stTicketId = m_elements.get(nIdxElem++).getText();
			Log.logThread("Ticket Id = " + stTicketId);

			if (!stTicketId.equals(m_stTitle))
				throw new NonexistentPageException(m_stTitle + " rerouted to ticket " + stTicketId);

			setHtmlPage(m_elements.get(nIdxElem++).clickElement(), true, true);
			setHtmlPage(m_elements.get(nIdxElem++).clickElement(), true, true);

			JiraTicket jtTicket = new JiraTicket(stTicketId);
			jtTicket.setLatestCrawlId(m_crawlRun.getId());

			ArrayList<JiraEmployee> alWatchers = new ArrayList<JiraEmployee>();
			ArrayList<Integer> alCommentIds = new ArrayList<Integer>();
			ArrayList<JiraEmployee> alCommentEmployees = new ArrayList<JiraEmployee>();
			ArrayList<JiraDate> alCommentDates = new ArrayList<JiraDate>();
			ArrayList<String> alCommentTexts = new ArrayList<String>();
			ArrayList<Integer> alWorkLogIds = new ArrayList<Integer>();
			ArrayList<JiraEmployee> alWorkLogEmployees = new ArrayList<JiraEmployee>();
			ArrayList<JiraDate> alWorkLogDates = new ArrayList<JiraDate>();
			ArrayList<String> alWorkLogTimesSpent = new ArrayList<String>();
			ArrayList<String> alWorkLogComments = new ArrayList<String>();

			for (; nIdxElem < m_elements.size(); nIdxElem++) {
				PageElement pageElement = m_elements.get(nIdxElem);
				switch (pageElement.getName()) {

				case CFG_TICKET_TITLE:
					jtTicket.setTitle(pageElement.getText());
					break;
				case CFG_TICKET_TYPE:
					jtTicket.setType(pageElement.getText());
					break;
				case CFG_TICKET_PRIORITY:
					jtTicket.setPriority(pageElement.getText());
					break;
				case CFG_TICKET_CUSTOMER:
				case CFG_TICKET_AVRD_CUSTOMER:
					jtTicket.setCustomer(pageElement.getText());
					break;
				case CFG_TICKET_OPERATING_SYSTEM:
					jtTicket.setOS(pageElement.getText());
					break;
				case CFG_TICKET_MAIL_SERVER:
					jtTicket.setMailServer(pageElement.getText());
					break;
				case CFG_TICKET_ZL_VERSION:
					jtTicket.setZLVersion(pageElement.getText());
					break;
				case CFG_TICKET_ZL_BUILD:
					jtTicket.setZLBuild(pageElement.getText());
					break;
				case CFG_TICKET_STATUS:
					jtTicket.setStatus(pageElement.getText());
					break;
				case CFG_TICKET_RESOLUTION:
					jtTicket.setResolution(pageElement.getText());
					break;
				case CFG_TICKET_ASSIGNEE:
					jtTicket.setAssignee(pageElement.getText());
					break;
				case CFG_TICKET_REPORTER:
					jtTicket.setReporter(pageElement.getText());
					break;
				case CFG_TICKET_CREATED_UPDATED_RESOLVED:
					jtTicket.setCreatedUpdatedResolved(pageElement.getTitle());
					break;
				case CFG_TICKET_DESCRIPTION:
					jtTicket.setDescription(pageElement.getText());
					break;
				case CFG_WATCHER:
					String[] aStWatcher = pageElement.getText().split("Remove watcher");
					for (int idx = 0; idx < aStWatcher.length - 1; idx++) {
						String stWatcher = aStWatcher[idx];
						stWatcher = stWatcher.substring(stWatcher.indexOf(".") + 1).trim();
						alWatchers.add(new JiraEmployee(stWatcher));
						Log.logThread("Added watcher " + stWatcher);
					}
					break;
				case CFG_COMMENT_ID:
					String stCommentId = pageElement.getId();
					stCommentId = stCommentId.substring(stCommentId.indexOf("-") + 1);
					int nCommentId = Integer.parseInt(stCommentId);
					alCommentIds.add(nCommentId);
					Log.logThread("Added comment ID = " + nCommentId);
					break;
				case CFG_COMMENT_EMPLOYEE:
					String stCommentEmployee = pageElement.getText();
					JiraEmployee commentEmployee = new JiraEmployee(stCommentEmployee);
					alCommentEmployees.add(commentEmployee);
					Log.logThread("Added comment employee = " + commentEmployee);
					break;
				case CFG_COMMENT_DATE:
					String stCommentDate = pageElement.getTitle();
					JiraDate jdCommentDate = new JiraDate(stCommentDate);
					alCommentDates.add(jdCommentDate);
					Log.logThread("Added comment date " + jdCommentDate);
					break;
				case CFG_COMMENT_TEXT:
					String stCommentText = pageElement.getText();
					alCommentTexts.add(stCommentText);
					Log.logThread("Added comment text " + stCommentText);
					break;
				case CFG_WORK_LOG_ID:
					String stWorkLogId = pageElement.getId();
					stWorkLogId = stWorkLogId.substring(stWorkLogId.indexOf("-") + 1);
					int nWorkLogId = Integer.parseInt(stWorkLogId);
					alWorkLogIds.add(nWorkLogId);
					Log.logThread("Added work log ID " + nWorkLogId);
					break;
				case CFG_WORK_LOG_EMPLOYEE:
					String stWorkLogEmployee = pageElement.getText();
					JiraEmployee workLogEmployee = new JiraEmployee(stWorkLogEmployee);
					alWorkLogEmployees.add(workLogEmployee);
					Log.logThread("Added work log employee = " + workLogEmployee);
					break;
				case CFG_WORK_LOG_DATE:
					String stWorkLogDate = pageElement.getText();
					JiraDate jdWorkLogDate = new JiraDate(stWorkLogDate);
					alWorkLogDates.add(jdWorkLogDate);
					Log.logThread("Added work log date " + jdWorkLogDate);
					break;
				case CFG_WORK_LOG_TIME_SPENT:
					String stTimeSpent = pageElement.getText();
					alWorkLogTimesSpent.add(stTimeSpent);
					Log.logThread("Added work log time spent " + stTimeSpent);
					break;
				case CFG_WORK_LOG_COMMENT:
					String stWorkLogComment = pageElement.getText();
					alWorkLogComments.add(stWorkLogComment);
					Log.logThread("Added work log comment " + stWorkLogComment);
					break;

				default:
					throw new NotImplementedException("Unknown ticket element name " + pageElement.getName());
				}
			}

			Log.logThread("Done constucting ticket " + stTicketId + ".\n" + jtTicket.toString());
			jtTicket.insertOrUpdate();
			int nTicketId = jtTicket.findId();

			JiraTicketWatcher.createWatchers(alWatchers, nTicketId);

			JiraTicketComment.createComments(alCommentIds, nTicketId, alCommentEmployees, alCommentDates,
					alCommentTexts);

			JiraTicketWorkLog.createWorkLogs(alWorkLogIds, nTicketId, alWorkLogEmployees, alWorkLogDates,
					alWorkLogTimesSpent, alWorkLogComments);

		} catch (NotImplementedException | SQLException | ConfigException e) {
			Log.logThread(e);
			throw new CrawlException(e.getMessage());
		}

	}

	public JiraTicket getTicket() {
		return m_ticket;
	}

	public String getTicketId() {
		return m_ticket.getId();
	}

	public boolean equals(Object otherObj) {
		JiraTicketPage otherPage = (JiraTicketPage) otherObj;
		return m_ticket.equals(otherPage.m_ticket);
	}

	public int hashCode() {
		return m_ticket.hashCode();
	}

	public int compareTo(JiraTicketPage otherJtp) {
		return m_ticket.compareTo(otherJtp.m_ticket);
	}

	public String toString() {
		return m_ticket.getId();
	}
}
