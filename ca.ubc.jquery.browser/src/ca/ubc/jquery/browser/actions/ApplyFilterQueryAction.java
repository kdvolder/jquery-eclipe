package ca.ubc.jquery.browser.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;

public class ApplyFilterQueryAction extends Action {

	private JQueryTreeViewer view;

	private String filter;

	private String label;

	public ApplyFilterQueryAction(String label, String filter, JQueryTreeViewer view) {
		super(label, Action.AS_CHECK_BOX);
		this.label = label;
		this.view = view;
		this.filter = filter;
	}

	public void run() {
		IStructuredSelection selection = (IStructuredSelection) view.getSelection();
		Iterator itr = selection.iterator();
		while (itr.hasNext()) {
			QueryResultNode node = (QueryResultNode) itr.next();

			QueryNode qn = node.getQueryNode();
			//			if (!(qn.getQuery() instanceof QueryResultsAndFilter)) {
			//				qn.setQuery(new QueryResultsAndFilter(qn.getQuery()));
			//			}

			if (isChecked()) {
				//				QueryResultsAndFilter qrf = (QueryResultsAndFilter) qn.getQuery();
				try {
					qn.getQuery().addFilter(label, JQueryAPI.createQuery(filter), node.getElementSource(), node.getListPosition());
					view.execute(qn);
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error("Applying filter: " + filter + ": ", e);
				}
				//				viewer.reveal(qn);
			} else {
				//				QueryResultsAndFilter qrf = (QueryResultsAndFilter) qn.getQuery();
				qn.getQuery().removeFilter(label);
				view.execute(qn);
			}
		}
	}
}
