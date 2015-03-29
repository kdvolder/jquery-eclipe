/*
 * Created on Nov 30, 2008
 */
package ca.ubc.jquery.browser;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * To be implemented by extenders of the selectionListener extension point
 * @author gpothier
 */
public interface BrowserSelectionListener
{
	public void selected(IStructuredSelection selection);
}
