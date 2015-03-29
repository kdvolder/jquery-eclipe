package ca.ubc.jquery.browser.menu;

import java.util.Iterator;
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
import ca.ubc.jquery.browser.actions.ApplyAnnotationAction;
import ca.ubc.jquery.browser.actions.RemoveAnnotationAction;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryResultNode;

// Don't think this is used anywhere. Probably superseded by ca.ubc.jquery.refactoring.
@Deprecated
public class InsertAnnotationMenuProvider extends LayeredMenuProvider {
	public static final String GROUP_INSERT_ANNOTATION = "ca.ubc.jquery.JQueryTreeView.menu.insert.annotation";

	public InsertAnnotationMenuProvider(JQueryTreeViewer view, IMenuManager menu) {
		super(view, menu);
	}

	@Override
	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		if (selection.size() == 0 || !(getElement(selection) instanceof QueryResultNode)) {
			return;
		}
		
		Object[] targets = findTargets(selection);
		IMenuManager annotate = new MenuManager("Apply annotation...");
		IMenuManager deannotate = new MenuManager("Remove annotation...");
		menu.appendToGroup(GROUP_INSERT_ANNOTATION, annotate);
		menu.appendToGroup(GROUP_INSERT_ANNOTATION, deannotate);

		JQueryResultSet rs = null;
		try {
			SortedMap snippets = new TreeMap();

			JQuery q = JQueryAPI.createQuery("applyAnnotation(!this,?L,?A,?F,?T)");
			q.bind(JQueryAPI.getThisVar(), targets);

			q.setChosenVars(new String[] { "?L", "?A", "?F", "?T" });
			rs = q.execute();
			while (rs.hasNext()) {
				JQueryResult r = rs.next();

				Object[] path = (Object[]) r.get(0);
				String label = createLabel(path);
				String annotation = (String) r.get(1);
				String f = (String) r.get(2);
				String t = (String) r.get(3);

				snippets.put(label, new Object[] { path, annotation, f, t });
			}

			// create menu from sorted list
			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
				Object[] o = (Object[]) snippets.get(iter.next());

				Object[] pathTerm = (Object[]) o[0];
				final String annotation = (String) o[1];
				String label = createLabel(pathTerm);
				String f = (String) o[2];
				String t = (String) o[3];

				// create the menu item
				Action action = new ApplyAnnotationAction(label, targets, annotation, f, t);
				Action deaction = new RemoveAnnotationAction(label, targets, annotation, f, t);

				// This here adds all them purrty choices
				createPath(annotate, pathTerm, action, GROUP_INSERT_ANNOTATION);
				createPath(deannotate, pathTerm, deaction, GROUP_INSERT_ANNOTATION);
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Generating Insert Annotations menu: ", e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	@Override
	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_INSERT_ANNOTATION));
	}
}
