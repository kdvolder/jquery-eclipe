package ca.ubc.jquery;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import ca.ubc.jquery.api.JQueryAPI;

public class FactbaseExtensionHandler implements IExtensionChangeHandler {

	private IExtensionTracker tracker;

	protected FactbaseExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addExtension(tracker, extensions[i]);
		}
	}

	protected JQueryAPI loadExtension(IExtension e) {
		IConfigurationElement[] configs = e.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			try {
				JQueryAPI delegate = (JQueryAPI) configs[i].createExecutableExtension("JQueryAPI");
				// register association between object and extension
				// with the tracker
				tracker.registerObject(e, delegate, IExtensionTracker.REF_WEAK);
				return delegate;
			} catch (CoreException ex) {
				JQueryBackendPlugin.error("Adding API extension: ", ex);
			}
		}
		return null;
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] configs = extension.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			// adds API to list of 'known-but-perhaps-not-initialized' apis
			JQueryBackendPlugin.installAPI(configs[i].getDeclaringExtension().getLabel(), extension);
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		// stop using objects associated with
		// the removed extension
		for (int i = 0; i < objects.length; i++) {
			JQueryBackendPlugin.removeAPI(objects[i]);
		}
	}
}
