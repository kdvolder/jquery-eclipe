package ca.ubc.jquery.resources.textfiles;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.resource.ImageDescriptor;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;
import ca.ubc.jquery.api.JQueryResourceStrategy;

public class TextFileStrategy implements JQueryResourceStrategy {
	private boolean matchExtension(IFile f) {
		String ext = f.getFileExtension();
		return ext!=null && ext.equalsIgnoreCase("rub");
	}

	public void buildWorkingSet(IAdaptable element, Collection c) throws CoreException {
		// TODO: This is very basic, just to get things working. Expand.
		if (rightType(element)) {
			c.add(element);
		}
	}
	
	public void applyStrategy(IAdaptable element) {
		// TODO: Remove me?
	}

	public boolean rightType(IAdaptable adaptable) {
		if (adaptable instanceof IFile) {
			IFile file = (IFile) adaptable;
			if (matchExtension(file)) {
				return true;
			}
		}
		return false;
	}

	public void addApplicableElementToCollection(IAdaptable element, Collection c) {
		if (!(element instanceof IResource)) {
			element = element.getAdapter(IResource.class);
		}
		if (element instanceof IResource) {
			IResource rsrc = (IResource) element;
			if (rsrc.isDerived()) {
				return;
			}
		}
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (matchExtension(file)) {
				c.add(element);
			}
		} else if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			try {
				addApplicableElementToCollection(folder, c);
			} catch (CoreException e) {
				System.err.println(e);
			}
		}
	}

	private void addApplicableElementToCollection(IFolder element, Collection c) throws CoreException {
		IResource[] r = element.members();
		for (int i = 0; i < r.length; i++) {
			addApplicableElementToCollection(r[i], c);
		}

	}

	public IAdaptable resourceDelta(IResourceDelta delta) {
		IResource res = delta.getResource();
		if (res.getType() == IResource.FILE) {
			IFile file = (IFile) res;
			if (matchExtension(file)) {
				return file;
			}
		}
		return null;
	}

	public JQueryResourceParser makeParser(IAdaptable adaptable, JQueryResourceManager manager) {
		return new TextFileParser(adaptable, manager);
	}

	public ImageDescriptor getImageDescriptor(String image) {
		// TODO Auto-generated method stub
		return null;
	}
}
