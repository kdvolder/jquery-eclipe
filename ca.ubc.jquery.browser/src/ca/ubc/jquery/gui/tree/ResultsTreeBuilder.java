//package ca.ubc.jquery.gui.tree;
//
//import java.util.List;
//
//import ca.ubc.jquery.JQueryTreeBrowserPlugin;
//import ca.ubc.jquery.api.JQueryException;
//import ca.ubc.jquery.api.JQueryResult;
//import ca.ubc.jquery.api.JQueryResultSet;
//import ca.ubc.jquery.gui.results.NoResultNode;
//import ca.ubc.jquery.gui.results.QueryNode;
//import ca.ubc.jquery.query.IQueryResults;
//import ca.ubc.jquery.query.QueryStatus;
//
///**
// * Builds a tree of results.
// */
//public class ResultsTreeBuilder extends Thread {
//
//	private IQueryResults query;
//
//	/**
//	 * The root of the results tree for the query being run. Not necessarily the root node in the tree viewer
//	 */
//	private QueryNode root;
//
//	/** The source from which to populate the results tree. */
//	private JQueryResultSet results;
//
//	/** The variables that determine the order of the results tree hierarchy. */
//	private String[] varList;
//
//	/** The object that tracks the status of the query. */
//	private QueryStatus queryStatus;
//
//	// /** The maximimum number of children nodes are allowed to have. */
//	// private static int maxChildren = 100;
//
//	/**
//	 * If this variable is set to true, then the result has been cencelled. We just exit.
//	 * 
//	 * @author Andrew Eisenberg ade@cs.ubc.ca
//	 * @author lmarkle Changed from public to private... why public?!
//	 */
//	private boolean shouldStop;
//
//	public void stopExecution() {
//		shouldStop = true;
//	}
//
//	/**
//	 * Constructor for ResultsTreeBuilder.
//	 */
//	public ResultsTreeBuilder(QueryNode root, QueryStatus queryStatus) {
//		this.query = root.getQuery();
//		this.root = root;
//		this.queryStatus = queryStatus;
//		shouldStop = false;
//		root.hideResults();
//	}
//
//	/**
//	 * @vsf+ queries
//	 */
//	public void run() {
//		try {
//			results = query.execute();
//			List vars = query.getChosenVars();
//
//			varList = new String[vars.size()];
//			for (int i = 0; i < vars.size(); i++) {
//				varList[i] = (String) vars.get(i);
//			}
//
//			if (results.hasNext()) {
//				while (results.hasNext() && !shouldStop) {
//					addResultToTree(results.next());
//				}
//			} else {
//				root.addChild(new NoResultNode());
//			}
//			// mark query as being done
//			synchronized (queryStatus) {
//				if (queryStatus.getStatus() == QueryStatus.RUNNING && !shouldStop) {
//					queryStatus.setStatus(QueryStatus.DONE);
//				} else if (shouldStop) {
//					queryStatus.setStatus(QueryStatus.ABORTED);
//				}
//			}
//
//		} catch (JQueryException e) {
//			JQueryTreeBrowserPlugin.traceQueries("ResultsTreeBuilder.run: exception occurred: " + e.getMessage());
//			queryStatus.abort("An exception occurred.", e);
//		} catch (Error e) {
//			// Error could result from two cases:
//			// 1. Thread.stop() was called
//			// 2. An error occurred while executing the query.
//			// We only want to call queryStatus.abort() in case 2.
//			JQueryTreeBrowserPlugin.traceQueries(e);
//			synchronized (queryStatus) {
//				if (queryStatus.getStatus() == QueryStatus.RUNNING) {
//					queryStatus.abort("An error occurred.", e);
//				}
//			}
//		} finally {
//			JQueryTreeBrowserPlugin.traceQueries("ResultsTreeBuilder thread done.");
//			if (results != null) {
//				results.close();
//			}
//		}
//	}
//
//	/**
//	 * Adds the contents of the given frame to the tree using variables to order the hierarchy in the tree.
//	 * 
//	 * @vsf+ queries
//	 */
//	private void addResultToTree(JQueryResult r) throws JQueryException {
//		Object[] toInsert = new Object[varList.length];
//		for (int i = 0; i < varList.length; i++) {
//			toInsert[i] = r.get(varList[i]);
//		}
//
//		root.addResult(toInsert, varList, queryStatus);
//		queryStatus.incrementNumResults();
//
//		if (queryStatus.getNumResults() > JQueryTreeBrowserPlugin.getMaxResults()) {
//			stopExecution();
//			queryStatus.setExitReason("Number of results exceeded allowable limit");
//		}
//	}
//}
