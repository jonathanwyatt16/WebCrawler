package exceptions;

@SuppressWarnings("serial")
public class IncompleteDataException extends CrawlException {

	public IncompleteDataException(String stMessage) {
		super(stMessage);
	}

}
