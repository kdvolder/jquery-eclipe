package ca.ubc.jquery;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.osgi.framework.Bundle;

public class IncludeRulesExtensionHandler implements IExtensionChangeHandler {
	private static final String RULES_DEFINITION = "RulesDefinition";
	private static final String FILE_ATTRIBUTE = "File";
	private static final String REQUIRED_ATTRIBUTE = "Required";
	
	private IExtensionTracker tracker;

	protected IncludeRulesExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;

		for (IExtension extn : ep.getExtensions()) {
			addExtension(tracker, extn);
		}
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement configElt : extension.getConfigurationElements()) {
			if (configElt.getName().equals(RULES_DEFINITION)) {
				String fileName = configElt.getAttribute(FILE_ATTRIBUTE);
				if (fileName != null) {
					String required = configElt.getAttribute(REQUIRED_ATTRIBUTE);
					if (required != null && required.equals("true")) {
						JQueryBackendPlugin.installIncludeRequiredRuleExtension(resolveFile(extension, fileName));
					} else {
						JQueryBackendPlugin.installIncludeOptionalRuleExtension(resolveFile(extension, fileName));						
					}
				}
			}
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		for (IConfigurationElement configElt : extension.getConfigurationElements()) {
			if (configElt.getName().equals(RULES_DEFINITION)) {
				String fileName = configElt.getAttribute(FILE_ATTRIBUTE);
				if (fileName != null) {
					String required = configElt.getAttribute(REQUIRED_ATTRIBUTE);
					if (required != null && required.equals("true")) {
						JQueryBackendPlugin.removeIncludeRequiredRuleExtension(resolveFile(extension, fileName));
					} else {
						JQueryBackendPlugin.removeIncludeOptionalRuleExtension(resolveFile(extension, fileName));						
					}
				}
			}
		}

		tracker.unregisterObject(extension);
	}
	
	private String resolveFile (IExtension e, String fileName) {
		Bundle t = Platform.getBundle(e.getContributor().getName());
		if (t != null) {
			try {
				File p = new File(FileLocator.resolve(t.getEntry("/")).getPath());
				return p.toString() + File.separatorChar + fileName;
			} catch (IOException exp) {
				JQueryBackendPlugin.error("Loading resource: ", exp);
			}
		}
		throw new IllegalStateException("Unable to resolve filesystem location of extension " + e.getUniqueIdentifier());
	}
}
