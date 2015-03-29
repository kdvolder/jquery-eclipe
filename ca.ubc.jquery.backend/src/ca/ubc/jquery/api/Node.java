package ca.ubc.jquery.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.ubc.jquery.JQueryBackendPlugin;

/**
 * 
 * @author lmarkle
 */
public class Node implements JQueryResultNode {
	protected JQueryResultGraph graph;

	private Object value;

	private Map boundVariables;

	private int count;

	protected Node(JQueryResultGraph graph, Map vars, Object value, int count) {
		boundVariables = vars;
		this.graph = graph;
		this.value = value;
		this.count = count + 1;
	}

	public Object getValue() {
		return value;
	}

	public Node[] getChildren() {
		try {
			return getChildren(false);
		} catch (JQueryException e) {
			JQueryBackendPlugin.error(e);
			return null;
		}
	}

	private Node[] getChildren(boolean useFilter) throws JQueryException {
		Node[] ch = (Node[]) graph.getNodes().get(value);
		if (ch == null) {
			Collection result = null;

			if (graph.getQuery().getChosenVars().get(count - 1).equals(graph.getQuery().getRecursiveVar())) {
				// we are recursively executing the query
				Map vmap = new HashMap();
				vmap.put(JQueryAPI.getThisVar(), value);
				result = graph.computeChildren(vmap, 0, useFilter);

				// make sure we don't cut off any variables...
				if (count < graph.getQuery().getChosenVars().size()) {
					Collection c1 = graph.computeChildren(boundVariables, count, useFilter);
					result.addAll(c1);
				}

				ch = (Node[]) result.toArray(new Node[result.size()]);

				if (!useFilter) {
					graph.getNodes().put(value, ch);
				}
			} else {
				// else behave normally
				result = graph.computeChildren(boundVariables, count, useFilter);
				ch = (Node[]) result.toArray(new Node[result.size()]);
			}
		}

		return ch;
	}

	protected boolean isVisible() throws JQueryException {
		// simplest case: if this node matches the filtered query
		if (visible()) {
			return true;
		}

		// no luck, check if one of the children passes the filtered query
		Node[] x = getChildren(true);
		if (x.length > 0) {
			return true;
		}

		if (graph.getQuery().getChosenVars().get(count - 1).equals(graph.getQuery().getRecursiveVar())) {
			graph.getNodes().put(value, x);
		}

		// still no luck, unfilter the query, compute it's children, see if they are visible
		x = getChildren();
		for (int i = 0; i < x.length; i++) {
			Node n = x[i];
			if (n.isVisible()) {
				return true;
			}
		}

		return false;
	}

	private boolean visible() throws JQueryException {
		JQuery query = (JQuery) graph.getQuery().clone();
		JQueryResultSet rs = null;
		try {
			query.unbindVariables();
			graph.bindQueryVariables(boundVariables, query);
			query.bind((String) query.getChosenVars().get(count - 1), getValue());
			rs = query.execute();
			if (rs.hasNext()) {
				return true;
			}
		} catch (JQueryException e) {
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "Node:" + value;
	}
}