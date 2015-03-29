package ca.ubc.jquery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.ubc.jquery.api.JQueryJavaVisitor;
import ca.ubc.jquery.api.JQueryFactGenerator;

public class JavaVisitorExtensionHandler implements IExtensionChangeHandler {
	private IExtensionTracker tracker;

	private Map extensions;

	public JavaVisitorExtensionHandler(IExtensionPoint ep, IExtensionTracker trackerP) {
		tracker = trackerP;
		extensions = new HashMap();

		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			addExtension(tracker, extensions[i]);
		}
	}

	public void install(CompilationUnit cu, JQueryFactGenerator generator, ca.ubc.jquery.engine.tyruba.java.CodeFactBucket bucket) {
		// make sure extensions are loaded
		Map toBeLoaded = new HashMap();
		for (Iterator it = extensions.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			if (e.getValue() instanceof IExtension) {
				toBeLoaded.put(e.getKey(), e.getValue());
			}
		}

		// load any not yet loaded extensions
		for (Iterator it = toBeLoaded.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			extensions.put(e.getKey(), loadExtension((IExtension) e.getValue()));
		}

		// apply visitors
		for (Iterator it = extensions.values().iterator(); it.hasNext();) {
			JQueryJavaVisitor jv = (JQueryJavaVisitor) it.next();
			if (jv != null) {
				jv.reset();
				jv.setGenerator(generator);
				jv.setBucket(bucket);
				cu.accept(jv);
			}
		}
	}

	private JQueryJavaVisitor loadExtension(IExtension e) {
		IConfigurationElement[] configs = e.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			try {
				JQueryJavaVisitor delegate = (JQueryJavaVisitor) configs[i].createExecutableExtension("JQueryJavaVisitor");
				// register association between object and extension
				// with the tracker
				tracker.registerObject(e, delegate, IExtensionTracker.REF_WEAK);
				return delegate;
			} catch (CoreException ex) {
				JQueryBackendPlugin.error("Adding Java Visitor extension: ", ex);
			}
		}
		return null;
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		extensions.put(extension.getLabel(), extension);
		//		IConfigurationElement[] configs = extension.getConfigurationElements();
		//		for (int i = 0; i < configs.length; i++) {
		//			// adds visitor to list of 'known-but-perhaps-not-initialized' visitors
		//			extensions.put(configs[i].getDeclaringExtension().getLabel(), extension);
		//		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		// FIXME: this looks like it should be a remove?
		extensions.put(extension.getLabel(), extension);
		tracker.unregisterObject(extension);
	}
}
