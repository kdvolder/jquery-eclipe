package ca.ubc.jquery.api;

/**
 * A Result from the query. 
 *  
 * @author lmarkle
 */
public interface JQueryResult {
	/**
	 * Gets the values stored in this result frame.
	 */
	public abstract Object[] get();

	/**
	 * Gets the value stored in this frame for the given variable.
	 * 
	 * @param var
	 *            The name of the variable to get the value for.
	 */
	public abstract Object get(String var) throws JQueryException;
	
	/**
	 * Gets the value stored in this frame for the number variable.
	 * Variable numberings start at 0...n-1 where there are n variables
	 * in this query result.  
	 * 
	 * Basically you use this function as if you were accessing the results
	 * from an array.
	 * 
	 * @param var
	 * 			The variable you wish to read
	 */
	public abstract Object get(int var) throws JQueryException;
}
