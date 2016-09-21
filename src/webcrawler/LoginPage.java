package webcrawler;

import javax.security.auth.login.LoginException;

import exceptions.ConfigException;

public abstract class LoginPage extends Page {

	public static final String WEB_USER = "WebUser";
	public static final String WEB_PASS = "WebPass";

	protected String m_stUser, m_stPass;

	public LoginPage() throws ConfigException {
		super(true);
		m_stUser = m_cfg.getCfgVal(WEB_USER);
		m_stPass = m_cfg.getCfgVal(WEB_PASS);
	}

	public abstract void login() throws LoginException;
}
