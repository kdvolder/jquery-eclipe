package ca.ubc.jquery.browser.menu;

import org.eclipse.jface.action.IMenuManager;

import ca.ubc.jquery.gui.JQueryTreeViewer;

/**
 * A factory for JQuery {@link LayeredMenuProvider}s that will be used to add
 * content to JQuery context menus. For use by plugins that
 * extend the menuProvider extension point of this plugin. (We can't provide
 * the LayeredMenuProvider subclasses to the extension point directly because
 * {@link org.eclipse.core.runtime.IConfigurationElement#createExecutableExtension IConfigurationElement.createExecutableExtension}
 * only works for classes with zero-argument constructors.)
 * @author awjb
 */
public abstract class LayeredMenuProviderFactory {
	public LayeredMenuProviderFactory () {
	}

	/**
	 * Factory method to create a new {@link LayeredMenuProvider}.
	 * @param view JQuery tree viewer control
	 * @param menuContext menu context in which this menu provider will be working
	 * @return A new instance of a subclass of {@link LayeredMenuProvider} associated with the
	 * specified {@code view} and {@code menuContext}
	 */ 
	abstract public LayeredMenuProvider create (JQueryTreeViewer view, 
			IMenuManager menuContext);
}
