package ca.ubc.jquery;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.viewers.IStructuredSelection;

import ca.ubc.jquery.browser.BrowserSelectionListener;
import ca.ubc.jquery.browser.menu.LayeredMenuProviderFactory;

public class SelectionListenerExtensionHandler implements IExtensionChangeHandler {
	private static final String LISTENER_ELEMENT_NAME = "Listener";
	private static final String CLASS_ATTRIBUTE_NAME = "class";
	
	private IExtensionTracker tracker;

	protected SelectionListenerExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addExtension(tracker, extensions[i]);
		}
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement config : extension.getConfigurationElements()) {
			if (config.getName().equals(LISTENER_ELEMENT_NAME)) {
				try {
					BrowserSelectionListener listener = (BrowserSelectionListener)config.createExecutableExtension(CLASS_ATTRIBUTE_NAME);
					tracker.registerObject(extension, listener.getClass().getName(), 
							IExtensionTracker.REF_WEAK);
					JQueryTreeBrowserPlugin.addSelectionListener(listener);
				} catch (CoreException e) {
					JQueryTreeBrowserPlugin.error(e.getMessage(), e);
				}
			}
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		// stop using objects associated with
		// the removed extension
		
		// TODO: implement (if needed...)
		throw new RuntimeException("We got: "+Arrays.asList(objects));
	}
	
	/**
	 * Helper method to fire a selection event to all listeners.
	 */
	public static void fireSelected(IStructuredSelection selection) {
		for (BrowserSelectionListener listener : JQueryTreeBrowserPlugin.getBrowserSelectionListeners()) {
			listener.selected(selection);
		}
	}
}
