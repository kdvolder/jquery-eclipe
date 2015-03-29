package ca.ubc.jquery.resource.xml;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;
import ca.ubc.jquery.api.JQueryResourceStrategy;

public class XMLStrategy implements JQueryResourceStrategy {
	public XMLStrategy() {
	}

	public void buildWorkingSet(IAdaptable element, Collection c) throws CoreException {
		// TODO: This is very basic, just to get things working. Expand.
		if (rightType(element)) {
			c.add(element);
		}
	}
	protected boolean rightExtension(String ext) {
		return ("xml").equalsIgnoreCase(ext) || ("exsd").equalsIgnoreCase(ext) || ("xsd").equalsIgnoreCase(ext);
	}

	protected boolean rightFile(IFile f) {
		if (rightExtension(f.getFileExtension())) {
			return true;
		} else {
			return false;
		}
	}

	public void applyStrategy(IAdaptable element) {
	}

	/**
	 * Returns true if the adaptable passed in is of a type applicable to this strategy.
	 * This method is called by JQuery when it needs to know which strategy to use for a
	 * given file.
	 */
	public boolean rightType(IAdaptable element) {
		if (element instanceof IFile) {
			return rightFile((IFile) element);
		} else {
			return false;
		}
	}

	/**
	 * Checks the IAdaptable, and adds any applicable resources to the collection.
	 * This method is called by JQuery when it is looking through a working set and
	 * deciding which elements it has to create buckets for.
	 */
	public void addApplicableElementToCollection(IAdaptable element, Collection c) {
		if (rightType(element)) {
			c.add(element);
		}
	}

	/**
	 * Returns the IAdaptable changed by the resource delta.
	 * This method is called by JQuery when it determines that a change has occured to 
	 * a resource.  Return an IAdaptable if the resource delta is applicable
	 * to the file type that this strategy is for.
	 */
	public IAdaptable resourceDelta(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (resource.getType() == IResource.FILE && rightExtension(resource.getFileExtension())) {
			return resource;
		} else {
			return null;
		}
	}

	public ImageDescriptor getImageDescriptor(String image) {
		return Activator.getImageDescriptor(image);
	}

	public JQueryResourceParser makeParser(IAdaptable adaptable, JQueryResourceManager manager) {
		return new JQXMLParser(adaptable, manager);
	}
}
