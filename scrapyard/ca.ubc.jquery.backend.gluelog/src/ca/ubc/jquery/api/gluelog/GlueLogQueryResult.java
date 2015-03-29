package ca.ubc.jquery.api.gluelog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.ubc.gluelog.data.GLList;
import ca.ubc.gluelog.values.TupleValue;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;

/**
 * A Result from the query.
 * 
 * @author lmarkle
 */
public class GlueLogQueryResult implements JQueryResult {

	private Object[] values;
	
	private Map<String,Object> varsToResults;
	
	protected GlueLogQueryResult(String[] vars, Object rawResult) throws JQueryException {
		
		// FIXME actually, gluelog should return tuples of each result instead of GLList
		// Need to fix that in gluelog, and then we won't have problem here of distinguishing
		// between list of values and value consisting of a list
		
		// TODO for now, the rawResult should always be Object array from TupleValue
		if (rawResult instanceof Object[]) {
			try {
				values = (Object[])rawResult;
			} catch (ClassCastException e) {
				// apparantely, the result was not as we expected
				throw new JQueryGlueLogException("maybe tuple value didn't return an array?");
			}
		} else {
			throw new JQueryGlueLogException("raw result not a tuple: " + rawResult + " is instead a " + rawResult.getClass());
		}
		
//		if (rawResult instanceof Object[]) {
//			values = (Object[])rawResult;
//		} else if (rawResult instanceof GLList) {
//			values = ((GLList)rawResult).toJavaList().toArray(new Object[0]);
//		} else {
//			values = new Object[]{rawResult};
//		}
		if (vars.length != values.length) {
			// something inconsistent
			throw new JQueryGlueLogException("number of wanted variables not the same as number of returned values for each result");
		}
		Map<String,Object> varsToResults = new HashMap();
		for (int i = 0; i < vars.length; i++) {
			varsToResults.put(vars[i], values[i]);
		}
		this.varsToResults = varsToResults;
	}
	
//	protected GlueLogQueryResult(Map result) {
//		Set temp = result.entrySet();
//		int i = 0;
//
//		this.result = result;
//		this.values = new Object[temp.size()];
//
//		for (Iterator it = temp.iterator(); it.hasNext(); i++) {
//			values[i] = ((Map.Entry) it.next()).getValue();
//
//			// JTransformer returns vectors as lists rather than Object[] which
//			// is what we need...
//			if (values[i] instanceof Vector) {
//				values[i] = ((Vector) values[i]).toArray();
//			}
//		}
//	}
//
//	protected GlueLogQueryResult(Map result, String[] vars) {
//		this.result = result;
//		this.values = new Object[vars.length];
//
//		for (int i = 0; i < vars.length; i++) {
//			values[i] = result.get(vars[i]);
//
//			// JTransformer returns vectors as lists rather than Object[] which
//			// is what we need...
//			if (values[i] instanceof Vector) {
//				values[i] = ((Vector) values[i]).toArray();
//			}
//		}
//	}

	/**
	 * Gets the values stored in this result frame.
	 */
	public Object[] get() {
		return values;
	}

	public Object get(int var) throws JQueryException {
		if (var >= values.length || var < 0) {
			throw new JQueryGlueLogException("Invalid variable index " + var);
		}
		return values[var];
	}

	/**
	 * Gets the value stored in this frame for the given variable.
	 * 
	 * @param var
	 *            The name of the variable to get the value for.
	 */
	public Object get(String var) throws JQueryException {
		Object r = varsToResults.get(var);

		if (r == null) {
			throw new JQueryGlueLogException("No value for variable: " + var);
		}
		return r;
	}
}
