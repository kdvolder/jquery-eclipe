package ca.ubc.jquery.api.tyruba;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;

import tyRuBa.modes.Type;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.TyrubaException;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * A precompiled query which can be executed.
 * 
 * NOTE: bind(String,Object[]) is poorly implemented and effects this whole class. Ideally 
 * we could bind to a list of objects, this is not possible so instead we generate a query 
 * for each object, bind them and execute them and combine the results. It's sloppy and 
 * works but isn't ideal. This also spills some mess into TyRuBaQueryResultSet.
 * 
 * @author lmarkle
 */
public class TyRuBaQuery extends JQuery {
	public static final long serialVersionUID = 1L;

	private transient Type objectType;

	private transient PreparedQuery query;

	/**
	 * Constructs a query
	 */
	protected TyRuBaQuery(String queryString) throws JQueryException {
		super(queryString);
		query = null;

		try {
			objectType = JQueryTyRuBaAPI.getTyRuBaConnection().findType("Object");
		} catch (TyrubaException e) {
			throw new JQueryTyRuBaException("Creating Query: ", e);
		}
	}

	protected void bindVariables() throws JQueryException {
		//
		// Okay... the logic in this method is INSANELY complex so try not to mess with it
		// too much unless you plan on rewritting it.
		//
		// We needed a way to bind ? variables as well as ! variables for two reasons:
		//	1) for more advanced filtering mechanisms
		//	2) for the JQueryResultGraph structure
		//
		// reallyBoundVars represents the variables we will actually call query.put() with
		// so we first getBoundVars() and make any appropriate renamings according to the
		// logic below.
		//
		// The logic is:
		//		if we are binding the variable to an array:
		//			1) if the variable is a !variable:
		//				replace the old !variable with a ?variable and use the member/2 
		//				function and bind the it something like: member(?var,!var)
		//			2) if the variable is a ?variable:
		//				replace with a !variable and add to the really bound vars
		//		else:
		//			1) if  the variable is the !this var, replace it with ?this and bind
		//				a !this var in an equals clause
		//			2) if the variable is a !variable: put it in the really bound vars
		//			3) else: add an equals(?var,!var) clause to the query and bind ?var to !var
		//
		// Lastly, YES, this method makes changes the getBoundVars() HashMap during the
		// iteration.  It's not good, I wish it could change, but I really don't want to 
		// make this loop any more complicated than it already is.
		//
		// Feel free to make changes.
		Map reallyBoundVariables = new HashMap();
		for (Iterator it = getBoundVars().entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			Object value = e.getValue();

			if (value instanceof Object[]) {
				String var = (String) e.getKey();
				String oldVar = var;

				if (var.charAt(0) == '!') {
					var = "?" + var.substring(1);
					reallyBoundVariables.put(oldVar, value);
					replaceVariable(oldVar, var);
				} else if (var.charAt(0) == '?') {
					oldVar = "!" + oldVar.substring(1);
					reallyBoundVariables.put(oldVar, value);
				}

				queryString = "(" + queryString + "),(member(" + var + "," + oldVar + "))";
			} else {
				String var = (String) e.getKey();
				if (JQueryAPI.getThisVar().equals(var)) {
					replaceVariable(JQueryAPI.getThisVar(), "?this");
					queryString = "(" + queryString + "),equals(?this,!this)";
					reallyBoundVariables.put("!this", value);
				} else if (var.charAt(0) == '!') {
					reallyBoundVariables.put(var, value);
				} else {
					String newVar = "!" + var.substring(1);
					queryString = "(" + queryString + "),equals(" + var + "," + newVar + ")";
					reallyBoundVariables.put(newVar, value);
				}
			}
		}

		setString(queryString);
		query = getQuery();

		for (Iterator it = reallyBoundVariables.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			try {
				query.put((String) e.getKey(), e.getValue(), objectType);
			} catch (TyrubaException ex) {
				throw new JQueryTyRuBaException(ex.getMessage());
			}
		}
	}

	@Override
	public void setString(String query) throws JQueryException {
		this.query = null;
		super.setString(query);
	}

	/**
	 * Executes the query and returns the results for a given variable ordering.
	 */
	protected JQueryResultSet execute(String vars[]) throws JQueryException {
		TyRuBaQueryResultSet result = null;

		try {
			Job.getJobManager().beginRule(JQueryAPI.getRule(), null);
			if (query == null) {
				query = getQuery();
			}

			result = new TyRuBaQueryResultSet(query.executeQuery(), vars);
		} catch (TyrubaException e) {
			throw new JQueryTyRuBaException("Executing query: " + getString() + ": " + e.getMessage());
		} catch (JQueryException e) {
			throw new JQueryTyRuBaException("Executing query: " + getString() + ": " + e.getMessage());
		} finally {
			Job.getJobManager().endRule(JQueryAPI.getRule());
		}

		return result;
	}

	/**
	 * Compiles the query
	 */
	private PreparedQuery getQuery() throws JQueryTyRuBaException {
		if (queryString == null || ("").equals(queryString)) {
			throw new JQueryTyRuBaException("Can't prepare a blank query");
		} else {
			try {
				// FIXME Add type checking again after the system is working better
				PreparedQuery q = JQueryTyRuBaAPI.getTyRuBaConnection().prepareNoTypeCheckQuery(queryString);
				return q;
			} catch (TyrubaException e) {
				throw new JQueryTyRuBaException("Problem preparing query: " + queryString + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Returns the set of all variables found in the query.
	 */
	public String[] getVariables() throws JQueryException {
		String[] result = null;

		if (query == null) {
			query = getQuery();
		}
		result = query.getOutputVariables();

		return result;
	}

	private void replaceVariable(String var, String newVar) throws JQueryException {
		if (var.charAt(0) == '?') {
			queryString = queryString.replaceAll("\\" + var, newVar);
		} else {
			queryString = queryString.replaceAll(var, newVar);
		}
		query = null;

		// replace bindings
		Object value = getBoundVars().get(var);
		if (value != null) {
			getBoundVars().remove(var);
			getBoundVars().put(newVar, value);
		}
	}

	private void appendListRef(int count, String var, String newVar) throws JQueryException {
		//		queryString = "(list_ref(" + count + "," + var + "," + newVar + ")," + queryString + ")";
		queryString = "(member(" + newVar + "," + var + ")," + queryString + ")";
		query = null;
	}

	@Override
	protected JQuery applyFilter(JQuery f, String var, int position) throws JQueryException {
		if (!(f instanceof TyRuBaQuery)) {
			throw new JQueryTyRuBaException("Cannot mix query types when applying filters: Given " + f.getClass() + ", expecting " + TyRuBaQuery.class);
		}

		TyRuBaQuery filter = (TyRuBaQuery) f.clone();
		StringBuilder b = new StringBuilder("(" + queryString + ")");

		// append filter to query
		// rename variables to avoid name conflicts
		String[] vars = filter.getVariables();
		for (int i = 0; i < vars.length; i++) {
			String newName = createVariable(filter);
			filter.replaceVariable(vars[i], newName);
		}

		//				if (node.isSourceList()) {
		if (position < 0) {
			filter.replaceVariable(JQueryAPI.getThisVar(), var);
		} else {
			String thisVar = createVariable(filter);
			filter.appendListRef(position, var, thisVar);
			filter.replaceVariable(JQueryAPI.getThisVar(), thisVar);
		}

		b.append(",(" + filter.getString() + ")");

		JQuery result = new TyRuBaQuery(b.toString());
		for (Iterator it = getBoundVars().entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			result.bind((String) e.getKey(), e.getValue());
		}
		for (Iterator it = filter.getBoundVars().entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			result.bind((String) e.getKey(), e.getValue());
		}

		result.setChosenVars(chosenVars);
		return result;
	}

	private String createVariable(JQuery other) throws JQueryException {
		String var = "?V";
		String vars[] = getVariables();
		String otherVars[] = other.getVariables();
		boolean contains = true;

		for (int j = 0; contains; j++) {
			contains = false;
			var = "?V" + j;
			for (int i = 0; i < vars.length; i++) {
				if (var.equals(vars[i])) {
					contains = true;
				}
			}

			for (int i = 0; i < otherVars.length; i++) {
				if (var.equals(otherVars[i])) {
					contains = true;
				}
			}
		}
		return var;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		query = null;

		try {
			objectType = JQueryTyRuBaAPI.getTyRuBaConnection().findType("Object");
		} catch (TyrubaException e) {
			JQueryBackendPlugin.error("Creating Query: ", e);
		}
	}
}
