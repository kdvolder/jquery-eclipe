package ca.ubc.jquery.browser.menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryMenuResults;
import ca.ubc.jquery.browser.actions.DoTopLevelQueryAction;
import ca.ubc.jquery.browser.actions.NewTopLevelQueryAction;
import ca.ubc.jquery.gui.JQueryTreeViewer;

public class TopQueryMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_TOP_QUERY = "ca.ubc.jquery.JQueryTreeView.menu.top.query";

	public TopQueryMenuProvider(JQueryTreeViewer view, IMenuManager context) {
		super(view, context);
	}

	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_TOP_QUERY));
	}

	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		// can't do top queries if the tree isn't rooted at the factbase
		if (!view.isRootTree()) {
			return;
		}
		
		IMenuManager topQueries = new MenuManager("Available Top-Level Queries");
		menu.appendToGroup(GROUP_TOP_QUERY, topQueries);
		try {
			JQueryMenuResults rs = JQueryAPI.topLevelQuery();
			for (; rs.hasNext(); rs.next()) {
				String label = rs.getLabel();
				String[] path = rs.getPath();
				String query = rs.getQuery();
				String[] vars = rs.getChosenVariables();

				// This here adds all them purrty choices
				Action action = new DoTopLevelQueryAction(label, view, query, vars);
				createPath(topQueries, path, action, GROUP_TOP_QUERY);
			}
		} catch (JQueryException e) {
			// this shouldn't happen unless rules are written incorrectly
			JQueryTreeBrowserPlugin.error("Error occurred while retrieving available top-level queries: ", e);
		}
		
		//		JQueryResultSet results = null;
		//
		//		try {
		//			JQuery query = JQueryAPI.topLevelQuery();
		//			SortedMap snippets = new TreeMap();
		//
		//			results = query.execute();
		//			String[] vars = query.getVariables();
		//
		//			while (results.hasNext()) {
		//				JQueryResult result = results.next();
		//
		//				Object[] path = (Object[]) result.get(vars[0]);
		//				String ruleName = (String) result.get(vars[1]);
		//				Object[] varList = (Object[]) result.get(vars[2]);
		//
		//				String label = createLabel(path);
		//				snippets.put(label, new Object[] { path, ruleName, varList });
		//			}
		//
		//			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
		//
		//				String label = (String) iter.next();
		//				Object[] o = (Object[]) snippets.get(label);
		//
		//				Object[] path = (Object[]) o[0];
		//				String ruleName = (String) o[1];
		//				Object[] varList = (Object[]) o[2];
		//
		//				// create the menu item
		//				Action action = new DoTopLevelQueryAction(label, view, ruleName, varList);
		//
		//				// This here adds all them purrty choices
		//				this.createPath(topQueries, path, action, GROUP_TOP_QUERY);
		//			}
		//
		//			menu.appendToGroup(GROUP_TOP_QUERY, topQueries);
		//
		//		} catch (JQueryException e) {
		//			// this shouldn't happen unless rules are written incorrectly
		//			JQueryTreeBrowserPlugin.error("Error occurred while retrieving available top-level queries: ", e);
		//		} finally {
		//			if (results != null) {
		//				results.close();
		//			}
		//		}

		Action newTopLevelQueryAction = new NewTopLevelQueryAction("New Top-Level Query...", view);
		menu.appendToGroup(GROUP_TOP_QUERY, newTopLevelQueryAction);
	}	
}
