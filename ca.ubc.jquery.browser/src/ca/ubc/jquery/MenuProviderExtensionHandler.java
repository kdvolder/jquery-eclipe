package ca.ubc.jquery;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import ca.ubc.jquery.browser.menu.LayeredMenuProviderFactory;

public class MenuProviderExtensionHandler implements IExtensionChangeHandler {
	private static final String PROVIDER_ELEMENT_NAME = "Provider";
	private static final String FACTORY_CLASS_ATTRIBUTE_NAME = "factoryClass";
	
	private IExtensionTracker tracker;

	protected MenuProviderExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addExtension(tracker, extensions[i]);
		}
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement config : extension.getConfigurationElements()) {
			if (config.getName().equals(PROVIDER_ELEMENT_NAME)) {
				try {
					LayeredMenuProviderFactory menuProvider = (LayeredMenuProviderFactory)config.createExecutableExtension(FACTORY_CLASS_ATTRIBUTE_NAME);
					tracker.registerObject(extension, menuProvider.getClass().getName(), 
							IExtensionTracker.REF_WEAK);
					JQueryTreeBrowserPlugin.installMenuProviderExtension(menuProvider.getClass().getName(), menuProvider);
				} catch (CoreException e) {
					JQueryTreeBrowserPlugin.error(e.getMessage(), e);
				}
			}
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		// stop using objects associated with
		// the removed extension
		for (Object obj : objects) {
			JQueryTreeBrowserPlugin.removeMenuProviderExtension((String)obj);
		}
	}
}
