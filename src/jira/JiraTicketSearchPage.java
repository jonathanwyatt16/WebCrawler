package jira;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import util.DateUtil;
import app.Log;
import exceptions.ConfigException;
import exceptions.GetPagesException;
import exceptions.HtmlElementException;
import exceptions.HtmlPageOpenException;
import webcrawler.CrawlablePage;
import webcrawler.Page;
import webcrawler.PageElement;

public class JiraTicketSearchPage extends Page {

	private static final String CFG_NEXT_BUTTON = "NextButton";
	private static final String CFG_TICKET_IDS = "TicketIDs";

	private static final int JIRA_QUERY_DAYS_HORIZON = 3;
	private static final String JIRA_QUERY_REPLACEMENT_STRING = "#";
	private static final String JIRA_QUERY_TICKET_RANGE = "updatedDate < '#' and updatedDate >= '#' order by updatedDate desc";
	private static final String JIRA_QUERY_LATEST_TICKET = "project = # order by key desc";

	public JiraTicketSearchPage() throws ConfigException {
		super(true);
	}

	public LinkedList<CrawlablePage> getTicketPages(DateUtil dLatestCrawl) throws GetPagesException, SQLException {
		try {
			if (dLatestCrawl == null) {
				Log.logThread("No previous successful crawl found");
				return getAllTicketPages();
			}

			Log.logThread("Latest crawl on " + dLatestCrawl);
			LinkedList<CrawlablePage> llTickets = new LinkedList<CrawlablePage>();
			DateUtil dNow = new DateUtil();
			int nQueries = 0;

			while (dNow.isAfter(dLatestCrawl)) {
				String ticketsBefore = dNow.toString(JiraDate.JIRA_DATE_FORMAT_2);
				dNow.subtractDays(JIRA_QUERY_DAYS_HORIZON);

				if (dLatestCrawl.isAfter(dNow))
					dNow = dLatestCrawl;

				String stTicketsAfter = dNow.toString(JiraDate.JIRA_DATE_FORMAT_2);
				String stJiraDateQuery = JIRA_QUERY_TICKET_RANGE.replaceFirst(JIRA_QUERY_REPLACEMENT_STRING,
						ticketsBefore);
				stJiraDateQuery = stJiraDateQuery.replaceFirst(JIRA_QUERY_REPLACEMENT_STRING, stTicketsAfter);

				Log.logThread("Starting query " + ++nQueries);
				queryForTickets(stJiraDateQuery, llTickets, true);
				Log.logThread("Done with query " + nQueries);
			}

			return llTickets;

		} catch (HtmlPageOpenException | ConfigException e) {
			Log.logThread(e);
			throw new GetPagesException(e.getMessage());
		}
	}

	private LinkedList<CrawlablePage> getAllTicketPages()
			throws SQLException, ConfigException, GetPagesException, HtmlElementException, HtmlPageOpenException {
		Log.logThread("Getting all tickets based on max ID of each project");

		LinkedList<String> llProjAbbrevs = JiraProject.getAllProjectAbbrevs();
		LinkedList<CrawlablePage> llAllTicketPages = new LinkedList<CrawlablePage>();

		for (String stAbbrev : llProjAbbrevs) {
			String stRecentTicketQuery = JIRA_QUERY_LATEST_TICKET.replaceFirst(JIRA_QUERY_REPLACEMENT_STRING, stAbbrev);
			LinkedList<CrawlablePage> llMostRecentTickets = new LinkedList<CrawlablePage>();

			queryForTickets(stRecentTicketQuery, llMostRecentTickets, false);

			Iterator<CrawlablePage> itrMostRecentTickets = llMostRecentTickets.iterator();
			JiraTicket mostRecentTicket = null;

			while (itrMostRecentTickets.hasNext()) {
				JiraTicket nextTicket = ((JiraTicketPage) itrMostRecentTickets.next()).getTicket();
				if (mostRecentTicket == null || mostRecentTicket.compareTo(nextTicket) < 0)
					mostRecentTicket = nextTicket;
			}

			Log.logThread("Most recent " + stAbbrev + " ticket = " + mostRecentTicket);

			for (int nTicketNumber = mostRecentTicket
					.getNumber(), nTicketsFoundThisQuery = 0; nTicketNumber > 0; nTicketNumber--) {
				JiraTicketPage newTicketPage = new JiraTicketPage(stAbbrev + JiraTicket.ID_SEPARATOR + nTicketNumber);

				llAllTicketPages.add(newTicketPage);
				Log.logThread("Added ticket #" + ++nTicketsFoundThisQuery + " for this project, #"
						+ llAllTicketPages.size() + " overall: " + newTicketPage);
			}
		}

		return llAllTicketPages;
	}

	private void queryForTickets(String stJiraQuery, LinkedList<CrawlablePage> llTicketPages,
			boolean bClickThroughAllPages)
					throws GetPagesException, HtmlElementException, HtmlPageOpenException, ConfigException {
		int nIdxElem = 0;
		m_elements.get(nIdxElem).setInputVal(stJiraQuery);
		setHtmlPage(m_elements.get(nIdxElem++).hitEnter(), true, true);

		int nTicketsPreviouslyFound = llTicketPages.size();
		String stTotalTicketsThisQuery = m_elements.size() > 1 ? m_elements.get(nIdxElem++).getText() : "0";
		int nTotalTicketsThisQuery = Integer.parseInt(stTotalTicketsThisQuery);

		Log.logThread("Total tickets for this query = " + nTotalTicketsThisQuery);
		if (nTotalTicketsThisQuery == 0)
			return;

		PageElement nextButton = null;
		for (int nTicketsFoundThisQuery = 0;; setHtmlPage(nextButton.clickElement(), true, true)) {
			for (PageElement pageElement : m_elements) {

				if (pageElement.getName().equals(CFG_NEXT_BUTTON))
					nextButton = pageElement;

				if (pageElement.getName().equals(CFG_TICKET_IDS)) {
					JiraTicketPage newTicketPage = new JiraTicketPage(pageElement.getText());

					llTicketPages.add(newTicketPage);
					Log.logThread("Added ticket #" + ++nTicketsFoundThisQuery + " for this query, #"
							+ llTicketPages.size() + " overall: " + newTicketPage);

				}
			}
			if (nTicketsFoundThisQuery >= nTotalTicketsThisQuery || !bClickThroughAllPages)
				break;
		}

		int nTotalTicketsFound = llTicketPages.size();
		int nTicketsFoundThisQuery = nTotalTicketsFound - nTicketsPreviouslyFound;
		Log.logThread(
				"Now have " + nTotalTicketsFound + " total tickets. This query added " + nTicketsFoundThisQuery + ".");

		if (nTicketsFoundThisQuery != nTotalTicketsThisQuery && bClickThroughAllPages)
			throw new GetPagesException("Did not find the expected number of tickets for query " + stJiraQuery
					+ ". Expected " + nTotalTicketsThisQuery + " but got " + nTicketsFoundThisQuery);
	}
}
