package jira;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.security.auth.login.LoginException;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import app.Log;
import exceptions.ConfigException;
import exceptions.GetPagesException;
import exceptions.HtmlPageOpenException;
import exceptions.IncompleteDataException;
import webcrawler.CrawlablePage;
import webcrawler.WebCrawlRun;
import webcrawler.WebCrawlRunScheduler.WebCrawlerType;

public class JiraCrawlRun extends WebCrawlRun {

	public JiraCrawlRun() throws ConfigException, HtmlPageOpenException, SQLException {
		super(WebCrawlerType.JIRA);
	}

	@Override
	protected void getPagesToCrawl() throws GetPagesException {
		try {
			m_browser = new JiraBrowser();
			m_browser.logIn();

			JiraProjectsPage projectsPage = new JiraProjectsPage();
			m_browser.setPage(projectsPage, true);
			projectsPage.getProjects();

			JiraTicketSearchPage ticketSearch = projectsPage.clickOnSearch();
			m_browser.setPage(ticketSearch, false);
			m_pagesToCrawl = ticketSearch.getTicketPages(m_dPreviousCrawl);
			m_browser = null;

			LinkedList<CrawlablePage> llErrorPages = getPreviousErrorPages();
			if (llErrorPages != null) {
				int nErrorPagesAdded = 0;
				for (CrawlablePage errorPage : llErrorPages) {
					if (!m_pagesToCrawl.contains(errorPage)) {
						m_pagesToCrawl.add(errorPage);
						Log.logThread("Added error page " + ++nErrorPagesAdded + ": " + errorPage);
					} else
						Log.logThread(errorPage + " has already been added.");
				}
			}

			logTicketsFound();

		} catch (FailingHttpStatusCodeException | ConfigException | IncompleteDataException | HtmlPageOpenException
				| ElementNotFoundException | SQLException | LoginException e) {
			Log.logThread(e);
			throw new GetPagesException(e.getMessage());
		}
	}

	private void logTicketsFound() {
		Log.logThread("Done finding tickets. Found " + m_pagesToCrawl.size() + " total tickets:");

		StringBuilder sbAllTickets = new StringBuilder();
		Iterator<CrawlablePage> itrTicketPages = m_pagesToCrawl.iterator();

		while (itrTicketPages.hasNext())
			sbAllTickets.append(itrTicketPages.next().toString() + " ");

		Log.logThread(sbAllTickets.toString());
	}

	@Override
	public String getErrandName() {
		return "JiraCrawlRun";
	}
}
