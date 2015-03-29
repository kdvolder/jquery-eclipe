package ca.ubc.jquery.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactBase;
import ca.ubc.jquery.gui.dialogs.PopupQueryDialog;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.tree.ResultsTreeBuilderJob;
import ca.ubc.jquery.gui.views.JQueryTreeView;

/**
 * This class is a the heart of the JQuery browser.
 * 
 * It creates query nodes and displays the results in a tree given the correct
 * Content and Label Providers.  It also adds to the Eclipse TreeViewer the ability to
 * expand nodes and execute queries and manage query node result updates.
 * 
 * This tree class assumes that all elements it displays are of type ResultsTreeNode.
 *  
 * @author lmarkle
 */
public class JQueryTreeViewer extends TreeViewer implements JQueryViewer {

	private Map resultsJobs;

	// only here to help rename part
	private JQueryTreeView viewPart;

	public JQueryTreeViewer(Composite parent, int flags) {
		this(parent, flags, null);
	}

	public JQueryTreeViewer(Composite parent, int flags, JQueryTreeView view) {
		super(parent, flags);

		viewPart = view;
		resultsJobs = new HashMap();
	}

	public void cancelAllJobs() {
		JQueryTreeBrowserPlugin.traceUI("Stop Action");
		for (Iterator it = resultsJobs.values().iterator(); it.hasNext();) {
			Job j = (Job) it.next();
			j.cancel();
		}
	}

	public ISelection getJMSelection() {
		ISelection s = super.getSelection();
		IStructuredSelection selection = (IStructuredSelection) s;
		List result = new ArrayList();

		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object target = it.next();
			if (target instanceof QueryResultNode) {
				Object o = ((QueryResultNode) target).getElement();
				try {
					IJavaElement e = JQueryAPI.getJavaModelElement(o);
					if (e != null) {
						result.add(e);
					} else {
						result.add(o);
					}
				} catch (JQueryException e) {
					result.add(o);
				}
			}
		}

		return new StructuredSelection(result);
	}

	@Override
	public void setSelection(ISelection selection) {
		setSelection(selection, false);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		List newSelection = new ArrayList();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			for (Iterator it = ss.iterator(); it.hasNext();) {
				ResultsTreeNode node = (ResultsTreeNode) it.next();
				ResultsTreeNode current = findNodeInTree((ResultsTreeNode) getRoot(), node);
				if (current != null) {
					newSelection.add(current);
				}
			}
		}

		if (!newSelection.isEmpty()) {
			setSelectionToWidget(new StructuredSelection(newSelection), reveal);
		}
	}

	private ResultsTreeNode findNodeInTree(ResultsTreeNode root, ResultsTreeNode node) {
		if (isNodeEqual(root, node)) {
			return root;
		}

		ResultsTreeNode result = null;
		for (Iterator it = root.getChildren().iterator(); result == null && it.hasNext();) {
			ResultsTreeNode temp = (ResultsTreeNode) it.next();
			if (isNodeEqual(temp, node)) {
				result = temp;
			} else {
				result = findNodeInTree(temp, node);
			}
		}

		return result;
	}

	private boolean isNodeEqual(ResultsTreeNode node1, ResultsTreeNode node2) {
		if (node1 == null || node2 == null) {
			return false;
		}

		return (node1.getElement() == null) ? (node2.getElement() == null) : node1.getElement().equals(node2.getElement());
	}

	public void performNodeExpansion() {
		performNodeExpansion((ResultsTreeNode) getInput());
	}

	public void performNodeExpansion(ResultsTreeNode root) {
		if (root == null) {
			return;
		}

		Collection c = root.getChildren();

		for (Iterator it = c.iterator(); it.hasNext();) {
			ResultsTreeNode n = (ResultsTreeNode) it.next();
			if (n.getExpandState() == ResultsTreeNode.NodeExpanded) {
				expandToLevel(n, 1);
				performNodeExpansion(n);
			} else if (n.getExpandState() == ResultsTreeNode.NodeCollapsed) {
				collapseToLevel(n, TreeViewer.ALL_LEVELS);
			}
		}
	}

	public ResultsTreeNode getTreeRoot() {
		return (ResultsTreeNode) getInput();
	}

	/**
	 * @return true if this tree has the factbase at it's root, false otherwise
	 */
	public boolean isRootTree() {
		ResultsTreeNode n = getTreeRoot();
		return (n != null ? n.getElement() instanceof JQueryFactBase : false);
	}

	/**
	 * Creates a QueryNode having the given query, sets it as a child of parentNode and calls execute(QueryNode) on the newly created node.
	 * 
	 * @param query
	 * @param targetNode
	 */
	public QueryNode createQueryNode(String query, String label, ResultsTreeNode parentNode) throws JQueryException {
		QueryNode newNode = new QueryNode(query, label);
		parentNode.addChild(newNode);
		refresh(parentNode, false);

		return newNode;
	}

	/**
	 * Convenience method: Just calls createAndExecute(Query, ResultsTreeNode) with the tree's root as the parentNode parameter.
	 */
	public QueryNode createQueryNode(String topLevelQuery, String label) throws JQueryException {
		return createQueryNode(topLevelQuery, label, getTreeRoot());
	}

	/**
	 * Executes the query contained in this QueryNode against the current rulebase. This method is the main entry point to the results tree; that is, execute is the only method that should be called when wanting to modify the results tree.
	 * 
	 * To esure that the queryNode is propperly nested/displayed, avoid creating/adding the node to the tree manually. Instead call createAndExecute with the query you want to run and the desired parentNode.
	 * 
	 * @vsf+ queries
	 */
	public Job execute(QueryNode queryNode) {
		return execute(queryNode, 0);
	}

	public Job execute(final QueryNode queryNode, int startDelay) {
		ResultsTreeBuilderJob resultsJob = (ResultsTreeBuilderJob) resultsJobs.get(queryNode);
		if (resultsJob != null && resultsJob.getState() != Job.NONE) {
			resultsJob.cancel();
		}

		String name = "Query execute: " + queryNode.getLabel();
		resultsJob = new ResultsTreeBuilderJob(name, queryNode);
		resultsJob.setPriority(Job.INTERACTIVE);

		// only rename the browser if we have renamed the query node
		if (viewPart != null && getTreeRoot() == queryNode) {
			LabelUpdateJob j = new LabelUpdateJob("part name updater", queryNode.getLabel()) {
				protected void updateLabel(String label) {
					viewPart.renamePart(label);
				}
			};

			resultsJob.addDefaultChangeAdapter(this, queryNode, j);
		} else {
			resultsJob.addDefaultChangeAdapter(this, queryNode);
		}

		resultsJob.setSystem(true);
		resultsJob.schedule(startDelay);
		resultsJobs.put(queryNode, resultsJob);

		return resultsJob;
	}

	public void doEditQuery(final QueryNode node) {
		setSelection(new StructuredSelection(node));
		PopupDialog dialog = new PopupQueryDialog(JQueryTreeBrowserPlugin.getShell(), this, node);
		dialog.open();
	}

	public void doReExecuteAction(IStructuredSelection queryNodes) {
		boolean flush = false;

		setSelection(queryNodes);
		for (Iterator iter = queryNodes.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof QueryNode) {
				flush = true;
				execute((QueryNode) obj);
			}
		}

		if (flush) {
			((JQueryResultsLabelProvider) getLabelProvider()).flushCache();
		}
	}

	public void doDeleteQueryAction(IStructuredSelection nodes) {
		Set queryNodes = new HashSet();

		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof QueryNode) {
				QueryNode node = (QueryNode) obj;
				node.getParent().removeChild(node);
				queryNodes.add(obj);
			}
		}

		for (Iterator it = queryNodes.iterator(); it.hasNext();) {
			// remove from tree
			QueryNode n = (QueryNode) it.next();
			refresh(n.getParent());

			// remove job (if it's still executing)
			Job j = (Job) resultsJobs.get(n);
			if (j != null) {
				j.cancel();
			}
		}
	}
}
