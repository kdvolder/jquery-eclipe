package ca.ubc.jquery.browser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.dialogs.RegexpDialog;
import ca.ubc.jquery.gui.results.QueryResultNode;

public class FilterQueryAction extends Action {
	private JQueryTreeViewer view;

	public FilterQueryAction(String name, JQueryTreeViewer view) {
		super(name);
		this.view = view;
	}

	public void run() {
		IStructuredSelection selection = (IStructuredSelection) view.getSelection();
		if (selection.isEmpty()) {
			JQueryTreeBrowserPlugin.error("Cannot filter query: no result node selected");
			return;
		}

		QueryResultNode node = (QueryResultNode) selection.getFirstElement();
		//		QueryNode qn = node.getQueryNode();
		//		if (!(qn.getQuery() instanceof QueryResultsAndFilter)) {
		//			qn.setQuery(new QueryResultsAndFilter(qn.getQuery()));
		//		}

		RegexpDialog d = new RegexpDialog(JQueryTreeBrowserPlugin.getShell(), view, node);
		d.open();
	}
}
