/*
 * Created on Aug 23, 2004
 */
package ca.ubc.jquery.resource.java.tyruba;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;

import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;
import ca.ubc.jquery.api.JQueryResourceStrategy;

/**
 * @author riecken
 */
public class ClassFileStrategy implements JQueryResourceStrategy {
	public boolean rightType(IAdaptable adaptable) {
		return adaptable instanceof IClassFile;
	}

	public void buildWorkingSet(IAdaptable element, Collection c) throws CoreException {
	}

	public void addApplicableElementToCollection(IAdaptable element, Collection c) {
		if (element instanceof IJavaProject) {
			addApplicableElementToCollection((IJavaProject) element, c);
		} else if (element instanceof IPackageFragmentRoot) {
			addApplicableElementToCollection((IPackageFragmentRoot) element, c);
		} else if (element instanceof IPackageFragment) {
			addApplicableElementToCollection((IPackageFragment) element, c);
		} else if (element instanceof IClassFile) {
			c.add(element);
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (("class").equalsIgnoreCase(file.getFileExtension())) {
				IJavaElement je = JavaCore.create(file);
				if (je != null) {
					c.add(je);
				}
			}
		}
	}

	private void addApplicableElementToCollection(IPackageFragment fragment, Collection c) {
		try {
			IClassFile classFiles[] = fragment.getClassFiles();
			for (int j = 0; j < classFiles.length; j++) {
				c.add(classFiles[j]);
			}
		} catch (JavaModelException e) {
			JQueryBackendPlugin.traceQueries("processWorkingSetElement: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void addApplicableElementToCollection(IPackageFragmentRoot pfr, Collection c) {
		try {
			IJavaElement fragments[] = pfr.getChildren();

			for (int i = 0; i < fragments.length; i++) {
				addApplicableElementToCollection((IPackageFragment) fragments[i], c);
			}
		} catch (JavaModelException e) {
			JQueryBackendPlugin.traceQueries("processWorkingSetElement: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void addApplicableElementToCollection(IJavaProject project, Collection c) {
		try {
			IPackageFragmentRoot pfrs[] = project.getPackageFragmentRoots();
			for (int i = 0; i < pfrs.length; i++) {
				addApplicableElementToCollection(pfrs[i], c);
			}
		} catch (JavaModelException e) {
			JQueryBackendPlugin.traceQueries("processWorkingSetElement: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public IAdaptable resourceDelta(IResourceDelta delta) {
		// TODO: see what should go here, right now, I'm just preserving previous behaviour.
		// at least this should handle .class files, it really should handle .jar files to see if
		// the stuff in the jar file changed, but that would be very tricky to implement.
		return null;
	}

	public JQueryResourceParser makeParser(IAdaptable adaptable, JQueryResourceManager manager) {
		return new ClassFileBucket(adaptable, manager);
	}

	public ImageDescriptor getImageDescriptor(String image) {
		return null;
	}
}
