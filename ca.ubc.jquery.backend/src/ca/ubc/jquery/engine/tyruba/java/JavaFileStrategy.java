/*
 * Created on Aug 23, 2004
 */
package ca.ubc.jquery.engine.tyruba.java;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RuleBaseBucket;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;
import ca.ubc.jquery.api.JQueryResourceStrategy;

/**
 * @author riecken
 */
public class JavaFileStrategy implements JQueryResourceStrategy {

	public boolean rightType(IAdaptable adaptable) {
		return adaptable instanceof ICompilationUnit;
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
		} else if (element instanceof ICompilationUnit) {
			c.add(element);
			System.out.println(((ICompilationUnit) element).getParent().getParent());
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (("java").equalsIgnoreCase(file.getFileExtension())) {
				IJavaElement je = JavaCore.create(file);
				if (je != null) {
					c.add(je);
				}
			}
		}
	}

	private void addApplicableElementToCollection(IPackageFragment fragment, Collection c) {
		try {
			ICompilationUnit compUnits[] = fragment.getCompilationUnits();
			for (int i = 0; i < compUnits.length; i++) {
				addApplicableElementToCollection(compUnits[i], c);
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
		IResource resource = delta.getResource();
		if (resource.getType() == IResource.FILE && ("java").equalsIgnoreCase(resource.getFileExtension())) {
			IJavaElement element = JavaCore.create(resource);
			return element;
		} else {
			return null;
		}
	}

	public boolean openInEditor(JQueryFileElement loc) {
		try {
			IJavaElement cu = JavaCore.create(loc.locationID);
			IEditorPart editor = JavaUI.openInEditor(cu);
			if (editor != null) {
				if (editor != null && editor instanceof ITextEditor) {
					((ITextEditor) editor).selectAndReveal(loc.start, loc.length);
				}
				return true;
			}
		} catch (PartInitException e) {
			JQueryBackendPlugin.error(e);
		} catch (JavaModelException e) {
			JQueryBackendPlugin.error(e);
		}
		return false;
	}

	public JQueryResourceParser makeParser(IAdaptable adaptable, JQueryResourceManager manager) {
		return new CompilationUnitBucket(adaptable, manager);
	}

	public ImageDescriptor getImageDescriptor(String image) {
		return null;
	}
}
