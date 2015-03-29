package ca.ubc.jquery.api;

import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import ca.ubc.jquery.JQueryBackendPlugin;

/**
 * Represents a location in a file.
 */
public class JQueryFileElement implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	public String locationID;

	public int start;

	public int length;

	public String toString() {
		return "\"(" + locationID + "," + start + "," + length + ")" + "\"" + "::ca.ubc.jquery.ast.JQueryFileElement";
	}

	/**
	 * By providing this constructor, it becomes possible to read back and parse this kind of Object back into TyRuBa terms from text produced by the toString method below.
	 */
	public JQueryFileElement(String parseFrom) {
		int lparAt = parseFrom.indexOf('(');
		int comma1 = parseFrom.indexOf(',', lparAt);
		int comma2 = parseFrom.indexOf(')', comma1 + 1);

		this.locationID = parseFrom.substring(0, lparAt);
		this.start = Integer.parseInt(parseFrom.substring(lparAt + 1, comma1));
		this.length = Integer.parseInt(parseFrom.substring(comma1 + 1, comma2));
	}

	public JQueryFileElement(String locationID, int startPos, int length) {
		this.locationID = locationID;
		this.start = startPos;
		this.length = length;
	}

	public int hashCode() {
		if (locationID != null) {
			return 37 * locationID.hashCode() + start + length;// + position;
		} else {
			return start + length;// + position;
		}
	}

	public boolean equals(Object object) {
		if (object instanceof JQueryFileElement) {
			JQueryFileElement other = (JQueryFileElement) object;
			boolean result = (other.locationID != null && other.locationID.equals(this.locationID));
			result = result && (other.start == start);
			result = result && (other.length == length);
			return result;
		} else {
			return super.equals(object);
		}
	}

	public int compareTo(Object object) {
		if (object instanceof JQueryFileElement) {
			JQueryFileElement other = (JQueryFileElement) object;
			if (locationID == other.locationID || (locationID != null && locationID.equals(other.locationID))) {
				if (start == other.start)
					return length - other.length;
				else
					return start - other.start;
			} else
				return locationID.compareTo(other.locationID);
		} else {
			return getClass().toString().compareTo(object.getClass().toString());
		}
	}

	public void openInEditor() throws JQueryException {
		try {
			IFile file = getSourceFile();
			if (file != null) {
				// open a regular text file (or source file)
				IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
				if (editor != null && editor instanceof ITextEditor) {
					((ITextEditor) editor).selectAndReveal(start, length);
				}

				// open a form editor (e.g., XML file)
				if (editor instanceof FormEditor) {
					FormEditor ed = (FormEditor) editor;

					ed.setActivePage(file.getName());
					if (ed.getActiveEditor() == null) {
						JQueryBackendPlugin.error("Select the text editor tab and try again");
					} else {
						((ITextEditor) ed.getActiveEditor()).selectAndReveal(start, length);
					}
				}
			}

			// check for opening a class file			
			IJavaElement cu = JavaCore.create(locationID);
			IEditorPart editor = JavaUI.openInEditor(cu);
			if (editor != null) {
				if (editor != null && editor instanceof ITextEditor) {
					((ITextEditor) editor).selectAndReveal(start, length);
				}
			}
		} catch (PartInitException e) {
			throw new JQueryException("Opening location: " + toString() + " ", e);
		} catch (JavaModelException e) {
			throw new JQueryException("Opening location: " + toString() + " ", e);
		}
	}

	public IFile getSourceFile() {
		IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(locationID));
		if (file != null && file.getType() == IResource.FILE) {
			return (IFile) file;
		}
		return null;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
}
