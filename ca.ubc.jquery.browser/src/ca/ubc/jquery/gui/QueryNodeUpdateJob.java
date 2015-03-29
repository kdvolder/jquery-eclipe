package ca.ubc.jquery.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * This class is responsible for updating a QueryNode inside a given viewer.  It will
 * execute the query and update the results tree.
 *
 * To use it you must set the target (setQueryTarget) then call schedule.
 * 
 * It has support for input/selection filters as well to assist when updating a view
 * rooted around a particular query.
 * TODO: Perhaps handling input/selection filtering should be done by the view, not the 
 * updater?  The code is a little difficult to separate currently. 
 * 
 * @author lmarkle
 */
public class QueryNodeUpdateJob extends Job {
	private QueryNode query;

	private JQueryTreeViewer view;

	private String selectionFilter;

	private String inputFilter;

	private List itemSelection;

	private List selectedItems;

	private List previousSelection;

	private Object[] target;

	private boolean updateSelection;

	private Job updater;

	public QueryNodeUpdateJob(String name, JQueryTreeViewer view) {
		super(name);

		this.view = view;
		selectedItems = new ArrayList();
		previousSelection = new ArrayList();
		itemSelection = new ArrayList();

		updateSelection = true;

		selectionFilter = "";
		inputFilter = "";
	}

	public void enableItemSelection() {
		updateSelection = true;
	}

	public void disableItemSelection() {
		updateSelection = false;
	}

	public void setSelectionFilter(String filter) {
		selectionFilter = filter;
	}

	public void setInputFilter(String filter) {
		inputFilter = filter;
	}

	public void setQuery(QueryNode q) {
		query = q;
	}

	public void setQueryTarget(Object o) {
		List t = new ArrayList();
		if (o instanceof Object[]) {
			Object[] to = (Object[]) o;
			for (int i = 0; i < to.length; i++) {
				t.add(to[i]);
			}
		} else {
			t.add(o);
		}
		
		itemSelection = t;
	}

	public String getInputFilter() {
		return inputFilter;
	}

	public String getSelectionFilter() {
		return selectionFilter;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		//
		// Note that there is lots of room for optimization in this method. For starters,
		// instead of running each element of the new targets through the query, we could
		// bind the whole thing to a list and append a member/2 predicate to the query.  Of
		// course this is somewhat language dependent but perhaps the API should adjust to
		// handle this case better?
		//
		// This optimization would apply to both the input and selection filters.
		//
		// A further optimization may be possible if we store the raw results from setQueryTarget()
		// and make a comparison here to see if the last update actually changed the value.  This would
		// be a little sloppy coding but it may provide some performance improvements.  It would also
		// avoid applying filters to a result we have already obtained.
		//
		// In general this method seems to complicated and long to be a good implementation.
		
		// add items for selection highlighting
		//
		// execute selection query on each element
		JQuery fq = null;
		selectedItems.clear();
		if (!selectionFilter.equals("")) {
			try {
				fq = JQueryAPI.createQuery(selectionFilter);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Malformed selection filter", e);
			}
			for (Iterator it = itemSelection.iterator(); fq != null && it.hasNext();) {
				Object t = it.next();
				try {
					fq.bind(JQueryAPI.getThisVar(), t);
					if (fq.execute().hasNext()) {
						ResultsTreeNode n = new QueryResultNode(t, "");
						n.setParent(view.getTreeRoot());
						selectedItems.add(n);
					}
				} catch (Exception e) {
				}
			}
		}

		// filter items so they don't crash the query
		//
		// execute the filter query on each item
		if (!inputFilter.equals("")) {
			fq = null;
			try {
				fq = JQueryAPI.createQuery(inputFilter);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Malformed link filter", e);
			}
			for (Iterator it = itemSelection.iterator(); fq != null && it.hasNext();) {
				Object t = it.next();
				try {
					fq.bind(JQueryAPI.getThisVar(), t);
					if (!fq.execute().hasNext()) {
						it.remove();
					}
				} catch (Exception e) {
					it.remove();
				}
			}
		}

		if (!previousSelection.equals(itemSelection)) {
			// generate target for query of remaining selected items after the filter
			target = new Object[itemSelection.size()];
			Iterator it = itemSelection.iterator();
			for (int i = 0; it.hasNext(); i++) {
				target[i] = it.next();
			}
		}

		Job j = null;
		// don't waste time querying if don't have any items to target
		if (!previousSelection.equals(itemSelection) && target != null && target.length > 0) {
			try {
				query.getQuery().bind(JQueryAPI.getThisVar(), target);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Setting query target: ", e);
			}
			j = view.execute(query);
			previousSelection = (List) ((ArrayList) itemSelection).clone();
		}

		if (updateSelection) {
			if (j == null) {
				updateSelection();
			} else {
				j.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						updateSelection();
					}
				});
			}
		}

		return Status.OK_STATUS;
	}

	@Override
	public void canceling() {
		updater.cancel();
	}

	private void updateSelection() {
		if (updater != null) {
			updater.cancel();
		}

		updater = new UIJob("Refresh linked browser selection") {
			public IStatus runInUIThread(IProgressMonitor mon) {
				// update selection
				view.setSelection(new StructuredSelection(selectedItems));
				return Status.OK_STATUS;
			}
		};
		updater.setSystem(true);
		updater.schedule();
	}
}
