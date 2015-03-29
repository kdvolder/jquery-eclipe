package ca.ubc.jquery.browser.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryResultsLabelProvider;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.dialogs.QueryDialog;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class NewSubQueryAction extends Action {
	private JQueryTreeViewer view;

	private JQueryResultsLabelProvider labelProvider;

	public NewSubQueryAction(String text, JQueryTreeViewer view, JQueryResultsLabelProvider provider) {
		super(text);
		this.view = view;
		this.labelProvider = provider;

	}

	@Override
	public void run() {
		IStructuredSelection targetNodes = (IStructuredSelection) view.getSelection();
		if (targetNodes.isEmpty()) {
			JQueryTreeBrowserPlugin.error("Cannot create sub-query: no result node selected");
			return;
		}

		ResultsTreeNode target = (ResultsTreeNode) targetNodes.getFirstElement();
		QueryDialog newQueryDialog = new QueryDialog(JQueryTreeBrowserPlugin.getShell(), target.getElement(), "New Sub-Query: " + labelProvider.getText(target));

		// show dialog
		int result = newQueryDialog.open();
		// only create nodes if the user clicked "ok"
		if (result == Dialog.OK) {
			for (Iterator iter = targetNodes.iterator(); iter.hasNext();) {
				ResultsTreeNode targetNode = (ResultsTreeNode) iter.next();
				try {
					QueryNode n = view.createQueryNode("", newQueryDialog.getQueryLabel(), targetNode);
					JQuery sub = (JQuery) newQueryDialog.getQuery().clone();
					sub.bind(JQueryAPI.getThisVar(), targetNode.getElement());
					n.setQuery(sub);
					view.execute(n);
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error("Creating new sub query: ", e);
				}
			}
		}
	}
}
