package ca.ubc.jquery.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionDelegate;

import ca.ubc.jquery.api.JQueryAPI;

public class CreateWorkingSet extends ActionDelegate {

	private List projects;

	public CreateWorkingSet() {
		projects = new ArrayList();
	}

	@Override
	public void run(IAction action) {
		if (projects.isEmpty()) {
			return;
		}

		for (Iterator it = projects.iterator(); it.hasNext();) {
			IProject p = (IProject) it.next();
			JQueryAPI.getFactBase().addProject(p);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		projects = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;

			for (Iterator it = ss.iterator(); it.hasNext();) {
				projects.add(it.next());
			}
		}

		action.setEnabled(!projects.isEmpty());
	}
}
