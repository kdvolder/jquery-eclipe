package ca.ubc.jquery.gui.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.part.PluginTransfer;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.dnd.JQueryFilterTransfer;
import ca.ubc.jquery.gui.dnd.JQueryResultsTreeTransfer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.views.JQueryTreeView;

/**
 * Supports dropping gadgets into a tree viewer.
 */
public class ResultsTreeDropAdapter extends ViewerDropAdapter implements IDropActionDelegate {
	private JQueryTreeView view;

	private QueryResultNode filterDrop;

	public ResultsTreeDropAdapter(TreeViewer viewer, JQueryTreeView view) {
		super(viewer);
		this.view = view;
		filterDrop = null;
	}

	public boolean run(Object source, Object target) {
		System.out.println(source + " -> " + target);
		return false;
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean performDrop(Object data) {

		if (filterDrop == null) {
			ResultsTreeNode[] d = (ResultsTreeNode[]) data;

			// filter out QueryNodes
			List t = new ArrayList();
			for (int i = 0; i < d.length; i++) {
				if (!(d[i] instanceof QueryNode)) {
					t.add(d[i].getElement());
				}
			}

			Object[] targets = new Object[t.size()];
			Iterator it = t.iterator();
			for (int i = 0; it.hasNext(); i++) {
				targets[i] = it.next();
			}

			view.setBrowserTarget(targets);
		} else {
			QueryNode n = filterDrop.getQueryNode();
			Object[] d = (Object[]) data;
			for (int i = 0; i < d.length; i++) {
				Object[] f = (Object[]) d[i];
				n.getQuery().addFilter(f[0].toString(), (JQuery) f[1], filterDrop.getElementSource(), filterDrop.getListPosition());
			}

			((JQueryTreeViewer) view.getViewer()).doReExecuteAction(new StructuredSelection(n));
		}

		return true;
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean validateDrop(Object target, int op, TransferData type) {
		if (getViewer().getInput() instanceof QueryNode && checkType(type)) {
			filterDrop = null;
			return true;
		}

		if (JQueryFilterTransfer.getInstance().isSupportedType(type) && target instanceof QueryResultNode) {
			filterDrop = (QueryResultNode) target;
			return true;
		}

		filterDrop = null;
		return false;
	}

	private boolean checkType(TransferData type) {
		boolean result = true;
		result = JQueryResultsTreeTransfer.getInstance().isSupportedType(type);
		result = result || PluginTransfer.getInstance().isSupportedType(type);
		return result;
	}
}