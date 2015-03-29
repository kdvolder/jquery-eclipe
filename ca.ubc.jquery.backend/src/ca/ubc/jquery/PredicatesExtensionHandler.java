package ca.ubc.jquery;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import ca.ubc.jquery.api.JQueryPredicateInstaller;

public class PredicatesExtensionHandler implements IExtensionChangeHandler {
	private static final String PREDICATE_INSTALLER = "PredicateInstaller";
	private static final String CLASS_ATTRIBUTE = "Class";
	
	private IExtensionTracker tracker;

	protected PredicatesExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		for (IExtension extn : ep.getExtensions()) {
			addExtension(tracker, extn);
		}
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement configElt : extension.getConfigurationElements()) {
			if (configElt.getName().equals(PREDICATE_INSTALLER)) {
				JQueryPredicateInstaller installer;
				try
				{
					installer = (JQueryPredicateInstaller) configElt.createExecutableExtension(CLASS_ATTRIBUTE);
				}
				catch (CoreException e)
				{
					throw new RuntimeException(e);
				}
				JQueryBackendPlugin.addPredicateInstaller(installer);
			}
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		for (IConfigurationElement configElt : extension.getConfigurationElements()) {
			if (configElt.getName().equals(PREDICATE_INSTALLER)) {
				JQueryPredicateInstaller installer;
				try
				{
					installer = (JQueryPredicateInstaller) configElt.createExecutableExtension(CLASS_ATTRIBUTE);
				}
				catch (CoreException e)
				{
					throw new RuntimeException(e);
				}
				JQueryBackendPlugin.removePredicateInstaller(installer);
			}
		}

		tracker.unregisterObject(extension);
	}
	
}
