//package ca.ubc.jquery.gui.tree;
//
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.swt.widgets.Display;
//
//import ca.ubc.jquery.gui.results.ResultsTreeNode;
//import ca.ubc.jquery.query.QueryStatus;
//
///**
// * @author Doug Janzen
// *
// * This class operates on a timer, updating the given TreeViewer
// * at increasing intervals.
// * 
// */
//public class ResultsTreeMonitor implements Runnable {
//
//	private ResultsTreeNode root;
//	private TreeViewer treeViewer;
//	private Display display;
//	public int delay = 100;
//	public double delayFactor = 1.2;
//	public int maxDelay = 5000;
//	public boolean doCallback = true;
////	private List consoleOutput;	
////	private Text console;
//	private QueryStatus queryStatus;
////	private QueryControl queryControl;
//
//	/**
//	 * Constructor for ResultsTreeMonitor.
//	 * @vsf+ queries
//	 */
//	// public ResultsTreeMonitor(QueryControl queryControl, TreeViewer
//	// treeViewer, ResultsTreeNode root, Text console, List consoleOutput,
//	// Display display, QueryStatus queryStatus) {
//	public ResultsTreeMonitor(TreeViewer treeViewer, ResultsTreeNode root, Display display, QueryStatus queryStatus) {
////		this.queryControl = queryControl;
//		this.treeViewer = treeViewer;
//		this.root = root;
////		this.console = console;
////		this.consoleOutput = consoleOutput;
//		this.display = display;
//		this.queryStatus = queryStatus;
//	}
//
//	/**
//	 * @see Runnable#run()
//	 * @vsf+ queries
//	 */
//	public void run() {
//
//		// query is not finished yet
//		if (queryStatus.getStatus() == QueryStatus.RUNNING) {
//			
//			// start displaying results if we've already waited for a while
//			if (delay > 1500) {
//				updateTreeViewer();
//			}
//			
//			// increase delay up to the maximum
//			if (delay < maxDelay) {
//				delay *= delayFactor;
//			}
//			
//			// update the console
////			updateConsole();
//			
//			// reset timer
//			display.timerExec(delay, this);
//		}
//		else {
//			updateTreeViewer();
////			updateConsole();
////			if (doCallback) {
////				queryControl.queryDone();
////			}
//		}
//	}
//
//	/**
//	 * Updates the tree viewer.
//	 */
//	public void updateTreeViewer() {
//		treeViewer.refresh(root);
//	}
//	
////	/**
////	 * Updates the console using the line reader.
////	 */
////	public void updateConsole() {
////		synchronized(consoleOutput) {
////			Iterator itr = consoleOutput.iterator();
////			while (itr.hasNext()) {
////				String out = (String) itr.next();
////				console.append(out+console.getLineDelimiter());
////			}
////			consoleOutput.clear();
////		}
////	}
//
//	/** Starts the monitor running in the display thread. */	
//	public void start() {
//		display.timerExec(delay, this);
//	}
//}
