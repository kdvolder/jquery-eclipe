/*
 * Created on May 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.gui.results;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IMemento;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;

/**
 * @author wannop
 * @author lmarkle
 * 
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryNode extends ResultsTreeNode {
	public final static long serialVersionUID = 5L;

	private JQuery query;

	private int autoExpandDepth;

	private int visible;

	private String label;

	private transient boolean doingUpdate = false;

	private transient static Action autoRefreshAction = new Action("Auto Refresh View", Action.AS_CHECK_BOX) {};

	/** 
	 * Prevents recursive queries from running forever...
	 */
	private transient Map recursiveChildren = null;

	public QueryNode(String query, String label) throws JQueryException {
		this(JQueryAPI.createQuery(query), label);
	}

	public QueryNode(IMemento memento) {
		try {
			label = memento.getString("queryLabel");
			Integer auto = memento.getInteger("autoExpand");
			autoExpandDepth = (auto == null) ? 0 : auto.intValue();
			query = JQueryAPI.createQuery(memento);
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error(e);
		}
	}

	public void saveState(IMemento memento) throws JQueryException {
		memento.putString("queryLabel", label);
		memento.putInteger("autoExpand", autoExpandDepth);
		query.saveState(memento);
	}

	private QueryNode(JQuery query, String label) {
		this.label = label;
		this.query = query;

		visible = 0;
	}

	public void setQuery(JQuery query) {
		// Because addChild/remove child indexes children based on their elements
		// and this actually sets the element, we have to do this step otherwise remove
		// won't work and the node will be in the tree forever... :(
		ResultsTreeNode p = getParent();
		p.removeChild(this);
		this.query = query;
		p.addChild(this);
	}

	public boolean addRecursiveChild(ResultsTreeNode child) {
		if (recursiveChildren == null) {
			recursiveChildren = new HashMap();
		}

		if (recursiveChildren.containsKey(child.getElement())) {
			return false;
		} else {
			recursiveChildren.put(child.getElement(), child);
			return true;
		}
	}

	public void clearRecursiveChildren() {
		if (recursiveChildren != null) {
			recursiveChildren.clear();
		}
	}

	public Action getAutoRefreshAction() {
		return autoRefreshAction;
	}

	public int getAutoExpandDepth() {
		return autoExpandDepth;
	}

	/** @return the Query object used to compute this node's children * */
	public JQuery getQuery() {
		return query;
	}

	public void setAutoExpansionDepth(int v) {
		autoExpandDepth = v;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAutoExpansionDepth() {
		InputDialog d = new InputDialog(JQueryTreeBrowserPlugin.getShell(), "Auto Expansion Depth", "Enter value (0..99)", "" + autoExpandDepth, new IInputValidator() {
			public String isValid(String newText) {
				try {
					Integer.parseInt(newText);
					return null;
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		if (d.open() == InputDialog.OK) {
			autoExpandDepth = Integer.parseInt(d.getValue());
		}
	}

	public Object getElement() {
		return query;
	}

	protected int getVisible() {
		if (doingUpdate) {
			return visible + 1;
		} else {
			return visible;
		}
	}

	public void beginUpdateResults() {
		// TODO: This method may not be necessary
		//
		// It's here because we have to remove the NoResultNode when
		// the query returns results.  Probably there's a better way to do this.
		//
		// For now it's probably easier just to leave this here.
		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			if (element instanceof NoResultNode) {
				removeChild(element);
			}
		}
		doingUpdate = true;
	}

	public void midUpdateResults() {
		doingUpdate = false;
		visible = visible + 1;
	}

	public void endUpdateResults() {
		for (Iterator iter = getHiddenChildren().iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			hideResult(element);
		}
	}

	private void hideResult(ResultsTreeNode n) {
		// maybe remove here (just in case)
		// it's always better to over remove - though potentially slower
		clearRecursiveChildren();

		for (Iterator it = n.getHiddenChildren().iterator(); it.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) it.next();
			hideResult(element);
		}

		removeChild(n);
	}

	public Object clone() {
		QueryNode qn = new QueryNode((JQuery) query.clone(), getLabel());
		cloneChildren(qn);

		qn.autoExpandDepth = autoExpandDepth;

		return qn;
	}
}