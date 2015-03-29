package ca.ubc.jquery.gui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.progress.UIJob;

public abstract class LabelUpdateJob extends UIJob {

	public final static int SchedulingDelay = 500;

	private final static String[] display = { ".", "..", "...", "" };

	private int p = 0;

	private String original;

	private boolean stop;

	public LabelUpdateJob(String name, String label) {
		super(name);

		// TODO Fix this Hack!
		//
		// We cut off any trailing '.' because this label provider adds them
		// and if the query gets refreshed before it's finished, the label gets all 
		// messed up.
		//
		// This hack prevents queries from having trailing '.' characters.  I don't think it
		// should be a really big deal anyway
		while (label.endsWith(".")) {
			label = label.substring(0, label.lastIndexOf("."));
		}

		original = label;
		stop = false;

		setSystem(true);

		addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult() != Status.OK_STATUS || stop) {
					stop = true;
					finish(getOriginal());
				}
			}
		});
	}

	private String getUpdateLabel() {
		return original + display[p];
	}

	private String getOriginal() {
		return original;
	}

	@Override
	protected void canceling() {
		stop = true;
	}

	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (!stop && !monitor.isCanceled()) {
			updateLabel(getUpdateLabel());
			p = (p + 1) % display.length;

			schedule(SchedulingDelay);
			return Status.OK_STATUS;
		} else {
			return Status.CANCEL_STATUS;
		}
	}

	protected void finish(final String original) {
		Job j = new UIJob("finish label update") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				updateLabel(original);
				return Status.OK_STATUS;
			}
		};
		j.setSystem(true);
		j.schedule();
	}

	protected abstract void updateLabel(String label);
}