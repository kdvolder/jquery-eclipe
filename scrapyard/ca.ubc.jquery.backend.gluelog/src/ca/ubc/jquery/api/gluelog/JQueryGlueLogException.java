package ca.ubc.jquery.api.gluelog;

import ca.ubc.jquery.api.JQueryException;

/**
 * A Miscellaneous Exception class
 * 
 * @author lmarkle
 */
public class JQueryGlueLogException extends JQueryException {
	protected JQueryGlueLogException(String message) {
		super(message);
	}

	protected JQueryGlueLogException(String message, Throwable e) {
		super(message, e);
	}
}
