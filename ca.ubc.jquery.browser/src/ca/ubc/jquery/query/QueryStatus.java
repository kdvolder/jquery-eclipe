package ca.ubc.jquery.query;

import java.util.Date;

/**
 * Represents the status of a query including statistics about the
 * execution of the query.  All the timing fields (<code>startTime</code>,
 * <code>endTime</code> and <code>executionTime</code>) are read-only.  
 * Their values are updated automatically when setStatus() is called.
 */
public class QueryStatus {

	/** STATUS: The query has not yet been started. */
	public static final int INIT	= 1;
	
	/** STATUS: The query is currently executing. */
	public static final int RUNNING	= 2;
	
	/** STATUS: The query was aborted before it was finished. */
	public static final int ABORTED	= 3;
	
	/** STATUS: The query finished naturally, because there were no more results. */
	public static final int DONE	= 4;
	
	/** The status of the query (value must be one of the 
	 * defined status codes). */
	private int status = INIT;

	/** The start time of the query. */
	private Date startTime = null;
		
	/** The end time of the query. */
	private Date endTime = null;

	/** The total time it took to execute the query. */
	private long executionTime = 0;
	
	/** The number of nodes in the query result. */
	private int numNodes = 0;
		
	/** The number of results in the query result. */
	private int numResults = 0;
	
	/** A short description of why the query finished. */
	private String exitReason = null;
	
	/** An object whoes toString() method provides a detailed 
	 * description of why the query finished.
	 */
	private Throwable exitDetails = null;
	
	/**
	 * A convenience method for aborting queries.  Sets the
	 * exitReason and exitDetails and changes status to ABORTED.
	 */
	public synchronized void abort(String reason, Throwable details) {
		exitReason = reason;
		exitDetails = details;
		setStatus(ABORTED);
	}
	
	/**
	 * Gets the endTime.
	 * @return Returns a Date
	 */
	public synchronized Date getEndTime() {
		return endTime;
	}

	/**
	 * Gets the numNodes.
	 * @return Returns a int
	 */
	public synchronized int getNumNodes() {
		return numNodes;
	}

	/**
	 * Gets the number of results.
	 * @return Returns a int
	 */
	public synchronized int getNumResults() {
		return numResults;
	}

//	/**
//	 * Sets the numNodes.
//	 * @param numNodes The numNodes to set
//	 */
//	public synchronized void setNumNodes(int numNodes) {
//		this.numNodes = numNodes;
//	}

	/**
	 * Increments the number of nodes by one.
	 */
	public synchronized void incrementNumNodes() {
		this.numNodes++;
	}

	/**
	 * Increments the number of results by one.
	 */
	public synchronized void incrementNumResults() {
		this.numResults++;
	}

	/**
	 * Gets the startTime.
	 * @return Returns a Date
	 */
	public synchronized Date getStartTime() {
		return startTime;
	}

	/**
	 * Gets the status.
	 * @return Returns a int
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * @param status The status to set
	 */
	public synchronized void setStatus(int status) {
		this.status = status;
		switch (status) {
			
			case INIT:
				
				numNodes = 0;
				numResults = 0 ;
				startTime = null;
				endTime = null;		
				executionTime = 0;
				exitReason = null;
				exitDetails = null;
				break;
				
			case RUNNING:
			
				numNodes = 0;
				numResults = 0 ;
				startTime = new Date();
				endTime = null;
				executionTime = 0;
				exitReason = null;
				exitDetails = null;
				break;
			
			case ABORTED:
			case DONE:
				endTime = new Date();
				if (startTime != null) 						
					executionTime = endTime.getTime() - startTime.getTime();

				else
					executionTime = 0;
				break;
		}
	}

	/**
	 * Gets the executionTime.
	 * @return Returns a long
	 */
	public synchronized long getExecutionTime() {
		return executionTime;
	}


	/**
	 * Gets the exitDetails.
	 * @return Returns a Object
	 */
	public synchronized Object getExitDetails() {
		return exitDetails;
	}

	/**
	 * Sets the exitDetails.
	 * @param exitDetails The exitDetails to set
	 */
	public synchronized void setExitDetails(Exception exitDetails) {
		this.exitDetails = exitDetails;
	}

	/**
	 * Gets the exitReason.
	 * @return Returns a String
	 */
	public synchronized String getExitReason() {
		return exitReason;
	}

	/**
	 * Sets the exitReason.
	 * @param exitReason The exitReason to set
	 */
	public synchronized void setExitReason(String exitReason) {
		this.exitReason = exitReason;
	}

}
