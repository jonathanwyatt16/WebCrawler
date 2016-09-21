package webcrawler;

public interface ICrawlRun extends Runnable{

	public static final int STATUS_NOT_STARTED = 0;
	public static final int STATUS_RUNNING = 100;
	public static final int STATUS_SUCCESS = 1000;
	public static final int STATUS_SUCCESS_WITH_ERROR = 1002;
	public static final int STATUS_ERROR = 5000;

	public static final String COMMENT_NOT_STARTED = "Not started.";
	public static final String COMMENT_RUNNING = "Running.";
	public static final String COMMENT_DONE = "Done.";

	public void run();
}
