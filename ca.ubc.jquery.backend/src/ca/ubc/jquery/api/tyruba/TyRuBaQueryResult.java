package ca.ubc.jquery.api.tyruba;

import tyRuBa.tdbc.*;

import ca.ubc.jquery.api.*;

import java.util.Map;
import java.util.HashMap;

/**
 * A Result from the query. In the case of TyRuBa, it's a binding for each
 * queried variable.
 * 
 * We bind !this to ?this but make a substituion so the user never sees it.  If we request
 * !this from a query, we actually receive the value stored for ?this.  Thus we restrict our
 * queries to never contain ?this.
 * 
 * @author lmarkle
 */
public class TyRuBaQueryResult implements JQueryResult {
	private Map result;

	private Object[] values;

	/**
	 * Creates a new query result. Variables are needed in order to know how to
	 * interpret the result.
	 */
	protected TyRuBaQueryResult(ResultSet r, String[] variables) throws JQueryTyRuBaException {
		result = new HashMap();
		values = new Object[variables.length];

		try {
			for (int i = 0; i < variables.length; i++) {
				values[i] = r.getObject(variables[i]);
				result.put(variables[i], values[i]);
			}
		} catch (TyrubaException e) {
			throw new JQueryTyRuBaException(e.getMessage());
		}
	}

	/**
	 * Gets the values stored in this result frame.
	 */
	public Object[] get() {
		return values;
	}

	/**
	 * Gets the value stored in this frame for the given variable.
	 * 
	 * @param var
	 *            The name of the variable to get the value for.
	 */
	public Object get(String var) throws JQueryTyRuBaException {
		Object r = result.get(var);
		if (JQueryAPI.getThisVar().equals(var)) {
			r = result.get("?this");
		}

		if (r == null) {
			throw new JQueryTyRuBaException("No value for variable " + var);
		}

		return r;
	}

	public Object get(int var) throws JQueryException {
		if (var >= values.length || var < 0) {
			throw new JQueryTyRuBaException("Invalid variable index " + var);
		}
		return values[var];
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				buf.append(", ");
			buf.append(values[i]);
		}
		buf.append("]");
		return buf.toString();
	}
}
