package jira;

import javax.security.auth.login.LoginException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import app.Log;
import exceptions.ConfigException;
import exceptions.HtmlElementException;
import exceptions.HtmlPageOpenException;
import webcrawler.LoginPage;
import webcrawler.PageElement;

public class JiraLoginPage extends LoginPage {

	public JiraLoginPage() throws ConfigException {
		super();
	}

	@Override
	public void login() throws LoginException {
		try {
			int nIdxElem = 0;
			m_elements.get(nIdxElem++).setInputVal(m_stUser);
			m_elements.get(nIdxElem++).setInputVal(m_stPass);
			m_elements.get(nIdxElem++).clickElement();

			setHtmlPage(m_elements.get(nIdxElem).clickElement(), false, false);

		} catch (FailingHttpStatusCodeException | HtmlPageOpenException | HtmlElementException e) {
			Log.logThread(e);
			throw new LoginException(
					"Login failed for " + m_stName + " with username " + m_stUser + " and password " + m_stPass);
		}
	}

	public HtmlPage searchForTicket(String stTicketId) throws HtmlElementException, HtmlPageOpenException {
		PageElement quickSearch = m_elements.get(m_elements.size() - 1);
		quickSearch.setInputVal(stTicketId);
		HtmlPage ticketPage = quickSearch.hitEnter();

		return ticketPage;
	}
}
