package ca.ubc.jquery.gui;

import org.eclipse.jface.viewers.ISelection;

/**
 * A general purpose interface for a viewer which supports having JQueries executed
 * inside it.
 * 
 * @author lmarkle
 */
public interface JQueryViewer {
	/** Cancels all JQuery jobs (pending and running) */
	public void cancelAllJobs();

	/** Gets the Eclipse JavaModel representation of the selection */
	public ISelection getJMSelection();

	/** Gets the selected items from the viewer */
	public ISelection getSelection();
}
