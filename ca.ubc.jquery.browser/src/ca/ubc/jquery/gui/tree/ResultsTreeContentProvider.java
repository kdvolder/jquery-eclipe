package ca.ubc.jquery.gui.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.gui.results.NoResultNode;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * The content provider for the JFace TreeView. Elements of the tree
 * are all TreeNodes.
 * @see ITreeContentProvider
 */
public class ResultsTreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		ResultsTreeNode parentNode = (ResultsTreeNode) parentElement;
		List result = new ArrayList();

		result.addAll(parentNode.getChildren());
		return result.toArray();
	}

	public Object getParent(Object element) {
		return ((ResultsTreeNode) element).getParent();
	}

	public boolean hasChildren(Object element) {
		//  TODO Fix this method
		//
		// normally the children generation stuff is handled in getChildren() not this 
		// method.  Although it works for this application, it may not always work that 
		// way and people sort of expect getChildren() to actually generate the children
		// if necessary, not this method.
		//
		// I'm going to leave it because it works but it may not be the best thing.
		if (element instanceof QueryResultNode) {
			QueryResultNode qr = (QueryResultNode) element;
			JQuery q = qr.getQueryNode().getQuery();
			if (q.isRecursive() && q.getRecursiveVar().equals(qr.getElementSource())) {
				try {
					QueryNode qn = qr.getQueryNode();
					if (qn.getQuery().isRecursive()) {
						String var = qn.getQuery().getRecursiveVar();
						if (var.equals(qr.getElementSource())) {
							// here is where we avoid doing infinite recursions
							if (qn.addRecursiveChild(qr)) {
								buildTree(qn, qr, qr);
								//							} else {
								//								// This is the case where we recursively execute and we've already seen the node
								//								// we still want to tell a user there's no more information (if there isn't another var
								//								// displayed below this.)
								//								//
								//								// this just ensures a NoResultNode is always displayed when there are no results
								//								qr.addChild(new NoResultNode());
							}
						}
					}
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error("Generating recursive tree: ", e);
				}
			}
		}

		return ((ResultsTreeNode) element).hasChildren();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	//
	// Helper stuff
	// 
	private void buildTree(QueryNode qn, QueryResultNode qr, ResultsTreeNode parentNode) throws JQueryException {
		JQuery query = (JQuery) qn.getQuery().clone();
		query.bind(JQueryAPI.getThisVar(), qr.getElement());

		// TODO Get background loading working...
		//					result.add(new TemporaryResultNode());
		//
		// return and run the rest of this in a thread...
		JQueryResultSet results = null;

		try {
			results = query.execute();
			List vars = query.getChosenVars();

			String[] varList = new String[vars.size()];
			for (int i = 0; i < vars.size(); i++) {
				varList[i] = (String) vars.get(i);
			}

			while (results.hasNext()) {
				addResultToTree(varList, parentNode, results.next());
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.traceQueries(e);
		} finally {
			JQueryTreeBrowserPlugin.traceQueries("ResultsTreeBuilder thread done.");
			if (results != null) {
				results.close();
			}
		}
	}

	/**
	 * Adds the contents of the given frame to the tree using variables to order the hierarchy in the tree.
	 */
	private void addResultToTree(String[] varList, ResultsTreeNode parent, JQueryResult r) throws JQueryException {
		ResultsTreeNode node = parent;
		for (int i = 0; i < varList.length; i++) {
			node = addResultToTree(node, r.get(varList[i]), varList[i]);
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
			ResultsTreeNode child = new QueryResultNode(value, var);
			result = node.addChild(child);
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
		}

		return result;
	}
}
