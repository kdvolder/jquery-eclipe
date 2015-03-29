package ca.ubc.jquery.gui.tree;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.SelectionListenerExtensionHandler;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.NoResultNode;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * This class responds to double clicks in the ResultsView tree. When items are double clicked, the file that contains that item is opened and the item is selected in the editor.
 */
public class ResultsTreeClickListener implements IDoubleClickListener, IOpenListener {

	private JQueryTreeViewer view;

	/**
	 * The constructor.
	 */
	public ResultsTreeClickListener(JQueryTreeViewer view) {
		this.view = view;
	}

	/**
	 * Responds to double click events in the View.
	 */
	public void doubleClick(DoubleClickEvent e) {

		IStructuredSelection selection = (IStructuredSelection) e.getSelection();

		ResultsTreeNode node = (ResultsTreeNode) selection.getFirstElement();
		if (node == null) {
			return;
		}

		// don't open no result node
		if (node instanceof NoResultNode) {
			return;
		}

		// double click edits a query
		if (node instanceof QueryNode) {
			view.doEditQuery((QueryNode) node);
			return;
		}

		try {
			Object value = node.getElement();
			if (value instanceof JQueryFileElement) {
				((JQueryFileElement) value).openInEditor();
			} else {
				JQueryFileElement loc = JQueryAPI.getFileElement(value);
				if (loc != null) {
					loc.openInEditor();
				}
			}
		} catch (JQueryException ex) {
			JQueryTreeBrowserPlugin.error(ex);
		}
		
		SelectionListenerExtensionHandler.fireSelected(selection);
	}

	public void open(OpenEvent event) {
		DoubleClickEvent ev = new DoubleClickEvent(event.getViewer(), event.getSelection());
		doubleClick(ev);
	}
}
