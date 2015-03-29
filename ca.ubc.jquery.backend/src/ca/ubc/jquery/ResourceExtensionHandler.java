package ca.ubc.jquery;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.osgi.framework.Bundle;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryResourceStrategy;

public class ResourceExtensionHandler implements IExtensionChangeHandler {
	private IExtensionTracker tracker;

	protected ResourceExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			addExtension(tracker, extensions[i]);
		}
	}

	protected JQueryResourceStrategy loadExtension(IExtension e, List files) {
		IConfigurationElement[] configs = e.getConfigurationElements();
		JQueryResourceStrategy delegate = null;

		// check to see if extension is loaded
		Object[] search = tracker.getObjects(e);
		for (int i = 0; i < search.length; i++) {
			if (search[i] instanceof JQueryResourceStrategy) {
				delegate = (JQueryResourceStrategy) search[i];
			} else {
				JQueryAPI.installDefinitionFile((File) search[i]);
			}
		}

		// this only happens if we have already loaded the extension
		if (delegate != null) {
			return delegate;
		}

		// extension is not loaded, so load it (possibly a second time?)
		for (int i = 0; i < configs.length; i++) {
			try {
				String f = configs[i].getAttribute("File");
				if (f != null) {
					Bundle t = Platform.getBundle(e.getContributor().getName());
					if (t != null) {
						try {
							File p = new File(FileLocator.resolve(t.getEntry("/")).getPath());
							p = new File(p.toString() + File.separatorChar + f);
							tracker.registerObject(e, p, IExtensionTracker.REF_WEAK);
							files.add(p);
						} catch (IOException exp) {
							JQueryBackendPlugin.error("Loading resource: ", exp);
						}
					}
				} else {
					delegate = (JQueryResourceStrategy) configs[i].createExecutableExtension("JQueryResource");
					// register association between object and extension
					// with the tracker
					tracker.registerObject(e, delegate, IExtensionTracker.REF_WEAK);
				}
			} catch (CoreException ex) {
				JQueryBackendPlugin.error("Adding API extension: ", ex);
			}
		}
		return delegate;
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] configs = extension.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			// adds API to list of 'known-but-perhaps-not-initialized' resources
			JQueryBackendPlugin.installResource(configs[i].getDeclaringExtension().getLabel(), extension);
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		IConfigurationElement[] configs = extension.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			JQueryBackendPlugin.removeResource(configs[i].getDeclaringExtension().getLabel());
		}

		for (int i = 0; i < objects.length; ++i) {
			if (objects[i] instanceof File) {
				JQueryAPI.removeDefinitionFile((File) objects[i]);
			}
		}
		tracker.unregisterObject(extension);
	}
}
