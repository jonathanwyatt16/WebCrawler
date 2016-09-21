package webcrawler;

import java.io.IOException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import app.Log;
import exceptions.HtmlElementException;
import exceptions.HtmlPageOpenException;

public class PageElement implements IHtmlElement {
	protected String m_stName;
	protected String m_stXPath;
	protected String m_stText;
	protected DomElement m_element;

	public PageElement(String stElementName, String stXPath) {
		this(stElementName, stXPath, null, null);
	}

	private PageElement(String stElementName, String stXPath, String stText, DomElement element) {
		m_stName = stElementName;
		m_stXPath = stXPath;
		m_stText = stText;
		m_element = element;
	}

	public IHtmlElement[] loadElement(IHtmlPage page) {
		ArrayList<PageElement> alInitializedElements = new ArrayList<PageElement>();

		try {
			for (Object objElem : page.getHtmlPage().getByXPath(m_stXPath)) {
				DomElement domElem = (DomElement) objElem;
				PageElement newElement = new PageElement(m_stName, m_stXPath, domElem.asText(), domElem);
				alInitializedElements.add(newElement);
			}
		} catch (Exception e) {
			Log.logThread("Exception while loading element " + m_stName);
			Log.logThread(e);
		}

		Log.logThread(
				"Found " + alInitializedElements.size() + " element(s) for " + m_stName + ". xPath = " + m_stXPath);
		return alInitializedElements.toArray(new PageElement[0]);
	}

	public void setInputVal(String inputText) throws HtmlElementException {
		Log.logThread("Setting " + m_stName + " = " + inputText);

		if (m_element instanceof HtmlInput)
			((HtmlInput) m_element).setValueAttribute(inputText);

		else if (m_element instanceof HtmlTextArea)
			((HtmlTextArea) m_element).setTextContent(inputText);

		else
			throw new HtmlElementException("Element is the wrong type. Is type " + m_element.getClass());
	}

	public HtmlPage clickElement() throws HtmlPageOpenException {
		try {
			Log.logThread("Clicking on " + m_stName);
			return m_element.click();
		} catch (IOException e) {
			Log.logThread(e);
			throw new HtmlPageOpenException("Error while clicking on " + m_stName);
		}
	}

	public HtmlPage hitEnter() throws HtmlElementException, HtmlPageOpenException {
		try {
			Log.logThread("Hitting enter on " + m_stName);
			char newLine = '\n';
			if (m_element instanceof HtmlTextArea)
				return (HtmlPage) ((HtmlTextArea) m_element).type(newLine);

			else if (m_element instanceof HtmlTextInput)
				return (HtmlPage) ((HtmlTextInput) m_element).type(newLine);

			else
				throw new HtmlElementException("Element is the wrong type. Is type " + m_element.getClass());

		} catch (IOException e) {
			Log.logThread(e);
			throw new HtmlPageOpenException("Error while hitting enter in " + m_stName);
		}
	}

	public String getURL() {
		return ((HtmlAnchor) m_element).getHrefAttribute();
	}

	public String getId() {
		return m_element.getAttribute("id");
	}

	public String getTitle() {
		return m_element.getAttribute("title");
	}

	public String getName() {
		return m_stName;
	}

	public String getText() {
		return m_stText;
	}

	public String toString() {
		return m_stName;
	}

}
