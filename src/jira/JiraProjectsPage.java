package jira;

import java.sql.SQLException;
import java.util.ArrayList;

import app.Log;
import exceptions.ConfigException;
import exceptions.HtmlElementException;
import exceptions.HtmlPageOpenException;
import exceptions.IncompleteDataException;
import webcrawler.Page;
import webcrawler.PageElement;

public class JiraProjectsPage extends Page {

	private static final String CFG_JIRA_PROJECT_NAME = "ProjectName";
	private static final String CFG_JIRA_PROJECT_OWNER = "ProjectOwner";

	public JiraProjectsPage() throws ConfigException {
		super(true);
	}

	public void getProjects() throws IncompleteDataException, SQLException, ConfigException {
		Log.logThread("Getting projects.");

		ArrayList<String> alProjNames = new ArrayList<String>();
		ArrayList<String> alProjAbbrevs = new ArrayList<String>();
		ArrayList<JiraEmployee> alProjOwners = new ArrayList<JiraEmployee>();

		for (PageElement element : m_elements) {
			String stElemName = element.getName();
			String stElemText = element.getText();

			if (stElemName.equals(CFG_JIRA_PROJECT_NAME)) {
				alProjNames.add(stElemText);
				String stAbbrev = element.getURL();
				stAbbrev = stAbbrev.substring(stAbbrev.lastIndexOf("/") + 1);
				alProjAbbrevs.add(stAbbrev);
			} else if (stElemName.equals(CFG_JIRA_PROJECT_OWNER)) {
				alProjOwners.add(new JiraEmployee(stElemText));
			}
		}

		if (alProjNames.size() != alProjOwners.size()) {
			throw new IncompleteDataException("Unequal number of project names and owners.");
		}

		for (int nIdx = 0; nIdx < alProjNames.size(); nIdx++) {
			JiraProject newProj = new JiraProject(alProjNames.get(nIdx), alProjAbbrevs.get(nIdx),
					alProjOwners.get(nIdx));
			newProj.insertOrUpdate();
		}
	}

	public JiraTicketSearchPage clickOnSearch() throws ConfigException, HtmlPageOpenException, HtmlElementException {
		JiraTicketSearchPage ticketSearchPage = new JiraTicketSearchPage();
		ticketSearchPage.setHtmlPage(m_elements.get(m_elements.size() - 1).hitEnter(), true, false);
		return ticketSearchPage;
	}
}
