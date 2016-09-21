package webcrawler;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import exceptions.HtmlPageOpenException;

public interface IHtmlPage {

	public void setHtmlPage(HtmlPage newPage, boolean bLoadElements, boolean bLoadCfg) throws HtmlPageOpenException;

	public HtmlPage getHtmlPage();

}
