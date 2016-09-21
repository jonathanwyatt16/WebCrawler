package server;

import javax.servlet.http.HttpServletResponse;

import exceptions.HttpResponseException;

public interface IHttpCallResponder {
	public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
	public static final String CONTENT_TYPE_TEXT = "text/html;charset=UTF-8";
	public static final String REQUEST_IDENTIFIER = "request";
	
	public void respond(HttpServletResponse response) throws HttpResponseException;
}
