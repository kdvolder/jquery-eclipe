package ca.ubc.jquery.browser.menu;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryMenuResults;
import ca.ubc.jquery.api.JQueryObjectInputStream;
import ca.ubc.jquery.browser.actions.DoSubQueryAction;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;

public class NodeSpecificMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_NODE_SPECIFIC = "ca.ubc.jquery.JQueryTreeView.menu.result.node";

	public NodeSpecificMenuProvider(JQueryTreeViewer view, IMenuManager menuContext) {
		super(view, menuContext);
	}

	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		Object[] targets = findTargets(selection);

		if (getElement(selection) instanceof QueryNode) {
			queryAddAvailableMenu(menu, selection, targets);
			// TODO Does it make sense to allow "top queries" on a selected item?  
			//		This will require making some changes to set target process
			//
			//		} else if (targets.length == 0 && !view.isRootTree()) {
			//			Object target = ((IQueryResults) view.getRoot().getElement()).getTarget();
			//			if (target instanceof Object[]) {
			//				addAvailableMenu(menu, (Object[]) target);
			//			} else {
			//				addAvailableMenu(menu, new Object[] { target });
			//			}
		} else if (targets.length > 0) {
			nodeAddAvailableMenu(menu, targets);
		}
	}

	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_NODE_SPECIFIC));
	}

	private void queryAddAvailableMenu(IMenuManager menu, final IStructuredSelection selection, Object[] targets) {
		final QueryNode qn = (QueryNode) selection.getFirstElement();

		Action reExecuteQueryAction = new Action("Re-execute Query") {
			public void run() {
				view.doReExecuteAction(selection);
			}
		};

		Action editQueryAction = new Action("Edit Query") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) view.getSelection();
				QueryNode node = (QueryNode) selection.getFirstElement();
				view.doEditQuery(node);
			}
		};
		Action deleteQueryAction = new Action("Delete Query") {
			public void run() {
				view.doDeleteQueryAction(selection);
			}
		};

		menu.appendToGroup(GROUP_NODE_SPECIFIC, reExecuteQueryAction);
		if (selection.size() == 1) {
			menu.appendToGroup(GROUP_NODE_SPECIFIC, editQueryAction);
			//			menu.appendToGroup(GROUP_NODE_SPECIFIC, qn.getAutoRefreshAction());
			menu.appendToGroup(GROUP_NODE_SPECIFIC, new Action("Set Auto-Expansion") {
				public void run() {
					qn.setAutoExpansionDepth();
				}
			});
		}
		menu.appendToGroup(GROUP_NODE_SPECIFIC, deleteQueryAction);

		menu.appendToGroup(GROUP_NODE_SPECIFIC, new Separator());

		Action importAction = new Action("Import Query") {
			public void run() {
				importQueryNode(qn);
			}
		};

		Action exportAction = new Action("Export Query") {
			public void run() {
				exportQueryNode(qn);
			}
		};
		menu.appendToGroup(GROUP_NODE_SPECIFIC, importAction);
		menu.appendToGroup(GROUP_NODE_SPECIFIC, exportAction);

		if (selection.size() > 1) {
			importAction.setEnabled(false);
			exportAction.setEnabled(false);
		}

		//		// TODO: Get query groups working ... no, it's generally useless
		//		if (selection.size() > 1 && !detectedNestedGroups) {
		//			// add menu option for creating a group
		//			final QueryNode[] temp = new QueryNode[targets.length];
		//			boolean sameType = true;
		//			it = selection.iterator();
		//			for (int i = 0; it.hasNext() && sameType; i++) {
		//				IResultsTreeNode node = (IResultsTreeNode) it.next();
		//				if (!(node instanceof QueryNode) || (node instanceof QueryGroupNode)) {
		//					sameType = false;
		//				} else {
		//					temp[i] = (QueryNode) node;
		//				}
		//			}
		//
		//			if (sameType) {
		//				menu.appendToGroup(GROUP_NODE_GROUP, new Action("Create Group") {
		//					public void run() {
		//						ResultsTreeNode t = (ResultsTreeNode) element;
		//						if (t.getParent() == null) {
		//							t.addChild(new QueryGroupNode(temp));
		//						} else {
		//							t.getParent().addChild(new QueryGroupNode(temp));
		//						}
		//						treeViewer.refresh();
		//					}
		//				});
		//			}
	}

	private void nodeAddAvailableMenu(IMenuManager menu, Object[] targets) {
		try {
			JQueryMenuResults rs = JQueryAPI.menuQuery(targets);
			for (; rs.hasNext(); rs.next()) {
				String[] path = rs.getPath();
				String query = rs.getQuery();
				String[] vars = rs.getChosenVariables();

				// This here adds all them purrty choices
				Action action = new DoSubQueryAction(view, path[path.length - 1], query, vars);
				createPath(menu, path, action, GROUP_NODE_SPECIFIC);
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Error occurred while retrieving available sub-queries: ", e);
		}
	}

	private void exportQueryNode(QueryNode node) {
		FileDialog d = new FileDialog(JQueryTreeBrowserPlugin.getShell(), SWT.SAVE);
		d.setFilterNames(new String[] { "JQuery Files" });
		d.setFilterExtensions(new String[] { "*.jq" });

		String file = d.open();
		if (file != null) {
			try {
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(node.getQuery());
				os.close();
			} catch (IOException e) {
				JQueryTreeBrowserPlugin.error("Saving query: ", e);
			}
		}
	}

	private void importQueryNode(QueryNode node) {
		FileDialog d = new FileDialog(JQueryTreeBrowserPlugin.getShell(), SWT.OPEN);
		d.setFilterNames(new String[] { "JQuery Files" });
		d.setFilterExtensions(new String[] { "*.jq" });

		String file = d.open();
		if (file != null) {
			try {
				JQueryObjectInputStream is = new JQueryObjectInputStream(getClass().getClassLoader(), new FileInputStream(file));
				JQuery q = (JQuery) is.readObject();
				is.close();

				node.getQuery().replaceWith(q);
				view.doReExecuteAction(new StructuredSelection(node));
			} catch (IOException e) {
				JQueryTreeBrowserPlugin.error("Importing query: ", e);
			} catch (ClassNotFoundException e) {
				JQueryTreeBrowserPlugin.error("Importing query: ", e);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Importing query: ", e);
			}
		}
	}
}
