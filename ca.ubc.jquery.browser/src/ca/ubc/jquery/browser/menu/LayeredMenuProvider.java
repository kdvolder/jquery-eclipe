package ca.ubc.jquery.browser.menu;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public abstract class LayeredMenuProvider extends JQueryMenuProvider {

	private IMenuManager menuContext;

	public LayeredMenuProvider(JQueryTreeViewer view, IMenuManager menuContext) {
		super(view);
		this.menuContext = menuContext;
	}

	protected String createLabel(Object[] elements) {
		String label = elements[0].toString();
		for (int i = 1; i < elements.length; i++) {
			label = label + "/" + elements[i];
		}
		return label;
	}

	protected void createPath(IMenuManager menu, Object[] subMenuPathandName, Action action, String nodeGroup) {
		Object[] pathPair = (Object[]) subMenuPathandName;
		ArrayList restPath = new ArrayList();
		for (int i = 1; i < pathPair.length; i++) {
			restPath.add(pathPair[i]);
		}

		String name = (String) ((Object[]) pathPair)[0];

		if (restPath.size() == 0) {
			// end of path, insert action here
			action.setText(name);
			if (menu == menuContext) {
				menu.appendToGroup(nodeGroup, action);
			} else {
				menu.add(action);
			}
		} else {
			IMenuManager firstMenu = (IMenuManager) menu.find(name);
			if (firstMenu == null) {
				firstMenu = new MenuManager(name, name);
				if (menu == menuContext) {
					menu.appendToGroup(nodeGroup, firstMenu);
				} else {
					menu.add(firstMenu);
				}
			}
			// recurse into sub menu
			createPath(firstMenu, restPath.toArray(), action, nodeGroup);
		}
	}

	protected Object[] findTargets(IStructuredSelection selection) {
		Object[] targets = new Object[selection.size()];
		boolean detectedNestedGroups = false;
	
		Iterator it = selection.iterator();
		for (int i = 0; it.hasNext() && !detectedNestedGroups; i++) {
			ResultsTreeNode node = (ResultsTreeNode) it.next();
			targets[i] = node.getElement();
			if (targets[i] instanceof Object[]) {
				detectedNestedGroups = true;
				targets = (Object[]) targets[i];
			}
		}
		
		return targets;
	}
}
