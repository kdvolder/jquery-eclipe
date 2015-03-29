package ca.ubc.jquery.gui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.dialogs.JQueryQuickOutline;
import ca.ubc.jquery.gui.results.QueryNode;

/**
 * This class allows Eclipse like "Quick Outline" views.  It lets a user define a command
 * which can be bound to keystrokes to quickly display a JQuery results view based on
 * the current editing context.
 * 
 * There are several parameters to configure the query and display the results.
 * 
 * @author lmarkle
 */
public class QuickQueryCommandHandler extends AbstractHandler {

	public static final String QueryStringParam = "ca.ubc.jquery.browser.command.parameter.query";

	public static final String SelectedVarsParam = "ca.ubc.jquery.browser.command.parameter.selectedVars";

	public static final String InputFilterParam = "ca.ubc.jquery.browser.command.parameter.inputFilter";

	public static final String SelectionFilterParam = "ca.ubc.jquery.browser.command.parameter.selectionFilter";

	public static final String AutoExpandParam = "a.ubc.jquery.browser.command.parameter.autoExpand";

	public static final String RecursiveVarParam = "ca.ubc.jquery.browser.command.parameter.recursiveVar";

	public static final String ViewTitleParam = "ca.ubc.jquery.browser.command.parameter.viewTitle";

	public QuickQueryCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String queryString = event.getParameter(QueryStringParam);
		String selectedVars = event.getParameter(SelectedVarsParam);
		String inputFilter = event.getParameter(InputFilterParam);
		String selectionFilter = event.getParameter(SelectionFilterParam);
		String autoExpand = event.getParameter(AutoExpandParam);
		String recursiveVar = event.getParameter(RecursiveVarParam);
		String viewTitle = event.getParameter(ViewTitleParam);

		queryString = (queryString == null) ? "outlineView(!this,?X)" : queryString;
		selectedVars = (selectedVars == null) ? "?X" : selectedVars;
		inputFilter = (inputFilter == null) ? "type(!this)" : inputFilter;
		selectionFilter = (selectionFilter == null) ? "Callable(!this);field(!this)" : selectionFilter;
		autoExpand = (autoExpand == null) ? "0" : autoExpand;
		viewTitle = (viewTitle == null) ? "outline view" : viewTitle;

		String[] selectedVarsArray = selectedVars.split(",");
		for (int i = 0; i < selectedVarsArray.length; i++) {
			selectedVarsArray[i] = selectedVarsArray[i].trim();
		}

		try {
			QueryNode qn = new QueryNode(queryString, viewTitle);
			qn.getQuery().setChosenVars(selectedVarsArray);
			qn.setAutoExpansionDepth(Integer.parseInt(autoExpand));

			JQueryUpdateTarget targ = JQueryAPI.getUpdateTarget("Editor");
			qn.getQuery().bind(JQueryAPI.getThisVar(), targ.getTarget());

			if (recursiveVar != null) {
				qn.getQuery().setRecursiveVar(recursiveVar);
			}

			JQueryQuickOutline v = new JQueryQuickOutline(JQueryTreeBrowserPlugin.getShell(), qn, inputFilter, selectionFilter, targ);
			v.open();
		} catch (JQueryException ex) {
			JQueryTreeBrowserPlugin.error("Executing show quick view: ", ex);
		}

		return null;
	}
}
