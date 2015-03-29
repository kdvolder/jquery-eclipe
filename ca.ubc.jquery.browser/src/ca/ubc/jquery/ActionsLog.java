package ca.ubc.jquery;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ca.ubc.jquery.api.JQuery;

/**
 * @author wannop
 */

public class ActionsLog {
		
	private static PrintWriter pw;

	private static boolean logging = false;

	/** true if we want to append to end of previous log */
	static boolean append = true;

	

	/**
	 * Method initLog.
	 */
	protected static void initLog() {
		try {
	    	pw = new PrintWriter(new FileWriter("JQueryActions.log", append));
			pw.println();
			pw.println();
			pw.println("*****New JQuery Session started*****");
			logging = true;
		}
		
		catch(IOException e) {		
		 	System.out.println("Unable to open actions.log!");
		}
	}
	
	/**
	 * Method logExecute.
	 * logs query attempts executed by user.  Query results are to be reported 
	 * separately using the logResults method.
	 * @param query 
	 * @param variables
	 * @param status
	 */
	public static void logExecute (JQuery query) {
		if (logging) {
			pw.println("Query attempted:");
			pw.println("\tquery: " + query.getString());
			//			pw.println("\tvariables: " + query.getChosenVars());
			pw.println();
		}
	}
	
	/**
	 * Method logResult.
	 * logs the results of each query attempt.  Currently called by QueryControl's queryDone method.
	 * @param exitStatus
	 * @param resultCount
	 * @param exitReason
	 */
	public static void logResult(String exitStatus, int resultCount, String exitReason) {
		if (logging) {
			pw.println("Query stopped:");
			pw.println("\tExit Status: " + exitStatus);
			pw.println("\tNumber of results: " + resultCount);
			if (exitReason.length() > 0) {
				pw.println("\tExit reason: " + exitReason);
			}
			pw.println();
		}
	}

	/**
	 * Method logGeneric.
	 * @param string
	 */
	public static void logGeneric(String actionType) {
		if (logging) {
			pw.println(actionType);
			pw.println();
		}
	}
	
	
	/**
	 * Method logGeneric.
	 * @param actionType
	 * @param detail
	 */
	public static void logGeneric(String actionType, String detail) {
		if (logging) {
			pw.println(actionType + ": ");
			if (detail != null) {
				pw.println("\t"+detail);
			}
			pw.println();
		}
	}

	/**
	 * Method logGeneric.  Similar to logGeneric(String, String), but accepts
	 * an array of detail strings.
	 * @param actionType: describes the type of action being logged
	 * @param details: array of any details to be logged
	 */
	public static void logGeneric(String actionType, String [] details) {
		if (logging) {
			pw.println(actionType + ": ");
			if (details != null) {
				for(int i= 0; i< details.length; i++) {
					if (details[i] != null) {
						pw.println("\t"+details[i]);
					}
				}
			}
			pw.println();
		}
	}
			
	
	/**
	 * Method closeLog.
	 * this method must be called before or when JQuery is closed.
	 */
	protected static void closeLog() {
			if (logging) {
				pw.close();
				pw=null;
			}
	}
} 