package ca.ubc.jquery.gui.tree;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;

import ca.ubc.jquery.ActionsLog;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 *
 * @author unknown
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ResultsTreeListener implements ITreeViewerListener {

	/**
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(TreeEvent)
	 */
	public void treeCollapsed(TreeExpansionEvent e) {
		ResultsTreeNode node = (ResultsTreeNode) e.getElement();
		node.setExpandstate(node.NodeCollapsed);

		ActionsLog.logGeneric("Tree node collapsed", node.getElement().toString());
	}

	/**
	 * @see org.eclipse.swt.events.TreeListener#treeExpanded(TreeEvent)
	 */
	public void treeExpanded(TreeExpansionEvent e) {
		ResultsTreeNode node = (ResultsTreeNode) e.getElement();
		node.setExpandstate(node.NodeExpanded);

		ActionsLog.logGeneric("Tree node expanded", node.getElement().toString());
	}

}
