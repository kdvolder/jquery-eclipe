package ca.ubc.jquery.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ca.ubc.jquery.JQueryBackendPlugin;

/**
 * 
 * @author lmarkle
 */
public class JQueryResultGraph implements JQueryResultNode {
	private JQuery query;

	private JQuery queryNoFilters;

	private boolean infiniteRecursion;

	private Map/*<Value,Node[]>*/nodes;

	private String[] vars;

	protected JQueryResultGraph(JQuery q, String[] variables) {
		query = (JQuery) q.clone();
		queryNoFilters = removeFilters(query);
		nodes = new HashMap/*<Value,Node[]>*/();
		infiniteRecursion = false;
		vars = variables;
	}

	public void useInfiniteRecursion(boolean b) {
		infiniteRecursion = b;
	}

	public Object getValue() {
		return query;
	}

	public JQueryResultNode[] getChildren() {
		Collection c;
		c = computeChildren(cloneHashMap(query.getBoundVars()), 0, false);
		JQueryResultNode[] result = (JQueryResultNode[]) c.toArray(new Node[c.size()]);
		return result;
	}

	protected Collection computeChildren(Map boundVars, int var, boolean useFilters) {
		Map result = new HashMap/*<Value,Node[]>*/();
		JQueryResultSet rs = null;
		JQuery execQuery = query;

		try {
			if (!useFilters) {
				execQuery = queryNoFilters;
			}

			// early out if we've already evaluated everything in the query
			if (!execQuery.isRecursive() && vars.length == var) {
				return new ArrayList();
			}

			String recursiveVar = execQuery.getRecursiveVar();
			bindQueryVariables(boundVars, execQuery);
			rs = execQuery.execute();

			while (rs.hasNext()) {
				JQueryResult r = rs.next();
				Object val = r.get(var);
				boundVars.put(currentVar(var), val);

				if (infiniteRecursion) {
					result.put(val, new Node(this, cloneHashMap(boundVars), val, var));
				} else if (nodes.get(val) == null || currentVar(var).equals(recursiveVar)) {
					result.put(val, new Node(this, cloneHashMap(boundVars), val, var));
				}
			}

			// take out results that don't pass the filter
			if (!useFilters && !query.getFilterMap().isEmpty()) {
				for (Iterator it = result.entrySet().iterator(); it.hasNext();) {
					Node n = (Node) ((Map.Entry) it.next()).getValue();
					if (!n.isVisible()) {
						it.remove();
					}
				}
			}
		} catch (JQueryException e) {
			JQueryBackendPlugin.error(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return new ArrayList(result.values());
	}

	protected String currentVar(int count) {
		return vars[count];
	}

	protected Map cloneHashMap(Map input) {
		Map result = new HashMap/*<Value,Node[]>*/();
		for (Iterator it = input.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}

	protected JQuery removeFilters(JQuery q) {
		JQuery result = (JQuery) q.clone();
		for (Iterator it = result.getFilters().iterator(); it.hasNext();) {
			result.removeFilter((String) it.next());
		}
		return result;
	}

	protected void bindQueryVariables(Map vars, JQuery q) throws JQueryException {
		q.unbindVariables();
		for (Iterator it = vars.entrySet().iterator(); it.hasNext();) {
			Map.Entry ent = (Map.Entry) it.next();
			q.bind((String) ent.getKey(), ent.getValue());
		}
	}

	protected JQuery getQuery() {
		return query;
	}

	protected Map getNodes() {
		return nodes;
	}
}
