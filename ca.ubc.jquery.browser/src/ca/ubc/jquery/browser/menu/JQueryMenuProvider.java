package ca.ubc.jquery.browser.menu;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public abstract class JQueryMenuProvider {

	protected JQueryTreeViewer view;

	public JQueryMenuProvider(JQueryTreeViewer view) {
		this.view = view;
	}

	protected ResultsTreeNode getElement(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		} else {
			return (ResultsTreeNode) selection.getFirstElement();
		}
	}

	public void dispose() {

	}

	abstract public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection);

	abstract public void addMenuGroup(IMenuManager menu);
}
