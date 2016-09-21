package webcrawler;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import app.Log;
import exceptions.HtmlPageOpenException;

public abstract class WebBrowser {

	protected WebClient m_client;
	protected Page m_currentPage;

	protected WebBrowser(Page firstPage) throws HtmlPageOpenException {
		initializeClient();
		setPage(firstPage, true);
	}

	protected abstract void initializeClient();

	public void logIn() throws LoginException {
		if (m_currentPage instanceof LoginPage) {
			((LoginPage) m_currentPage).login();
		} else {
			throw new LoginException("Current page " + m_currentPage.getName() + " is not a login page.");
		}
	}

	public void setPage(Page newPage, boolean bOpenURL) throws HtmlPageOpenException {
		m_currentPage = newPage;

		if (bOpenURL) {
			try {
				Log.logThread("Opening URL " + m_currentPage.getURL());
				m_currentPage.setHtmlPage((HtmlPage) m_client.getPage(m_currentPage.getURL()), true, false);
			} catch (FailingHttpStatusCodeException | IOException e) {
				Log.logThread(e);
				throw new HtmlPageOpenException("Browser could not open page " + m_currentPage);
			}
		}
	}

	@Override
	protected void finalize() {
		m_client.close();
		m_client = null;
		m_currentPage = null;
	}

	public Page getCurrentPage() {
		return m_currentPage;
	}
}
