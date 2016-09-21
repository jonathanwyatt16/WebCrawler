package exceptions;

import org.apache.http.HttpException;

@SuppressWarnings("serial")
public class HttpResponseException extends HttpException {
	public HttpResponseException(String stMessage) {
		super(stMessage);
	}
}
