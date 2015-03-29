package ca.ubc.jquery.api;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;

import ca.ubc.jquery.JQueryBackendPlugin;

public class JQueryEditorSelectionListener implements ISelectionListener {

	private static JQueryEditorSelectionListener instance = null;

	private static int refCount = 0;

	private Job updateJob;

	private JQueryUpdateTarget editorTarget;

	private JQueryUpdateTarget editorElementsTarget;

	private JQueryEditorSelectionListener() {
		updateJob = null;

		// We enable the listener in the UI thread because need
		// UI stuff for this to work... perhaps there is a better solution somewhere?
		Job j = new UIJob("Enable editor selection") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!PlatformUI.getWorkbench().isClosing()) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPostSelectionListener(JQueryEditorSelectionListener.getInstance());
				}

				return Status.OK_STATUS;
			}
		};
		j.setSystem(true);
		j.schedule();
	}

	protected static JQueryEditorSelectionListener getInstance() {
		if (instance == null) {
			instance = new JQueryEditorSelectionListener();
		}
		return instance;
	}

	protected boolean isEnabled() {
		return refCount > 0;
	}

	public void enable() {
		refCount = refCount + 1;
		if (refCount == 1) {
			editorTarget = JQueryAPI.getUpdateTarget("Editor");
			editorElementsTarget = JQueryAPI.getUpdateTarget("Editor (Elements)");
		}
	}

	public void disable() {
		refCount = refCount - 1;
		if (refCount <= 0) {
			refCount = 0;
			//TODO remove listener
			// we can't remove it here because it's possible the workbench has shutdown when
			// we get there.  There must be a better way?  Maybe we just don't disable it?
			//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePostSelectionListener(this);

			// don't use these... why would anyone ever remove them?  Even if they're disabled?
			//			JQueryAPI.removeUpdateTarget(editorTarget);
			//			JQueryAPI.removeUpdateTarget(editorElementsTarget);

			editorTarget = null;
			editorElementsTarget = null;
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof FormEditor) {
			FormEditor ed = (FormEditor) part;
			if (ed.getActiveEditor() == null) {
				return;
			} else {
				part = ed.getActiveEditor();
			}
		}

		if (part instanceof ITextEditor) {
			ITextEditor v = (ITextEditor) part;
			ITextSelection t = (ITextSelection) selection;

			int o = t.getOffset();
			int l = t.getLength();
			String fileName = v.getTitleToolTip();

			// schedule job to update reverse source location stuff
			if (updateJob != null) {
				updateJob.cancel();
			}

			updateJob = new UpdateJob("JQuery Editor Selection Target Updater", fileName, o, l);
			updateJob.setRule(JQueryAPI.getRule());
			updateJob.setSystem(true);
			updateJob.schedule(50);
		}
	}

	private class UpdateJob extends Job {
		private String fileName;

		private int offset;

		private int length;

		public UpdateJob(String name, String fileName, int offset, int length) {
			super(name);
			this.fileName = fileName;
			this.offset = offset;
			this.length = length;
		}

		public IStatus run(IProgressMonitor monitor) {
			if (editorTarget == null && editorElementsTarget == null) {
				return Status.OK_STATUS;
			}

			Set contextElements = new HashSet();
			Set elements = new HashSet();

			try {
				JQueryAPI.getElementFromFile(fileName, offset, length, contextElements, elements);
				editorTarget.updateTarget(contextElements.toArray());
				editorElementsTarget.updateTarget(elements.toArray());
			} catch (JQueryException e) {
				JQueryBackendPlugin.error(e);
			}

			return Status.OK_STATUS;
		}
	}
}
