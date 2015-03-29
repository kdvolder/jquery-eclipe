package ca.ubc.jquery.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

/**
 * Refactoring wizard for JQuery-based refactorings. 
 * @author awjb
 */
public class QueryBasedRefactoringWizard extends RefactoringWizard {
	public QueryBasedRefactoringWizard(QueryBasedRefactoring refactoring, int flags) {
		super(refactoring, flags);
	}

	@Override
	protected void addUserInputPages() {
		UserInputWizardPage page = ((QueryBasedRefactoring)getRefactoring()).getWizardPage();
		if (page != null) {
			addPage(page);
		}
	}
}
