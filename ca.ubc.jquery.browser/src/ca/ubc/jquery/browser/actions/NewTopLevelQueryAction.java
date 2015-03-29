package ca.ubc.jquery.browser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.dialogs.QueryDialog;
import ca.ubc.jquery.gui.results.QueryNode;

public class NewTopLevelQueryAction extends Action {
	private JQueryTreeViewer view;

	public NewTopLevelQueryAction(String name, JQueryTreeViewer view) {
		super(name);
		this.view = view;
	}

	@Override
	public void run() {
		QueryDialog newQueryDialog = new QueryDialog(JQueryTreeBrowserPlugin.getShell(), view.getTreeRoot().getElement(), "New Top-Level Query");

		try {
			int result = newQueryDialog.open();
			if (result == Dialog.OK) {
				QueryNode node = view.createQueryNode("", newQueryDialog.getQueryLabel());
				node.setQuery(newQueryDialog.getQuery());
				view.execute(node);
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Creating top level query: ", e);
		}
	}
}
