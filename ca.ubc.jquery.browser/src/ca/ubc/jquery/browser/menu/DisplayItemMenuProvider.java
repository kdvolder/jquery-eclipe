package ca.ubc.jquery.browser.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultGraph;
import ca.ubc.jquery.api.JQueryResultNode;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.JQueryResultsLabelProvider;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.tree.ResultsTreeSorter;
import ca.ubc.jquery.gui.views.JQueryTreeView;

public class DisplayItemMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_DISPLAY = "ca.ubc.jquery.JQueryTreeView.menu.display";

	private JQueryResultsLabelProvider provider;

	private JQueryTreeView viewPart;

	// Menu actions
	private Action sortByCategory;

	private Action showReturnType;

	private Action showFieldType;
	
	private Action showChildCount;

	private ResultsTreeSorter resultsTreeSorter;

	public DisplayItemMenuProvider(JQueryTreeView viewp, IMenuManager menuContext, JQueryResultsLabelProvider provider, ResultsTreeSorter sorter) {
		super((JQueryTreeViewer) viewp.getViewer(), menuContext);
		this.provider = provider;
		viewPart = viewp;
		this.resultsTreeSorter = sorter;

		sortByCategory = new Action("Sort by Element Type", Action.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
					resultsTreeSorter.byCategory = true;
				} else {
					resultsTreeSorter.byCategory = false;
				}
				view.refresh(view.getTreeRoot());
			}
		};
		sortByCategory.setChecked(true);
		showReturnType = new Action("Show Method Return Type", Action.AS_CHECK_BOX) {
			public void run() {
				doLabelOption(JQueryResultsLabelProvider.SHOW_RETURN_TYPE, isChecked());
			}
		};
		showFieldType = new Action("Show Field Type", Action.AS_CHECK_BOX) {
			public void run() {
				doLabelOption(JQueryResultsLabelProvider.SHOW_TYPE, isChecked());
			}
		};
		showChildCount = new Action("Show Query Result Count", Action.AS_CHECK_BOX) {
			public void run() {
				DisplayItemMenuProvider.this.provider.setShowChildCount(isChecked());
				view.refresh(view.getTreeRoot());				
			}
		};
	}

	/**
	 * Sets the given flag on the lableProvider as chosen from the context menu.
	 * 
	 * @vsf+ contextMenu
	 * @vsf+ treeviewer
	 */
	void doLabelOption(int flag, boolean turnOn) {
		if (turnOn) {
			provider.turnOn(flag);
		} else {
			provider.turnOff(flag);
		}
		view.refresh(view.getTreeRoot());
	}

	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_DISPLAY));
	}

	public void addAvailableMenu(IMenuManager menu, final IStructuredSelection selection) {
		Action newTreeRootAction = new Action("New tree from here") {
			public void run() {
				try {
					QueryNode n = (QueryNode) getElement(selection);
					viewPart.createNewTreeView(n, false);
				} catch (Exception e) {
					JQueryTreeBrowserPlugin.message(e);
				}
			}
		};

		//		addNewTreeMenus(menu, selection);

		Action refreshViewAction = new Action("Refresh view") {
			public void run() {
				view.doReExecuteAction(new StructuredSelection(view.getTreeRoot()));
			}
		};
		refreshViewAction.setImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("ReloadRules.gif"));

		Action stupid = new Action("Stupid") {
			public void run() {
				//				// Simple Test
				//				try {
				//					JQuery q = JQueryAPI.createQuery("package(?P),child(?P,?CU),child(?CU,?T),Type(?T)");
				//					JQuery filter = JQueryAPI.createQuery("re_name(!this,/Menu/)");
				//					q.setChosenVars(new String[] { "?P", "?T" });
				//					JQueryResultGraph g = q.getGraph();
				//
				//					System.out.println("======================= Displaying Simple Test =======================");
				//					JQueryResultNode[] x = g.getChildren();
				//					for (int i = 0; i < x.length; i++) {
				//						printChildren(x[i], 1);
				//					}
				//				} catch (JQueryException e) {
				//					JQueryTreeBrowserPlugin.error(e);
				//				}

				// Simple Test (with filter)
				try {
					JQuery q = JQueryAPI.createQuery("package(?P),child(?P,?CU),child(?CU,?T),Type(?T)");
					JQuery filter = JQueryAPI.createQuery("re_name(!this,/Menu/)");
					q.addFilter("regexp", filter, "?T", JQuery.NoPosition);
					q.setChosenVars(new String[] { "?P", "?T" });
					JQueryResultGraph g = q.getGraph();

					System.out.println("======================= Displaying Simple Test (with filter) =======================");
					JQueryResultNode[] x = g.getChildren();
					for (int i = 0; i < x.length; i++) {
						printChildren(x[i], 1);
					}
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error(e);
				}

				// Recursion Test
				try {
					JQuery q = JQueryAPI.createQuery("(equals(!this,?target) ; overrides(?target,!this)),polyCalls(?M2, ?target, ?Ref),child(?C2,?M2)");
					JQuery filter = JQueryAPI.createQuery("re_name(!this,/ok/)");
					q.bind("!this", ((ResultsTreeNode) getElement(selection)).getElement());
					q.setChosenVars(new String[] { "?C2", "?M2" });
					q.setRecursiveVar("?M2");
					JQueryResultGraph g = q.getGraph();

					System.out.println("======================= Displaying Recursion Test =======================");
					JQueryResultNode[] x = g.getChildren();
					for (int i = 0; i < x.length; i++) {
						printChildren(x[i], 1);
					}
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error(e);
				}

				// Recursion Test (with filter)
				try {
					JQuery q = JQueryAPI.createQuery("(equals(!this,?target) ; overrides(?target,!this)),polyCalls(?M2, ?target, ?Ref),child(?C2,?M2)");
					JQuery filter = JQueryAPI.createQuery("re_name(!this,/ok/)");
					q.bind("!this", ((ResultsTreeNode) getElement(selection)).getElement());
					q.addFilter("regexp", filter, "?M2", JQuery.NoPosition);
					q.setChosenVars(new String[] { "?C2", "?M2" });
					q.setRecursiveVar("?M2");
					JQueryResultGraph g = q.getGraph();

					System.out.println("======================= Displaying Recursion Test (with filter) =======================");
					JQueryResultNode[] x = g.getChildren();
					for (int i = 0; i < x.length; i++) {
						printChildren(x[i], 1);
					}
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error(e);
				}
				System.out.println("======================= DONE =======================");
			}
		};
		// menu.appendToGroup(GROUP_DISPLAY, stupid);

		if (getElement(selection) instanceof QueryNode) {
			menu.appendToGroup(GROUP_DISPLAY, newTreeRootAction);
		}
		if (!view.isRootTree()) {
			menu.appendToGroup(GROUP_DISPLAY, refreshViewAction);
		}
		menu.appendToGroup(GROUP_DISPLAY, sortByCategory);
		menu.appendToGroup(GROUP_DISPLAY, showReturnType);
		menu.appendToGroup(GROUP_DISPLAY, showFieldType);
		menu.appendToGroup(GROUP_DISPLAY, showChildCount);
	}

	private void printChildren(JQueryResultNode n, int count) {
		JQueryResultNode[] x = n.getChildren();
		System.out.println(n);

		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < count; j++) {
				System.out.print("  ");
			}

			printChildren(x[i], count + 1);
		}
	}

	private void addNewTreeMenus(IMenuManager menu, final IStructuredSelection selection) {
		if (selection.size() == 0 || !(getElement(selection) instanceof QueryResultNode)) {
			return;
		}
		
		Object[] targets = findTargets(selection); 
		IMenuManager newTreeAction = new MenuManager("New Tree From Here...");
		menu.appendToGroup(GROUP_DISPLAY, newTreeAction);

		JQueryResultSet rs = null;
		try {
			SortedMap snippets = new TreeMap();

			JQuery q = JQueryAPI.createQuery("newTreeMenu(!this,?L,?Q,?V,?If,?Sf,?Ae)");
			q.bind(JQueryAPI.getThisVar(), targets);

			q.setChosenVars(new String[] { "?L", "?Q", "?V", "?If", "?Sf", "?Ae" });
			rs = q.execute();
			while (rs.hasNext()) {
				JQueryResult r = rs.next();

				Object[] path = (Object[]) r.get(0);
				String label = createLabel(path);
				String query = (String) r.get(1);
				Object[] vars = (Object[]) r.get(2);
				String inputFilter = (String) r.get(3);
				String selectionFilter = (String) r.get(4);
				String autoExpand = (String) r.get(5);

				snippets.put(label, new Object[] { path, query, vars, inputFilter, selectionFilter, autoExpand });
			}

			final Object[] tg = targets;
			// create menu from sorted list
			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
				Object[] o = (Object[]) snippets.get(iter.next());

				Object[] pathTerm = (Object[]) o[0];
				final String query = (String) o[1];
				final Object[] vars = (Object[]) o[2];
				final String inputFilter = (String) o[3];
				final String selectionFilter = (String) o[4];
				final Integer autoExpand = Integer.parseInt((String) o[5]);

				final String label = createLabel(pathTerm);

				// create the menu item
				Action action = new Action(label) {
					public void run() {
						try {
							List t = new ArrayList();
							for (int i = 0; i < vars.length; i++) {
								t.add(vars[i]);
							}

							QueryNode n = new QueryNode(query, label);
							n.getQuery().setChosenVars(t);
							JQueryTreeView v = viewPart.createNewTreeView(n, false);
							v.getBrowserUpdater().setInputFilter(inputFilter);
							v.getBrowserUpdater().setSelectionFilter(selectionFilter);
							v.link(new JQueryUpdateTarget[] { JQueryAPI.getUpdateTarget("Editor") });
							((QueryNode) v.getTreeRoot()).setAutoExpansionDepth(autoExpand.intValue());
						} catch (JQueryException e) {
							JQueryTreeBrowserPlugin.error("Creating tree view: ", e);
						} catch (PartInitException e) {
							JQueryTreeBrowserPlugin.error("Creating tree view: ", e);
						}
					}
				};

				// This here adds all them purrty choices
				createPath(newTreeAction, pathTerm, action, GROUP_DISPLAY);
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Generating new tree menu: ", e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
}
