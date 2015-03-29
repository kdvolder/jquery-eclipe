package ca.ubc.jquery.api.gluelog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.ubc.gluelog.ast.Expression;
import ca.ubc.gluelog.ast.Term;
import ca.ubc.gluelog.compiled.CompExp;
import ca.ubc.gluelog.compiled.CompFactory;
import ca.ubc.gluelog.compiler.GLCompiler;
import ca.ubc.gluelog.parser.ParseException;
import ca.ubc.gluelog.parser.Parser;
import ca.ubc.gluelog.runtime.GLRuntime;
import ca.ubc.gluelog.values.ChoiceValue;
import ca.ubc.gluelog.values.Value;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * A GlueLogQuery is actually an uncompiled query which must be 
 * compiled, when result variables are specified, and the compiled query 
 * should be run when executed.
 * 
 * @author lmarkle
 * 
 */
public class GlueLogQuery extends JQuery {
	public static final long serialVersionUID = 1L;

	private transient String[] queryVars;

//	private transient PrologSession query;

	private String originalQuery;
	
	private GlueLogFactBase env;

	protected GlueLogQuery(String query, GlueLogFactBase env) {
		super(query);
		this.env = env;
		originalQuery = query;

		queryVars = null;
	}

	@Override
	public void setString(String query) throws JQueryException {
		originalQuery = query;
		queryVars = null;
		super.setString(query);
	}

	/**
	 * Executes the query and returns the results for a given variable ordering.
	 */
	protected JQueryResultSet execute(String vars[]) throws JQueryException {
//		try {
//			getQuery();
//			// return new Testing(queryString, vars);
//
//			List q = query.queryAll(queryString);
//			if (q == null) {
//				return new JTransformerQueryResultSet(query);
//			} else {
//				return new JTransformerQueryResultSet(query, q, vars);
//			}
//		} catch (PrologInterfaceException e) {
//			throw new JQueryGlueLogException(e.getMessage());
//		} finally {
//			if (query != null && !query.isDisposed()) {
//				query.dispose();
//				query = null;
//			}
//		}
		
		// run the query in the factbase env
//		String wantedResult = "";
//		if (vars.length > 1) {
//			wantedResult += "[" + vars[0];
//			for (int i = 1; i < vars.length; i++) {
//				wantedResult += "," + vars[i];
//			}
//			wantedResult += "]";
//		} else if (vars.length > 0) {
//			wantedResult = vars[0];
//		}

		GLCompiler compiler = new GLCompiler();
		try {
			CompExp compiledQuery = compiler.compileQuery(env.getLookupEnv(), vars, originalQuery);
			System.out.println(compiledQuery);
			Value result = env.getRuntime().run(compiledQuery);
			System.out.println(result);
			GlueLogQueryResultSet resultSet = new GlueLogQueryResultSet(vars, result);
			return resultSet;
		} catch (ParseException e) {
			throw new JQueryGlueLogException("Problem parsing query: " + e.getMessage());
		}
	}

	/**
	 * Returns the set of all variables found in the query.
	 */
	public String[] getVariables() throws JQueryException {
		GLCompiler compiler = new GLCompiler();
		try {
			Parser parser = new Parser(new StringReader(originalQuery));
			Expression exp = parser.ExpressionAndEOF();
			List<Term> allArgs = exp.allArgs();
			List<String> vars = new ArrayList<String>();
			for (Term arg : allArgs) {
				vars.addAll(arg.collectVarSymbols());
			}
			return vars.toArray(new String[vars.size()]);
		} catch (ParseException e) {
			throw new JQueryGlueLogException("problem getting variables: " + e.getMessage());
		}
		
//		if (queryVars == null) {
//			try {
//				// remove any strings from the list
//				String q = queryString.replaceAll("'.*'", "_");
//				if (q.contains("'")) {
//					throw new JQueryGlueLogException("Unmatched '");
//				} else {
//					getQuery();
//					Map temp = query.queryOnce("getVariables('" + q + "',Vars)");
//					JQueryResult r = new JTransformerQueryResult(temp);
//
//					Object[] t = (Object[]) r.get("Vars");
//					queryVars = chooseVars(t);
//				}
//			} catch (PrologInterfaceException e) {
//				throw new JQueryGlueLogException("JTransformerQuery.getVariables(): " + e.getMessage());
//			} finally {
//				if (query != null && !query.isDisposed()) {
//					query.dispose();
//					query = null;
//				}
//			}
//		}

		// FIXME testing
//		String[] vars = new String[9];
//		vars[0] = "?x";
//		vars[1] = "?y";
//		vars[2] = "?z";
//		vars[3] = "T";
//		vars[4] = "E";
//		vars[5] = "S";
//		vars[6] = "L";
//		vars[7] = "F";
//		vars[8] = "R";
//		return vars;
	}

	private String[] chooseVars(Object[] t) {
		List temp = new ArrayList();
		int i, j;

		for (i = 0, j = 0; i < t.length; i++) {
			String name = (String) t[i];
			name = name.substring(name.indexOf("=('") + 3, name.indexOf("',"));
			if (name.charAt(0) != '_') {
				// TODO: Remove this hack by implementing this in the prolog rule
				temp.add(name);
				j += 1;
			}
		}

		temp.remove(JQueryAPI.getThisVar());

		String[] result = new String[temp.size()];
		i = 0;

		for (Iterator it = temp.iterator(); it.hasNext(); i++) {
			result[i] = (String) it.next();
		}

		return result;
	}

	//	protected void bindVariables() throws JQueryException {
	//		for (Iterator it = getBoundVars().entrySet().iterator(); it.hasNext();) {
	//			Map.Entry e = (Map.Entry) it.next();
	//			if (e.getValue() instanceof Object[]) {
	//				bindVariable((String) e.getKey(), (Object[]) e.getValue());
	//			} else {
	//				bindVariable((String) e.getKey(), e.getValue());
	//			}
	//		}
	//	}

	/**
	 * Binds a variable to a value
	 */
	protected void bindVariables() throws JQueryGlueLogException {
		StringBuilder b = new StringBuilder();

		b.append("(");
		for (Iterator it = getBoundVars().entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			Object[] v;
			if (e.getValue() instanceof Object[]) {
				v = (Object[]) e.getValue();
			} else {
				v = new Object[] { e.getValue() };
			}
			String var = (String) e.getKey();

			for (int i = 0; i < v.length; i++) {
				b.append(var);
				b.append("=");
				if (v[i] instanceof String) {
					try {
						Integer.parseInt((String) v[i]);
						b.append(v[i]);
					} catch (NumberFormatException ex) {
						b.append("'" + v[i] + "'");
					}
				} else {
					b.append(v[i]);
				}

				if (i < v.length - 1) {
					b.append(";");
				}
			}
			b.append("),(");
		}
		b.append(originalQuery);
		b.append(")");

		queryString = b.toString();
	}

//	private void getQuery() throws JQueryException {
//		if (queryString == null) {
//			return;
//		} else {
//			query = JQueryGlueLogAPI.getPrologConnection();
//		}
//	}

	private void replaceVariable(String var, String newVar) throws JQueryException {
		// TODO this is ugly but without it we can't string replace variable names properly
		// I think ideally we would have prolog handle this kind of thing but for now,
		// because I'm lazy, it will work (and may indeed last for years to come...)
		// January 17th, 2008
		queryString = queryString.replaceAll("\\(\\p{Space}*" + var + "\\p{Space}*\\)", "(" + newVar + ")");
		queryString = queryString.replaceAll("\\(\\p{Space}*" + var + "\\p{Space}*,", "(" + newVar + ",");
		queryString = queryString.replaceAll(",\\p{Space}*" + var + "\\p{Space}*\\)", "," + newVar + ")");
		queryString = queryString.replaceAll(",\\p{Space}*" + var + "\\p{Space}*,", "," + newVar + ",");

		Object value = getBoundVars().get(var);
		if (value != null) {
			getBoundVars().remove(var);
			getBoundVars().put(newVar, value);
		}

		queryVars = null;
	}

	private void appendListRef(int count, String var, String newVar) throws JQueryException {
		//		queryString = "(listRef(" + count + "," + var + "," + newVar + ")," + queryString + ")";
		queryString = "(member(" + newVar + "," + var + ")," + queryString + ")";
	}

	@Override
	protected JQuery applyFilter(JQuery f, String var, int position) throws JQueryException {
		if (!(f instanceof GlueLogQuery)) {
			throw new JQueryGlueLogException("Cannot mix query types when applying filters: Given " + f.getClass() + ", expecting " + GlueLogQuery.class);
		}

		GlueLogQuery filter = (GlueLogQuery) f.clone();
		StringBuilder b = new StringBuilder("(" + queryString + ")");

		// append filter to query
		// rename variables to avoid name conflicts
		String[] vars = filter.getVariables();
		for (int i = 0; i < vars.length; i++) {
			String newName = createVariable(filter);
			filter.replaceVariable(vars[i], newName);
		}

		if (position < 0) {
			filter.replaceVariable(JQueryAPI.getThisVar(), var);
		} else {
			String thisVar = createVariable(filter);
			filter.appendListRef(position, var, thisVar);
			filter.replaceVariable(JQueryAPI.getThisVar(), thisVar);
		}

		b.append(",(" + filter.getString() + ")");

		JQuery result = new GlueLogQuery(b.toString(), env);
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
		String var = "V";
		String vars[] = getVariables();
		String otherVars[] = other.getVariables();
		boolean contains = true;

		for (int j = 0; contains; j++) {
			contains = false;
			var = "V" + j;
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

//	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//		in.defaultReadObject();
//
//		query = null;
//		queryVars = null;
//	}
}