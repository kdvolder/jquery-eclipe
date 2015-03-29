package ca.ubc.jquery.browser.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class DoSubQueryAction extends Action {
	private JQueryTreeViewer view;

	private String ruleText;

	private String[] varStrings;

	private String label;

	public DoSubQueryAction(JQueryTreeViewer view, String label, String ruleText, String[] varStrings) {
		this.view = view;
		this.label = label;
		this.ruleText = ruleText;
		this.varStrings = varStrings;
	}

	public void run() {
		// for each selected node
		IStructuredSelection selection = (IStructuredSelection) view.getSelection();
		Iterator itr = selection.iterator();
		while (itr.hasNext()) {
			ResultsTreeNode node = (ResultsTreeNode) itr.next();

			// String selectedNodeName = labelProvider.getText(node);
			// JQueryTreeBrowserPlugin.traceUI("Running Query on: " + selectedNodeName);

			// log query
			// ActionsLog.logGeneric("Menu query run", new String[] { "query: " + label, "on object: " + selectedNodeName });

			// create query object
			//			IQueryResults menuQuery;
			try {
				//				menuQuery = new QueryResults(node.getElement(), ruleText);
				//				menuQuery.setChosenVars(varStrings);
				//				JQuery menuQuery = JQueryAPI.createQuery(ruleText);
				//				menuQuery.bind(JQueryAPI.getThisVar(), node.getElement());
				//				menuQuery.setChosenVars(varStrings);
				QueryNode n = view.createQueryNode(ruleText, label, node);
				n.getQuery().bind(JQueryAPI.getThisVar(), node.getElement());
				n.getQuery().setChosenVars(varStrings);
				view.execute(n);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Error creating Query object: ", e);
				JQueryTreeBrowserPlugin.traceUI("ResultsMenuSelection: parseError creating Query object (this shouldn't happen: fix code/rules: " + e.getMessage());
			}
		}
	}
}
