/*
 * Created on May 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.gui.results;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;

/**
 * @author wannop
 * @author lmarkle
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryResultNode extends ResultsTreeNode {
	public final static long serialVersionUID = 2L;

	public final static int StalenessThreshold = 10;
	
	private static final Comparable<?> NO_SORTKEY = "";

	private Object value; // must be serializable

	/**
	 * Tells what variable in the query result the value came from.
	 * 
	 * This is null if the value came from inside of a list bound to a variable.
	 * This more complicated case is currently not supported.
	 */
	private String source;

	private int valueCategory = -1;
	
	/**
	 * Cached value of the sort key for this element.
	 */
	private Comparable<?> sortKey = null;

	private transient QueryNode queryNode = null;

	/**
	 * Some complexity to manage node visibility.
	 * 
	 * Nodes are only visible if this value is >= their parents value
	 * This way we can easily see if a node is visible or not without causing the
	 * view to refresh and temporarily hide our results while updating.
	 * 
	 * Essentially we hide this node by incrementing it's parents visible number.
	 */
	private int visible;

	private int listPosition;

	public QueryResultNode(Object value, String source) {
		this(value, source, JQuery.NoPosition);
	}

	public QueryResultNode(Object value, String source, int listPosition) {
		this.value = value;
		this.source = source;
		this.listPosition = listPosition;
		this.queryNode = null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof QueryResultNode) {
			QueryResultNode n = (QueryResultNode) o;

			if (getElementSource().equals(n.getElementSource()) && getListPosition() == n.getListPosition()) {
				return super.equals(o);
			} else {
				return false;
			}
		}

		return false;
	}

	public Object getElement() {
		return value;
	}

	/**
	 * Tells what variable in the query result the value came from.
	 * 
	 * This is null if the value came from inside of a list bound to a variable.
	 * This more complicated case is currently not supported.
	 */
	public String getElementSource() {
		return source;
	}

	public boolean isSourceList() {
		return (listPosition >= 0);
	}

	public int getListPosition() {
		return listPosition;
	}

	/**
	 * Returns the parent QueryNode of the selection
	 * 
	 */
	public QueryNode getQueryNode() {
		if (queryNode == null) {
			ResultsTreeNode parent = getParent();
			while (queryNode == null && parent != null && parent instanceof QueryResultNode) {
				queryNode = ((QueryResultNode) parent).queryNode;
				parent = parent.getParent();
			}

			if (parent instanceof QueryNode) {
				queryNode = (QueryNode) parent;
			} else if (queryNode == null) {
				throw new Error("Unexpected parent for node: " + toString());
			}
		}

		return queryNode;
		//		ResultsTreeNode parent = getParent();
		//		if (parent instanceof QueryNode) {
		//			return (QueryNode) parent;
		//		} else if (parent instanceof QueryResultNode) {
		//			return ((QueryResultNode) parent).getQueryNode();
		//		} else {
		//			throw new Error("Unexpected parent for node: " + toString());
		//		}
	}

	public int getCategory() {
		if (valueCategory == -1) {
			try {
				valueCategory = JQueryAPI.getIntProperty(getElement(), "category");
			} catch (Exception e) {
				JQueryTreeBrowserPlugin.traceUI("ResultsTreeSorter.category(): no category value for element:" + value);
				valueCategory = 0;
			}
		}

		return valueCategory;
	}
	
	public Comparable getSortKey() {
		if (sortKey == null) {
			try {
				sortKey = (Comparable) JQueryAPI.getObjectProperty(getElement(), "sortKey");
			} catch (Exception e) {
				JQueryTreeBrowserPlugin.traceUI("ResultsTreeSorter.category(): no sortKey value for element:" + value);
				sortKey = NO_SORTKEY; // Don't put null here otherwise it will always try to recompute the sort key
			}
		}

		return sortKey != NO_SORTKEY ? sortKey : null;
	}

	@Override
	protected boolean isVisible() {
		return (getQueryNode().getVisible() <= visible);
	}

	@Override
	protected boolean shouldRemove() {
		// always remove nodes with no parent
		if (getParent() == null) {
			return true;
		}
		// only remove nodes if they've reached their staleness threshold
		return (getQueryNode().getVisible() - visible > StalenessThreshold);
	}

	@Override
	protected void show() {
		visible = getQueryNode().getVisible();
	}

	@Override
	public String toString() {
		return "QueryResultNode(" + value + ")";
	}

	public void setParent(ResultsTreeNode node) {
		super.setParent(node);
		queryNode = null;
	}

	public Object clone() {
		QueryResultNode qrn = new QueryResultNode(getElement(), getElementSource());
		// this parent gets erase later anyway but if we don't do this, we get null pointer
		// exceptions while cloning because cloneChildren() calls addChild() which calls
		// show() which needs a parent.  So we give it a parent temporarily that gets
		// properly set in addChild() anyway.
		qrn.setParent(getParent());
		cloneChildren(qrn);

		return qrn;
	}
}
