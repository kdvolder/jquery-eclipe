package ca.ubc.jquery.api;

/**
 * Class to act as an iterator for query results. This allows results to be generated only when needed and should allow a user more freedom when looking at query results.
 * 
 * @author lmarkle
 */
public interface JQueryResultSet {
	/**
	 * @return true if there are more results
	 */
	public abstract boolean hasNext();

	/**
	 * returns the next result
	 */
	public abstract JQueryResult next() throws JQueryException;

	/**
	 * Nice code should always make sure to either read the
	 * resultSet to the end OR call this method. If ResultSets
	 * are left "dangling" in the middle this may create
	 * performance issues and deadlocks because database
	 * cursors/locks are not being properly released.
	 */
	public abstract void close();
}
