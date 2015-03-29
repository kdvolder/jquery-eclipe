package ca.ubc.jquery.browser.menu;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.browser.actions.ApplyFilterQueryAction;
import ca.ubc.jquery.browser.actions.FilterQueryAction;
import ca.ubc.jquery.browser.actions.NewSubQueryAction;
import ca.ubc.jquery.gui.JQueryResultsLabelProvider;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class FilterMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_NODE_FILTER = "ca.ubc.jquery.JQueryTreeView.menu.result.node.filter";

	private JQueryResultsLabelProvider provider;

	public FilterMenuProvider(JQueryTreeViewer view, IMenuManager menuContext, JQueryResultsLabelProvider provider) {
		super(view, menuContext);
		this.provider = provider;
	}

	@Override
	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		Object[] targets = findTargets(selection);

		Action newSubQueryAction = new NewSubQueryAction("New Sub-Query...", view, provider);
		// Filter by Regular Expression action
		Action filterQueryAction = new FilterQueryAction("Filter Query", view);

		// TODO: Remove this when filters are working properly
		// What does this mean....?
		final ResultsTreeNode t = getElement(selection);
		QueryNode qn = null;
		if (t instanceof QueryNode) {
			qn = (QueryNode) t;
		} else if (t instanceof QueryResultNode) {
			qn = ((QueryResultNode) t).getQueryNode();
		}

		if (qn == null || !qn.getQuery().isRecursive()) {
			// TODO Make this code cleaner...
			//			menu.appendToGroup(GROUP_NODE_FILTER, new Action("Experimental") {
			//				public void run() {
			//					if (t instanceof QueryResultNode) {
			//						Window d = new FilterDialog(JQueryTreeBrowserPlugin.getShell(), view, (QueryResultNode) t);
			//						d.open();
			//					} else {// Do Nothing
			//					}
			//				}
			//			});

			if (t instanceof QueryResultNode) {
				menu.appendToGroup(GROUP_NODE_FILTER, filterQueryAction);
			}
			addAvailableMenu(menu, selection, targets);
		}

		if (getElement(selection) != null) {
			menu.appendToGroup(GROUP_NODE_FILTER, newSubQueryAction);
		}
	}

	private void addAvailableMenu(IMenuManager menu, IStructuredSelection selection, Object[] targets) {
		// don't run this if nothing is selected
		if (selection.size() == 0 || !(getElement(selection) instanceof QueryResultNode)) {
			return;
		}

		QueryNode qn = ((QueryResultNode) selection.getFirstElement()).getQueryNode();
		List filters = qn.getQuery().getFilters();

		IMenuManager filter = new MenuManager("Filter...");
		menu.appendToGroup(GROUP_NODE_FILTER, filter);

		JQueryResultSet rs = null;
		try {
			SortedMap snippets = new TreeMap();

			JQuery q = JQueryAPI.filterQuery(targets);
			rs = q.execute();
			while (rs.hasNext()) {
				JQueryResult r = rs.next();

				Object[] path = (Object[]) r.get(0);
				String label = createLabel(path);
				String filterString = (String) r.get(1);

				snippets.put(label, new Object[] { path, filterString });
			}

			// create menu from sorted list
			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
				Object[] o = (Object[]) snippets.get(iter.next());

				Object[] pathTerm = (Object[]) o[0];
				String filterString = (String) o[1];
				String label = createLabel(pathTerm);

				// create the menu item
				Action action = new ApplyFilterQueryAction(label, filterString, view);

				// This here adds all them purrty choices
				createPath(filter, pathTerm, action, GROUP_NODE_FILTER);

				if (filters.contains(label)) {
					action.setChecked(true);
				} else {
					action.setChecked(false);
				}
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	@Override
	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_NODE_FILTER));
	}
}
