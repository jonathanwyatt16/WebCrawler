package exceptions;

@SuppressWarnings("serial")
public class NonexistentPageException extends HtmlPageOpenException {

	public NonexistentPageException(String stMessage) {
		super(stMessage);
	}
}
