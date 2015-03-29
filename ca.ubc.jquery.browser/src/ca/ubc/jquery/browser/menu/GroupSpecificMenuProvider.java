package ca.ubc.jquery.browser.menu;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryResultGroupNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class GroupSpecificMenuProvider extends JQueryMenuProvider {
	public static final String GROUP_NODE_GROUP = "ca.ubc.jquery.JQueryTreeView.menu.result.node.group";

	private boolean detectedNestedGroups;

	private Object[] targets;

	public GroupSpecificMenuProvider(JQueryTreeViewer view) {
		super(view);

		targets = null;
	}

	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		targets = new Object[selection.size()];
		detectedNestedGroups = false;

		Iterator it = selection.iterator();
		for (int i = 0; it.hasNext() && !detectedNestedGroups; i++) {
			ResultsTreeNode node = (ResultsTreeNode) it.next();
			targets[i] = node.getElement();
			if (targets[i] instanceof Object[]) {
				detectedNestedGroups = true;
				targets = (Object[]) targets[i];
			}
		}

		//		addAvailableMenu(menu, selection, element);
		if (getElement(selection) instanceof QueryResultGroupNode) {
			gaddAvailableMenu(menu, selection);
		} else {
			caddAvailableMenu(menu, selection);
		}
	}

	private void gaddAvailableMenu(IMenuManager menu, final IStructuredSelection selection) {
		menu.appendToGroup(GROUP_NODE_GROUP, new Action("Remove group") {
			public void run() {
				ResultsTreeNode t = getElement(selection);
				if (t.getParent() == null) {
					t.removeChild(getElement(selection));
				} else {
					t.getParent().removeChild(getElement(selection));
				}
				// refresh whole tree
				view.refresh();
			}
		});
	}

	private void caddAvailableMenu(IMenuManager menu, final IStructuredSelection selection) {
		if (selection.size() > 1 && !detectedNestedGroups) {
			// add menu option for creating a group
			menu.appendToGroup(GROUP_NODE_GROUP, new Action("Create Group") {
				public void run() {
					ResultsTreeNode t = getElement(selection);
					t.getParent().addChild(new QueryResultGroupNode(targets));
					// refresh whole tree
					view.refresh();
				}
			});
		}
	}

	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_NODE_GROUP));
	}
}
