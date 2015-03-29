package ca.ubc.jquery.browser.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.views.JQueryTreeView;

public class OpenJQueryViewAction extends AbstractHandler implements IEditorActionDelegate, IViewActionDelegate, IObjectActionDelegate {

	private IEditorPart editorPart;

	private IWorkbenchPart workbenchPart;

	private IViewPart viewPart;

	public OpenJQueryViewAction() {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
		workbenchPart = null;
		viewPart = null;
	}

	public void init(IViewPart view) {
		editorPart = null;
		workbenchPart = null;
		viewPart = view;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		editorPart = null;
		workbenchPart = targetPart;
		viewPart = null;
	}

	public void run(IAction action) {
		try {
			Object[] value = null;
			IWorkbenchPart origin = null;

			if (editorPart != null) {
				origin = editorPart;
				if (editorPart.getSite().getSelectionProvider().getSelection() instanceof ITextSelection) {
					ITextSelection sel = (ITextSelection) editorPart.getEditorSite().getSelectionProvider().getSelection();
					Set c = new HashSet();
					Set e = new HashSet();
					FileEditorInput b = (FileEditorInput) editorPart.getEditorInput();

					JQueryAPI.getElementFromFile(b.getToolTipText(), sel.getOffset(), sel.getLength(), c, e);
					value = c.toArray();
				}
			} else if (viewPart != null) {
				origin = viewPart;
				if (viewPart.getSite().getSelectionProvider().getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) viewPart.getSite().getSelectionProvider().getSelection();
					value = ss.toArray();
					for (int i = 0; i < value.length; i++) {
						if (value[i] instanceof IJavaElement) {
							value[i] = JQueryAPI.getElementFromJavaModel((IJavaElement) value[i]);
						}
					}
				}
			} else if (workbenchPart != null) {
				origin = workbenchPart;
				if (workbenchPart.getSite().getSelectionProvider().getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) workbenchPart.getSite().getSelectionProvider().getSelection();
					value = ss.toArray();
					for (int i = 0; i < value.length; i++) {
						if (value[i] instanceof IJavaElement) {
							value[i] = JQueryAPI.getElementFromJavaModel((IJavaElement) value[i]);
						}
					}
				}
			}

			if (value != null && value[0] != null) {
				QueryNode qn = new QueryNode(JQueryAPI.getIdentityQuery(), "Open: " + JQueryAPI.getElementLabel(value[0]));
				qn.getQuery().bind(JQueryAPI.getThisVar(), value);
				JQueryTreeView.createTreeView(origin, qn, false);
			}
		} catch (PartInitException ex) {
			JQueryTreeBrowserPlugin.error("Opening from editor", ex);
		} catch (JQueryException ex) {
			JQueryTreeBrowserPlugin.error("Opening from editor", ex);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		run(null);
		return null;
	}
}
