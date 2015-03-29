package ca.ubc.jquery.browser.actions;

import org.eclipse.jface.action.Action;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;

public class DoTopLevelQueryAction extends Action {
	private JQueryTreeViewer view;

	private String ruleText;

	private String[] varStrings;

	public DoTopLevelQueryAction(String name, JQueryTreeViewer view, String ruleText, Object[] vars) {
		super(name);
		this.view = view;
		this.ruleText = ruleText;

		varStrings = new String[vars.length];
		for (int i = 0; i < vars.length; i++) {
			varStrings[i] = vars[i].toString();
		}
	}

	public void run() {
		//		IQueryResults menuQuery;
		try {
			//			menuQuery = new QueryResults(view.getTreeRoot().getElement(), ruleText);
			//			JQuery menuQuery = JQueryAPI.createQuery(ruleText);
			//			menuQuery.setChosenVars(varStrings);
			QueryNode n = view.createQueryNode(ruleText, getText());
			n.getQuery().setChosenVars(varStrings);
			view.execute(n);
		} catch (Exception e) {
			JQueryTreeBrowserPlugin.error("Error creating Top level Query object: ", e);
			JQueryTreeBrowserPlugin.traceUI("doTopLevelQueryAction: parseError creating Query object (this shouldn't happen: fix code/rules: " + e.getMessage());
		}
	}
}
