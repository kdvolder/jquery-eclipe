package ca.ubc.jquery.gui.results;

/**
 * A class to help manage aggregate results. This class is used to group several result nodes into a single object for querying.
 * 
 * @author lmarkle
 */
public class QueryResultGroupNode extends ResultsTreeNode {

	private Object[] value; // must serialize

	public QueryResultGroupNode(Object[] v) {
		value = v;
	}

	public Object getElement() {
		return value;
	}

	public Object clone() {
		QueryResultGroupNode qrgn = new QueryResultGroupNode(value);
		cloneChildren(qrgn);
		return qrgn;
	}
}
