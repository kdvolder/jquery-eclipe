package ca.ubc.jquery.refactoring.menus;

import org.eclipse.jface.action.IMenuManager;

import ca.ubc.jquery.browser.menu.LayeredMenuProvider;
import ca.ubc.jquery.browser.menu.LayeredMenuProviderFactory;
import ca.ubc.jquery.gui.JQueryTreeViewer;

public class RefactoringMenuProviderFactory extends LayeredMenuProviderFactory {
	@Override
	public LayeredMenuProvider create(JQueryTreeViewer view,
			IMenuManager menuContext) {
		return new RefactoringMenuProvider(view, menuContext);
	}
}
