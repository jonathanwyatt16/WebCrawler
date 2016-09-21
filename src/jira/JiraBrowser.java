package jira;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import exceptions.ConfigException;
import exceptions.HtmlPageOpenException;
import webcrawler.WebBrowser;

public class JiraBrowser extends WebBrowser {

	private static final int WAIT_FOR_JS = 10000;

	protected JiraBrowser() throws HtmlPageOpenException, ConfigException {
		super(new JiraLoginPage());
	}

	@Override
	protected void initializeClient() {
		m_client = new WebClient(BrowserVersion.FIREFOX_38);
		m_client.getOptions().setRedirectEnabled(true);
		m_client.getOptions().setJavaScriptEnabled(true);
		m_client.getOptions().setThrowExceptionOnScriptError(false);
		m_client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		m_client.getOptions().setUseInsecureSSL(true);
		m_client.getOptions().setCssEnabled(true);
		m_client.getOptions().setAppletEnabled(true);
		m_client.waitForBackgroundJavaScript(WAIT_FOR_JS);
	}
}
