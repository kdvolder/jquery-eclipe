package ca.ubc.jquery.refactoring.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.refactoring.QueryBasedRefactoring;
import ca.ubc.jquery.refactoring.QueryBasedRefactoringWizard;
import ca.ubc.jquery.refactoring.RefactoringTargetSet;

/**
 * Abstract base class for actions that perform a JQuery-based refactoring.
 * @author awjb
 */
public class DoRefactoringAction extends Action {
	protected final JQueryTreeViewer view;
	private final String query;
	private final Constructor<? extends QueryBasedRefactoring> refactoringConstructor;
	private final String[] args;
	
	public DoRefactoringAction(String name, JQueryTreeViewer view, String query, String className, String[] args) {
		super(name);
		this.view = view;
		this.query = query;
		this.args = args;
		this.refactoringConstructor = getConstructor(className);
	}
	
	@Override
	public void run() {
		// for each selected node
		IStructuredSelection selection = (IStructuredSelection) view.getSelection();
		Iterator<?> itr = selection.iterator();
		LinkedList<Object> inputs = new LinkedList<Object>();
		while (itr.hasNext()) {
			ResultsTreeNode node = (ResultsTreeNode) itr.next();
			inputs.add(node.getElement());
		}
		
		try {
			RefactoringTargetSet targets = new RefactoringTargetSet(args);

			for (Object input : inputs) {
				JQuery q = JQueryAPI.createQuery(query);
				q.bind(JQueryAPI.getThisVar(), input);
				q.setChosenVars(args);
				targets.addResults(q.execute());
			}

			executeRefactoring(targets);
		} catch (JQueryException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		}
	}
	
	/**
	 * Execute a given refactoring on a given set of results.
	 * @param refactoring Information about the refactoring to execute
	 * @param targets A collection of Maps from the variables in refactoring.getRefactoringArgs() to
	 * their values
	 */
	protected void executeRefactoring (RefactoringTargetSet targets) {
		try {
			QueryBasedRefactoring refact = refactoringConstructor.newInstance(targets);
			QueryBasedRefactoringWizard annotWizard = new QueryBasedRefactoringWizard(refact, 0);
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(annotWizard);

			try {
				String titleForFailedChecks = ""; //$NON-NLS-1$ 
				op.run(view.getControl().getShell(), titleForFailedChecks);
			} catch (InterruptedException irex) {
				// operation was cancelled 
			}
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		} catch (InstantiationException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		}
	}
	
	private Constructor<? extends QueryBasedRefactoring> getConstructor (String className) {
		try {
			return Class.forName(className).asSubclass(QueryBasedRefactoring.class).getConstructor(RefactoringTargetSet.class);
		} catch (ClassCastException e) {
			throw new IllegalStateException(className + " is not a JQuery-based refactoring", e);								
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e.getMessage(), e);						
		}
	}
}
