package ca.ubc.jquery.gui.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

/**
 * @funky
 */
public abstract class ResultsTreeNode implements Serializable, Cloneable {

	// ALWAYS UPDATE THIS VALUE IF YOU CHANGE THE NODE FIELDS
	// YOU DON'T NEED TO UPDATE THIS IF YOU ONLY CHANGE METHODS,
	// ONLY IF YOU ADD/REMOVE FIELDS THAT ARE SERIALIZED.
	public static final long serialVersionUID = 1L;

	public static final int NodeUnused = -1;

	public static final int NodeCollapsed = 0;

	public static final int NodeExpanded = 1;

	/** One of 3 values: -1 not used, 0 user collapsed, 1 user expanded */
	private int expandState = NodeUnused;

	private Map myChildren = new HashMap();

	// public Vector children = new Vector();
	private ResultsTreeNode parent = null;

	// helpful for caching results 
	public transient String cachedLabel = null;

	public transient Image cachedImage = null;

	abstract public Object getElement();

	abstract public Object clone();

	/**
	 * Allows a node more fine grained control over when it should be removed.  It will
	 * call this method before actually removing the node.
	 */
	protected boolean shouldRemove() {
		return true;
	}

	protected void cloneChildren(ResultsTreeNode node) {
		for (Iterator it = getChildren().iterator(); it.hasNext();) {
			ResultsTreeNode n = (ResultsTreeNode) it.next();
			n = node.addChild((ResultsTreeNode) n.clone());
			// set parent (even though it's done by addchild) so that query results node
			// clear the queryNode field used for optimizations.
			n.setParent(node);
		}
	}

	/**
	 * Inserts a child of this node.  Will not allow duplicate insertions.  If a child 
	 * exists under this node, the existing child is returned.
	 * @param child node to insert
	 * @return the inserted child (or the existing one)
	 */
	public synchronized ResultsTreeNode addChild(ResultsTreeNode child) {
		ResultsTreeNode result = (ResultsTreeNode) myChildren.get(child.getElement());
		if (result == null) {
			myChildren.put(child.getElement(), child);
			child.setParent(this);
			result = child;
		}
		result.show();
		return result;
	}

	public synchronized void removeChild(ResultsTreeNode child) {
		if (child.shouldRemove()) {
			Object key = child.getElement();
			ResultsTreeNode foundChild = (ResultsTreeNode) myChildren.remove(key);

			if (foundChild != null) {
				removeChildHelper(foundChild);
			}
		}
	}

	private synchronized void removeAllChildren() {
		// remove references to this parent node from children
		Collection childNodes = myChildren.values();
		for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			removeChildHelper(element);
		}
		myChildren.clear();
	}

	private void removeChildHelper(ResultsTreeNode child) {
		// remove children of found node because if we don't, we'll likely have a
		// null pointer exception trying to remove a node twice
		//
		// TODO If a parent shouldRemove() does that imply the children do too?
		for (Iterator it = child.myChildren.values().iterator(); it.hasNext();) {
			removeChild((ResultsTreeNode) it.next());
		}

		child.cachedLabel = null;
		child.cachedImage = null;
		child.setParent(null);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ResultsTreeNode) {
			ResultsTreeNode n = (ResultsTreeNode) o;

			boolean result = (n.getElement() == null ? getElement() == null : n.getElement().equals(getElement()));
			result = result && checkParent(n);
			return result;
		}
		return false;
	}

	private boolean checkParent(ResultsTreeNode n) {
		return (getParent() == null ? n.getParent() == null : getParent().equals(n.getParent()));
	}

	@Override
	public int hashCode() {
		return (getElement() == null ? 0 : getElement().hashCode());
	}

	public ResultsTreeNode getParent() {
		return parent;
	}

	public void setParent(ResultsTreeNode n) {
		parent = n;
	}

	public Collection getChildren() {
		Collection unfiltered = myChildren.values();
		ArrayList filtered = new ArrayList(unfiltered.size());
		for (Iterator iter = unfiltered.iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			if (element.isVisible())
				filtered.add(element);
		}
		return filtered;
	}

	protected Collection getHiddenChildren() {
		Collection unfiltered = myChildren.values();
		ArrayList filtered = new ArrayList(unfiltered.size());
		for (Iterator iter = unfiltered.iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			if (!element.isVisible())
				filtered.add(element);
		}
		return filtered;
	}

	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	public int numAllChildren() {
		return getChildren().size() + getHiddenChildren().size();
	}

	protected boolean isVisible() {
		return true;
	}

	protected void show() {
	}

	/**
	 * 
	 * @author andrew
	 * 
	 * For the purposes of sorting by score.
	 * 
	 * A node has a score if: 1) it has only one child element 2) that child is a numeric value
	 */
	public boolean hasScore() {
		if (numAllChildren() != 1) {
			return false;
		}

		if (score != -1) {
			return true;
		}

		// should only have 1 child. technically, don't need loop
		// but makes things a little safer.
		Collection children = getChildren();
		children.addAll(getHiddenChildren());
		for (Iterator childIter = children.iterator(); childIter.hasNext();) {
			ResultsTreeNode child = (ResultsTreeNode) childIter.next();
			if (child.getElement() instanceof Number) {
				score = ((Number) child.getElement()).intValue();
				return true;
			}
		}
		return false;

	}

	public int getScore() {
		hasScore(); // has side-effect of calculating score if not already done.
		return -score; // negate score so that we sort from highest to lowest.
		// may want to rethink this in the future
	}

	/**
	 * the score is used for sorting.
	 * 
	 * -1 means that either there is no score or the score hasn't been calculated yet.
	 * 
	 * @see #hasScore()
	 */
	private int score = -1;

	public int getExpandState() {
		return expandState;
	}

	public void setExpandstate(int x) {
		expandState = x;
	}
}