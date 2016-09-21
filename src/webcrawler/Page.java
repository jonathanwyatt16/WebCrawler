package webcrawler;

import java.util.LinkedList;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

import app.Config;
import app.Log;
import exceptions.ConfigException;
import exceptions.HtmlElementException;
import exceptions.HtmlPageOpenException;
import util.StringUtil;

public abstract class Page implements IHtmlPage {

	private static final String PAGE_CFG_DIRECTORY = "pages";
	private static final String CFG_PAGE_NAME = "PageName";
	private static final String CFG_PAGE_URL = "PageURL";
	private static final String CFG_PAGE_JS_SECS = "PageJSWaitSecs";
	private static final String CFG_PAGE_ELEMENT_PREFIX = "PageElement.";

	protected HtmlPage m_page;
	protected LinkedList<PageElement> m_elements;
	protected Config m_cfg;
	protected String m_stCfgName;
	protected String m_stName;
	protected String m_stTitle;
	protected String m_stUrl;
	protected int m_nJsWaitSecs;

	public Page(boolean bLoadConfig) throws ConfigException {
		m_stCfgName = this.getClass().getSimpleName();
		if (bLoadConfig)
			loadPageCfg();
	}

	public void setHtmlPage(HtmlPage newPage, boolean bLoadElements, boolean bLoadCfg) throws HtmlPageOpenException {
		try {
			m_page = waitForJavaScript(newPage);
			if (bLoadElements)
				loadElements(bLoadCfg);
		} catch (HtmlElementException e) {
			Log.logThread(e);
			throw new HtmlPageOpenException("Could not set page " + newPage);
		}
	}

	public HtmlPage waitForJavaScript(HtmlPage pageToLoad) throws HtmlPageOpenException {
		JavaScriptJobManager jsManager = pageToLoad.getEnclosingWindow().getJobManager();
		try {
			int nSecs = 0;
			while (jsManager.getJobCount() > 0 && nSecs < m_nJsWaitSecs) {
				nSecs++;
				Log.logThread(
						"Waiting for JavaScript of " + m_stName + ". Second " + nSecs + " of " + m_nJsWaitSecs + ".");
				synchronized (pageToLoad) {
					pageToLoad.wait(1000);
				}
			}
		} catch (InterruptedException e) {
			Log.logThread(e);
			throw new HtmlPageOpenException("Error waiting for JavaScript of page " + pageToLoad);
		}
		return pageToLoad;
	}

	protected void loadElements(boolean bResetElementsBeforeLoading) throws HtmlElementException {
		if (bResetElementsBeforeLoading)
			try {
				loadPageCfg();
			} catch (ConfigException e) {
				Log.logThread(e);
				throw new HtmlElementException("Config exception resetting elements: " + e.getMessage());
			}

		if (m_elements == null) {
			Log.logThread("No elements to load.");
			return;
		}

		LinkedList<PageElement> alLoadedElements = new LinkedList<PageElement>();
		for (PageElement unloadedElement : m_elements) {
			for (IHtmlElement htmlElement : unloadedElement.loadElement(this)) {
				alLoadedElements.add((PageElement) htmlElement);
			}
		}

		m_elements = alLoadedElements;
	}

	public void loadPageCfg() throws ConfigException {
		m_cfg = new Config(StringUtil.getFilePath(PAGE_CFG_DIRECTORY, m_stCfgName));

		m_elements = new LinkedList<PageElement>();

		for (String stKey : m_cfg.getPropKeys()) {
			if (stKey.equals(CFG_PAGE_NAME)) {
				m_stName = m_cfg.getCfgVal(stKey);
				Log.logThread("Set page name = " + m_stName);
			}

			else if (stKey.equals(CFG_PAGE_URL)) {
				m_stUrl = m_cfg.getCfgVal(stKey);
				Log.logThread("Set URL = " + m_stUrl);
			}

			else if (stKey.equals(CFG_PAGE_JS_SECS)) {
				m_nJsWaitSecs = Integer.parseInt(m_cfg.getCfgVal(stKey));
				Log.logThread("Set JS wait seconds = " + m_nJsWaitSecs);
			}

			else if (stKey.startsWith(CFG_PAGE_ELEMENT_PREFIX)) {
				String stElementName = stKey.replaceAll(CFG_PAGE_ELEMENT_PREFIX, "");
				String stElementPath = m_cfg.getCfgVal(stKey);
				m_elements.add(new PageElement(stElementName, stElementPath));
				Log.logThread("Set element " + stElementName + " = " + stElementPath);
			}
		}
	}

	public String getPageCfgVal(String stCfgName) throws ConfigException {
		return m_cfg.getCfgVal(stCfgName);
	}

	public String getName() {
		return m_stName;
	}

	public String getTitle() {
		return m_stTitle;
	}

	public String getURL() {
		return m_stUrl;
	}

	public HtmlPage getHtmlPage() {
		return m_page;
	}

	public String toString() {
		return getName();
	}
}
