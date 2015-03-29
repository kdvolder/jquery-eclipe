package ca.ubc.jquery.gui.tree;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.gui.JQueryTreeViewer;

public class TreeRegExpFilterJob extends UIJob {

	private Pattern exp;

	private JQueryTreeViewer tv;

	public TreeRegExpFilterJob(String name, JQueryTreeViewer tv, Pattern expression) {
		super(name);

		this.exp = expression;
		this.tv = tv;

		setSystem(true);
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (exp == null || ("").equals(exp)) {
			tv.resetFilters();
		} else {
			RegExpViewerFilter regExpFilter = new RegExpViewerFilter((ILabelProvider) tv.getLabelProvider(), (ITreeContentProvider) tv.getContentProvider(), exp);
			tv.setFilters(new ViewerFilter[] { regExpFilter });
		}
		tv.performNodeExpansion();

		return Status.OK_STATUS;
	}
}