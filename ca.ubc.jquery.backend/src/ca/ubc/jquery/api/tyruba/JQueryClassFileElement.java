package ca.ubc.jquery.api.tyruba;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFileElement;

public class JQueryClassFileElement extends JQueryFileElement {
	public static final long serialVersionUID = 1L;

	public JQueryClassFileElement(String parseFrom) {
		super(parseFrom);
	}

	public JQueryClassFileElement(String locationID, int startPos, int length) {
		super(locationID, startPos, length);
	}

	@Override
	public void openInEditor() throws JQueryException {
		try {
			IJavaElement cu = JavaCore.create(locationID);
			IEditorPart editor = JavaUI.openInEditor(cu);
			if (editor != null) {
				if (editor != null && editor instanceof ITextEditor) {
					((ITextEditor) editor).selectAndReveal(start, length);
				}
			}
		} catch (Exception e) {
			throw new JQueryTyRuBaException("Opening " + toString() + " ", e);
		}
	}
}
