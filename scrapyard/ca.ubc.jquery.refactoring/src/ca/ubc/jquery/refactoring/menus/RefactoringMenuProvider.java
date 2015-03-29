package ca.ubc.jquery.refactoring.menus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.browser.menu.LayeredMenuProvider;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.refactoring.actions.DoRefactoringAction;

public class RefactoringMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_REFACTORING = "ca.ubc.jquery.JQueryTreeView.menu.refactoring";

	private static final String REFACTORING_QUERY = "refactoring(!targets,?label,?query,?className,?args)";
	private static final String[] REFACTORING_QUERY_CHOSENVARS = { "?label", "?query", "?className", "?args" };
		
	public RefactoringMenuProvider (JQueryTreeViewer view, 
			IMenuManager menuContext) {
		super(view, menuContext);
	}
		
	@Override
	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_REFACTORING));
	}

	@Override
	public void addAvailableMenu (IMenuManager menu,
			IStructuredSelection selection) {
		if ( ! (getElement(selection) instanceof QueryNode) ) {
			Object[] targets = findTargets(selection);

			if (targets.length > 0) {
				IMenuManager refactoringsMenu = new MenuManager("Refactor");
				nodeAddAvailableRefactorings(refactoringsMenu, targets);
				if (!refactoringsMenu.isEmpty()) {
					menu.appendToGroup(GROUP_REFACTORING, refactoringsMenu);
				}
			}
		}
	}
	
	private void nodeAddAvailableRefactorings(IMenuManager menu, Object[] targets) {
		try {
			JQueryResultSet rs = getRefactorings(targets);
			
			// Keep track of what we add to menus to prevent duplication 
			Set<String> addedActionLabels = new HashSet<String>();

			while (rs.hasNext()) {
				JQueryResult result = rs.next();
				Object[] label = (Object[])result.get("?label");
				String labelStr = createLabel(label);
				
				if (!addedActionLabels.contains(labelStr)) {
					String query = (String)result.get("?query");
					String className = (String)result.get("?className");
					Object[] argObjs = (Object[])result.get("?args");
					String[] args = Arrays.copyOf(argObjs, argObjs.length, String[].class);

					Action action = new DoRefactoringAction((String)label[label.length - 1], view, query, className, args);
					createPath(menu, label, action, GROUP_REFACTORING);
					addedActionLabels.add(labelStr);
				}
			}
			
		} catch (JQueryException e) {
			throw new IllegalStateException("Error occurred while retrieving available refactorings: ", e);
		}
	}
	
	private JQueryResultSet getRefactorings (Object[] targets) throws JQueryException {
		if (targets == null) {
			targets = new Object[0];
		}
		JQuery query = JQueryAPI.createQuery(REFACTORING_QUERY);
		query.bind("!targets", targets);
		query.setChosenVars(REFACTORING_QUERY_CHOSENVARS);
		return query.execute();
	}
	
}
