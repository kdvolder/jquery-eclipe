package ca.ubc.jquery.gui.tree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.LabelUpdateJob;
import ca.ubc.jquery.gui.results.NoResultNode;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.query.QueryStatus;

public class ResultsTreeBuilderJob extends Job {
	private class BuilderJobChangeAdapter extends JobChangeAdapter {
		private QueryNode queryNode;

		private StructuredViewer viewer;

		public BuilderJobChangeAdapter(StructuredViewer viewer, QueryNode node) {
			this.viewer = viewer;
			this.queryNode = node;
		}

		@Override
		public void aboutToRun(IJobChangeEvent e) {
			queryNode.beginUpdateResults();
		}

		@Override
		public void done(IJobChangeEvent e) {
			nodeTitleUpdater.cancel();

			queryNode.midUpdateResults();
			UIJob updater = new UIJob("Finish tree update") {
				public IStatus runInUIThread(IProgressMonitor m) {
					if (queryNode.getParent() != null) {
						queryNode.getParent().setExpandstate(ResultsTreeNode.NodeExpanded);
					}
					queryNode.setExpandstate(ResultsTreeNode.NodeExpanded);
					viewer.refresh(queryNode);

					queryNode.endUpdateResults();

					if (viewer instanceof JQueryTreeViewer) {
						JQueryTreeViewer tv = (JQueryTreeViewer) viewer;
						tv.expandToLevel(queryNode, queryNode.getAutoExpandDepth());
						tv.performNodeExpansion(queryNode);
					}

					return Status.OK_STATUS;
				}
			};
			updater.setSystem(true);
			updater.setPriority(Job.INTERACTIVE);
			updater.schedule();
		}
	};

	private QueryNode query;

	private ResultsTreeNode target;

	private QueryStatus qs;

	/** The variables that determine the order of the results tree hierarchy. */
	private String[] varList;

	private int maxResults;

	private Job nodeTitleUpdater;

	public ResultsTreeBuilderJob(String name, QueryNode q) {
		super(name);
		query = q;
		maxResults = JQueryTreeBrowserPlugin.getMaxResults();
		target = null;
		nodeTitleUpdater = null;
	}

	public ResultsTreeBuilderJob(String name, QueryNode q, int maxResults) {
		this(name, q);
		this.maxResults = maxResults;
	}

	public ResultsTreeBuilderJob(String name, QueryNode q, int maxResults, ResultsTreeNode target) {
		this(name, q, maxResults);
		this.target = target;
	}

	public void addDefaultChangeAdapter(StructuredViewer viewer, QueryNode node) {
		addDefaultChangeAdapter(viewer, node, null);
	}

	public void addDefaultChangeAdapter(final StructuredViewer viewer, final QueryNode node, Job job) {
		addJobChangeListener(new BuilderJobChangeAdapter(viewer, node));

		if (job == null) {
			nodeTitleUpdater = new LabelUpdateJob("node title updater", node.getLabel()) {
				protected void updateLabel(String label) {
					node.setLabel(label);
					viewer.update(node, null);
				}
			};
		} else {
			nodeTitleUpdater = job;
		}
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		JQueryResultSet results = null;
		IStatus result = Status.OK_STATUS;

		nodeTitleUpdater.schedule();

		qs = new QueryStatus();
		try {
			query.clearRecursiveChildren();

			results = query.getQuery().execute();
			Object[] vars = query.getQuery().getChosenVars().toArray();

			varList = new String[vars.length];
			for (int i = 0; i < vars.length; i++) {
				varList[i] = (String) vars[i];
			}

			if (results.hasNext()) {
				while (results.hasNext() && !monitor.isCanceled()) {
					addResultToTree(results.next());
				}
			} else {
				query.addChild(new NoResultNode());
			}
			qs.setStatus(QueryStatus.DONE);
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error(e);
			result = Status.CANCEL_STATUS;
			qs.setExitDetails(e);
		} finally {
			JQueryTreeBrowserPlugin.traceQueries("ResultsTreeBuilder thread done.");
			if (results != null) {
				results.close();
			}

			nodeTitleUpdater.cancel();

			if (monitor.isCanceled()) {
				result = Status.CANCEL_STATUS;
				qs.setExitReason("Cancelled");
				qs.setStatus(QueryStatus.ABORTED);
			}
		}

		return result;
	}

	/**
	 * Adds the contents of the given frame to the tree using variables to order the hierarchy in the tree.
	 */
	private void addResultToTree(JQueryResult r) throws JQueryException {
		ResultsTreeNode node = query;
		if (target != null) {
			node = target;
		}

		for (int i = 0; i < varList.length; i++) {
			node = addResultToTree(node, r.get(varList[i]), varList[i]);
		}

		qs.incrementNumResults();
		if (qs.getNumResults() >= maxResults) {
			JQueryTreeBrowserPlugin.error(query.getLabel() + ": Number of results exceeded allowable limit");
			cancel();
		}
	}

	/** 
	 * Adds a result to a tree.  If the result is a list, it inserts it as a list, otherwise
	 * it does a simple insert.
	 */
	private ResultsTreeNode addResultToTree(ResultsTreeNode node, Object value, String var) {
		ResultsTreeNode result = null;
		if (value instanceof Object[]) {
			result = addListResultToTree(node, (Object[]) value, var);
		} else {
			result = node.addChild(new QueryResultNode(value, var));
		}

		return result;
	}

	/**
	 * Adds a list result to the tree.  List results are displayed as if they are recursive
	 * queries and each result is display as it's own node coming from the same variable.
	 * 
	 * TODO Probably in the future we can remove this in favour of recursive queries... not
	 * sure yet.
	 * 
	 * @param node
	 * @param list
	 * @param var
	 * @return
	 */
	private ResultsTreeNode addListResultToTree(ResultsTreeNode node, Object[] list, String var) {
		ResultsTreeNode result = node;

		if (list.length == 0) {
			result = new NoResultNode();
		}

		for (int i = 0; i < list.length; i++) {
			result = result.addChild(new QueryResultNode(list[i], var, i));
			qs.incrementNumResults();
		}

		if (qs.getNumResults() >= maxResults) {
			JQueryTreeBrowserPlugin.error(query.getLabel() + ": Number of results exceeded allowable limit");
			cancel();
		}

		return result;
	}
}
