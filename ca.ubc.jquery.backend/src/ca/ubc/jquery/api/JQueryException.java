package ca.ubc.jquery.api;

/**
 * A Miscellaneous Exception class
 * 
 * @author lmarkle
 */
public class JQueryException extends Exception {
	protected JQueryException(String message) {
		super(message);
	}

	protected JQueryException(String message, Throwable e) {
		super(message,e);
	}
}
