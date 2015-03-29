//package ca.ubc.jquery.gui;
//
//import java.text.DateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.SortedMap;
//import java.util.TreeMap;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IMenuListener;
//import org.eclipse.jface.action.IMenuManager;
//import org.eclipse.jface.action.MenuManager;
//import org.eclipse.jface.action.Separator;
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.CTabFolder;
//import org.eclipse.swt.custom.CTabItem;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.graphics.Cursor;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Menu;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.IWorkbenchPage;
////import org.eclipse.ui.IWorkingSet;
//
//import ca.ubc.jquery.api.*;
//import ca.ubc.jquery.ActionsLog;
//import ca.ubc.jquery.JQueryTreeBrowserPlugin;
//import ca.ubc.jquery.gui.dialogs.QueryDialog;
//import ca.ubc.jquery.gui.dialogs.RegexDialog;
//import ca.ubc.jquery.gui.results.QueryNode;
//import ca.ubc.jquery.gui.results.QueryResultNode;
//import ca.ubc.jquery.gui.results.ResultsTreeBuilder;
//import ca.ubc.jquery.gui.results.ResultsTreeContentProvider;
//import ca.ubc.jquery.gui.results.ResultsTreeDoubleClickListener;
//import ca.ubc.jquery.gui.results.ResultsTreeLabelProvider;
//import ca.ubc.jquery.gui.results.ResultsTreeListener;
//import ca.ubc.jquery.gui.results.ResultsTreeMonitor;
//import ca.ubc.jquery.gui.results.ResultsTreeNode;
//import ca.ubc.jquery.gui.results.ResultsTreeSorter;
//import ca.ubc.jquery.gui.results.ResultsTreeRootNode;
//import ca.ubc.jquery.query.IQueryEventListener;
//import ca.ubc.jquery.query.QueryResults;
//import ca.ubc.jquery.query.QueryEvent;
//import ca.ubc.jquery.query.QueryStatus;
//
///**
// * The widget containing the JQuery tree and console tabs
// */
//public class QueryControl extends Composite {
//
//	// /** the queryEngine containing facts for this controls working set */
//	// private QueryEngine queryEngine;
//	// /** The parent control. */
//	// private Composite parent = null;
//	/** The tab folder for the tree and console. */
//	CTabFolder tabFolder;
//
//	/** The results console. */
//	private Text console;
//
//	/** The button to close this query tab. */
//	private Button close = null;
//
//	/**
//	 * The root node in the results tree, which contains a pointer to the working set.
//	 */
//	ResultsTreeRootNode ROOT;
//
//	/** The results tree viewer. */
//	private TreeViewer treeViewer;
//
//	/** The label provider for the results tree viewer. */
//	private ResultsTreeLabelProvider labelProvider;// = new
//
//	// ResultsTreeLabelProvider();
//
//	/** The sorter for the results tree viewer. */
//	private ResultsTreeSorter resultsTreeSorter = new ResultsTreeSorter();
//
//	/** The thread that populates the TreeViewer with the results of the query. */
//	private ResultsTreeBuilder resultsThread = null;
//
//	/** The object that checks the resultsThread and updates the treeViewer. */
//	private ResultsTreeMonitor monitor;
//
//	/** The busy cursor for the treeViewer while results are being computed. */
//	Cursor busyCursor = null;
//
//	/** The normal cursor for the treeViewer. */
//	Cursor normalCursor = null;
//
//	/** The console output from running the query. */
//	private java.util.List consoleOutput = Collections.synchronizedList(new LinkedList());
//
//	/** List of event listeners. */
//	private LinkedList eventListeners = new LinkedList();
//
//	/** The tree tab. */
//	private CTabItem treeTabItem;
//
//	/** The console tab. */
//	private CTabItem consoleTabItem;
//
//	/** The status of the current query. */
//	private QueryStatus queryStatus = null;
//
//	/** The context menu for treeViewer */
//	private MenuManager mnuMainContext;
//
//	public static final String GROUP_TOP_QUERY = "group.top.query";
//
//	public static final String GROUP_NODE_SPECIFIC = "group.result.node";
//
//	public static final String GROUP_DISPLAY = "group.display";
//
//	public static final String GROUP_VSF = "group.vsf.add";
//
//	// Menu actions
//	Action sortByCategory;
//
//	Action showReturnType;
//
//	Action showFieldType;
//
//	/**
//	 * Creates a new QueryControl with the
//	 * 
//	 * @vsf+ widgets
//	 */
//	public QueryControl(JQueryTreeBrowserPlugin plugin, Composite parent, ResultsTreeRootNode root) {
//		super(parent, SWT.NULL);
//		JQueryTreeBrowserPlugin.traceUI("Creating QueryControl");
//
//		labelProvider = new ResultsTreeLabelProvider(plugin);
//
//		ROOT = root;
//		// this.queryEngine = root.getRuleBase();
//
//		// create cursors
//		busyCursor = new Cursor(getDisplay(), SWT.CURSOR_APPSTARTING);
//		normalCursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
//
//		// configure this
//		GridLayout gridLayout = new GridLayout();
//		gridLayout.numColumns = 1;
//		gridLayout.marginHeight = 1;
//		gridLayout.marginWidth = 1;
//		gridLayout.verticalSpacing = 1;
//		this.setLayout(gridLayout);
//
//		close = new Button(this, SWT.PUSH | SWT.FLAT);
//		close.setImage(plugin.getImageRegistry().get("Close"));
//		close.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				fireEvent(QueryEvent.CLOSED);
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//		close.setToolTipText("Closes this working set tree");
//		close.setLayoutData(new GridData(SWT.RIGHT));
//		makeBottomSash(plugin);
//		createContextMenu();
//
//	}
//
//	/**
//	 * Constructs the bottom portion of the sash that displays the results of executing the query.
//	 * 
//	 * @vsf+ widgets
//	 * @vsf+ treeviewer
//	 */
//	private void makeBottomSash(JQueryTreeBrowserPlugin plugin) {
//
//		// the tab folder
//		tabFolder = new CTabFolder(this, SWT.TOP | SWT.BORDER);
//
//		GridData gridData = new GridData();
//		gridData.verticalAlignment = GridData.FILL;
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//
//		tabFolder.setLayoutData(gridData);
//
//		// results tree
//		IWorkbenchPage workbenchPage = plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		if (workbenchPage == null) {
//			System.out.println("SD");
//		}
//		treeViewer = new TreeViewer(tabFolder, SWT.SINGLE);
//		treeViewer.setContentProvider(new ResultsTreeContentProvider());
//		treeViewer.setLabelProvider(labelProvider);
//		treeViewer.addDoubleClickListener(new ResultsTreeDoubleClickListener());
//		treeViewer.setSorter(resultsTreeSorter);
//		treeViewer.addTreeListener(new ResultsTreeListener());
//		treeViewer.setInput(ROOT);
//
//		gridData = new GridData();
//		gridData.verticalAlignment = GridData.FILL;
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//
//		treeViewer.getControl().setLayoutData(gridData);
//
//		treeTabItem = new CTabItem(tabFolder, 0);
//		treeTabItem.setText("Tree");
//		treeTabItem.setControl(treeViewer.getControl());
//		tabFolder.setSelection(treeTabItem);
//
//		// the console
//		console = new Text(tabFolder, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		gridData = new GridData();
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.verticalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//
//		console.setLayoutData(gridData);
//
//		consoleTabItem = new CTabItem(tabFolder, 0);
//		consoleTabItem.setText("Console");
//		consoleTabItem.setControl(console);
//
//	}
//
//	/**
//	 * Sets the given flag on the lableProvider as chosen from the context menu.
//	 * 
//	 * @vsf+ contextMenu
//	 * @vsf+ treeviewer
//	 */
//	void doLabelOption(int flag, boolean turnOn) {
//		if (turnOn) {
//			labelProvider.turnOn(flag);
//		} else {
//			labelProvider.turnOff(flag);
//		}
//		treeViewer.refresh(ROOT);
//	}
//
//	/**
//	 * Adds the query selected from the context menu to the node(s) selected in the tree viewer.
//	 * 
//	 * @vsf+ queries
//	 * @vsf+ contextMenu
//	 */
//	void doSubQuery(String label, String ruleText, Object[] varStrings) {
//
//		// for each selected node
//		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
//		Iterator itr = selection.iterator();
//		while (itr.hasNext()) {
//			ResultsTreeNode node = (ResultsTreeNode) itr.next();
//
////			String selectedNodeName = labelProvider.getText(node);
////			JQueryTreeBrowserPlugin.traceUI("Running Query on: " + selectedNodeName);
//
//			// log query
////			ActionsLog.logGeneric("Menu query run", new String[] { "query: " + label, "on object: " + selectedNodeName });
//
//			List varsList = new ArrayList(varStrings.length);
//			for (int i = 0; i < varStrings.length; i++) {
//				// varsList.add(FrontEnd.makeVar((String) varStrings[i]));
//				varsList.add(varStrings[i]);
//			}
//			// create query object
//			QueryResults menuQuery;
//			try {
//				menuQuery = new QueryResults(node.getElement(), ruleText, label);
//				menuQuery.setChosenVars(varsList);
//			} catch (Exception e) {
//				JQueryTreeBrowserPlugin.error("Error creating Query object: ", e);
//				JQueryTreeBrowserPlugin.traceUI("ResultsMenuSelection: parseError creating Query object (this shouldn't happen: fix code/rules: " + e.getMessage());
//				return;
//			}
//			createAndExecute(menuQuery, node);
//		}
//	}
//
//	private void addAvailableTopQueries(IMenuManager menu) {
//		IMenuManager topQueries = new MenuManager("Available Top-Level Queries");
//
//		try {
//			JQuery query = JQueryAPI.topLevelQuery();
//			SortedMap snippets = new TreeMap();
//
//			JQueryResultSet results = query.execute();
//			String[] vars = query.getVariables();
//
//			while (results.hasNext()) {
//				JQueryResult result = results.next();
//
//				// TODO Why did variable order change here?
//				String ruleName = (String) result.get(vars[0]);
//				Object[] varList = (Object[]) result.get(vars[1]);
//				String label = (String) result.get(vars[2]);
//
//				snippets.put(label, new Object[] { ruleName, varList });
//			}
//
//			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
//
//				String label = (String) iter.next();
//				Object[] o = (Object[]) snippets.get(label);
//
//				final String ruleName = (String) o[0];
//				final Object[] varList = (Object[]) o[1];
//
//				// create the menu item
//				Action action = new Action(label) {
//					public void run() {
//						doTopLevelQuery(this.getText(), ruleName, varList);
//					}
//				};
//
//				// This here adds all them purrty choices
//				topQueries.add(action);
//			}
//
//			menu.appendToGroup(GROUP_TOP_QUERY, topQueries);
//
//		} catch (JQueryException e) {
//			// this shouldn't happen unless rules are written incorrectly
//			JQueryTreeBrowserPlugin.error("Error occurred while retrieving available top-level queries: ", e);
//		}
//	}
//
//	private void addAvailableSubQueries(IMenuManager menu, IStructuredSelection queryResultNodes) {
//		Object[] targets = new Object[queryResultNodes.size()];
//		Iterator it = queryResultNodes.iterator();
//		for (int i = 0; it.hasNext(); i++) {
//			QueryResultNode node = (QueryResultNode) it.next();
//			targets[i] = node.getElement();
//		}
//
//		try {
//			JQuery query = JQueryAPI.menuQuery(targets);
//			SortedMap snippets = new TreeMap();
//
//			String[] vars = query.getVariables();
//			JQueryResultSet results = query.execute();
//			while (results.hasNext()) {
//				JQueryResult result = results.next();
//
//				Object[] pathTerm = (Object[]) result.get(vars[0]);
//				String ruleText = (String) result.get(vars[1]);
//				Object[] varList = (Object[]) result.get(vars[2]);
//
//				// label for sorting the list
//				String label = "";
//				for (int i = 0; i < pathTerm.length; i++) {
//					label = label + pathTerm[i] + " ";
//				}
//
//				snippets.put(label, new Object[] { pathTerm, ruleText, varList });
//			}
//
//			// create menu from sorted list
//			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {
//				Object[] o = (Object[]) snippets.get(iter.next());
//
//				final Object[] pathTerm = (Object[]) o[0];
//				final String ruleText = (String) o[1];
//				final Object[] varList = (Object[]) o[2];
//
//				// create the menu item
//				Action action = new Action() {
//					public void run() {
//						doSubQuery(this.getText(), ruleText, varList);
//					}
//				};
//
//				// This here adds all them purrty choices
//				createPath(menu, pathTerm, action);
//			}
//		} catch (JQueryException e) {
//			JQueryTreeBrowserPlugin.error("Error occurred while retrieving available sub-queries: ", e);
//		}
//	}
//
//	private void createPath(IMenuManager menu, Object[] subMenuPathandName, Action action) {
//		// if (!(subMenuPathandName instanceof Object[])) {
//		// // just add it directly to the menu
//		// String name = (String) subMenuPathandName;
//		// action.setText(name);
//		// if (menu == mnuMainContext) {
//		// menu.appendToGroup(GROUP_NODE_SPECIFIC, action);
//		// } else {
//		// menu.add(action);
//		// }
//		// } else {
//		Object[] pathPair = (Object[]) subMenuPathandName;
//		ArrayList restPath = new ArrayList();
//		for (int i = 1; i < pathPair.length; i++) {
//			restPath.add(pathPair[i]);
//		}
//
//		String name = (String) ((Object[]) pathPair)[0];
//
//		if (restPath.size() == 0) {
//			// end of path, insert action here
//			action.setText(name);
//			if (menu == mnuMainContext) {
//				menu.appendToGroup(GROUP_NODE_SPECIFIC, action);
//			} else {
//				menu.add(action);
//			}
//		} else {
//			IMenuManager firstMenu = (IMenuManager) menu.find(name);
//			if (firstMenu == null) {
//				firstMenu = new MenuManager(name, name);
//				if (menu == mnuMainContext) {
//					menu.appendToGroup(GROUP_NODE_SPECIFIC, firstMenu);
//				} else {
//					menu.add(firstMenu);
//				}
//			}
//			// recurse into sub menu
//			createPath(firstMenu, restPath.toArray(), action);
//		}
//		// }
//	}
//
//	private void addDisplayItems(IMenuManager menu, IStructuredSelection selection) {
//		menu.appendToGroup(GROUP_DISPLAY, sortByCategory);
//		menu.appendToGroup(GROUP_DISPLAY, showReturnType);
//		menu.appendToGroup(GROUP_DISPLAY, showFieldType);
//
//	}
//
//	public void createMenuGroups(IMenuManager menu) {
//		if (!menu.isEmpty())
//			return;
//
//		menu.add(new Separator(GROUP_NODE_SPECIFIC));
//		menu.add(new Separator(GROUP_TOP_QUERY));
//		menu.add(new Separator(GROUP_VSF));
//		menu.add(new Separator(GROUP_DISPLAY));
//	}
//
//	/**
//	 * 
//	 * @vsf+ contextMenu
//	 */
//	public void fillContextMenu(IMenuManager menu) {
//		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
//		// int size= selection.size();
//		// Object element= selection.getFirstElement();
//		//			
//		addNodeSpecificItems(menu, selection);
//		// TODO: Vsf Remove or fix
//		// addRenderVsfFromTreeItem(menu, selection);
//		addTopQueryItems(menu, selection);
//		addDisplayItems(menu, selection);
//	}
//
//	/**
//	 * @vsf+ contextMenu
//	 */
//	private void addTopQueryItems(IMenuManager menu, IStructuredSelection selection) {
//		addAvailableTopQueries(menu);
//		Action newTopLevelQueryAction = new Action("New Top-Level Query...") {
//			public void run() {
//				doNewTopLevelQueryAction();
//			}
//		};
//		menu.appendToGroup(GROUP_TOP_QUERY, newTopLevelQueryAction);
//	}
//
//	/**
//	 * @vsf+ contextMenu
//	 */
//	private void addNodeSpecificItems(IMenuManager menu, IStructuredSelection selection) {
//		Object element = selection.getFirstElement();
//		if (element instanceof QueryNode) {
//			Action reExecuteQueryAction = new Action("Re-execute Query") {
//				public void run() {
//					doReExecuteAction();
//				}
//			};
//
//			Action editQueryAction = new Action("Edit Query") {
//				public void run() {
//					doEditQueryAction();
//				}
//			};
//
//			Action deleteQueryAction = new Action("Delete Query") {
//				public void run() {
//					doDeleteQueryAction();
//				}
//			};
//
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, reExecuteQueryAction);
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, editQueryAction);
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, deleteQueryAction);
//		}
//
//		else if (element instanceof QueryResultNode) {
//
//			Action newSubQueryAction = new Action("New Sub-Query...") {
//				public void run() {
//					doNewSubQueryAction();
//				}
//			};
//			addAvailableSubQueries(menu, selection);
//
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, new Separator());
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, newSubQueryAction);
//
//			// Filter by Regular Expression action
//			Action filterQueryAction = new Action("Filter Query") {
//				public void run() {
//					doFilterQueryAction();
//				}
//			};
//
//			menu.appendToGroup(GROUP_NODE_SPECIFIC, filterQueryAction);
//
//		}
//	}// end addNodeSpecificItems
//
//	/**
//	 * Creates the context menu for the results tree.
//	 * 
//	 * @vsf+ contextMenu
//	 * @vsf+ treeviewer
//	 */
//	private void createContextMenu() {
//
//		// create context menu
//		mnuMainContext = new MenuManager();
//		mnuMainContext.setRemoveAllWhenShown(true);
//
//		mnuMainContext.addMenuListener(new IMenuListener() {
//			public void menuAboutToShow(IMenuManager menu) {
//				tabFolder.setCursor(busyCursor);
//				createMenuGroups(menu);
//				fillContextMenu(menu);
//				tabFolder.setCursor(normalCursor);
//
//			}
//		});
//
//		Menu menu = mnuMainContext.createContextMenu(treeViewer.getControl());
//		treeViewer.getControl().setMenu(menu);
//
//		sortByCategory = new Action("Sort by Java Element Type", Action.AS_CHECK_BOX) {
//			public void run() {
//				if (isChecked()) {
//					resultsTreeSorter.byCategory = true;
//				} else {
//					resultsTreeSorter.byCategory = false;
//				}
//				treeViewer.refresh(ROOT);
//			}
//		};
//		sortByCategory.setChecked(true);
//		showReturnType = new Action("Show Method Return Type", Action.AS_CHECK_BOX) {
//			public void run() {
//				doLabelOption(ResultsTreeLabelProvider.SHOW_RETURN_TYPE, isChecked());
//			}
//		};
//		showFieldType = new Action("Show Field Type", Action.AS_CHECK_BOX) {
//			public void run() {
//				doLabelOption(ResultsTreeLabelProvider.SHOW_TYPE, isChecked());
//			}
//		};
//
//	}
//
//	public void setCurrentQuery(QueryNode node) {
//		treeViewer.setSelection(new StructuredSelection(node));
//		doEditQueryAction();
//		// doFilterQueryAction();
//	}
//
//	void doEditQueryAction() {
//		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
//		QueryNode node = (QueryNode) selection.getFirstElement();
//		QueryDialog dialog = new QueryDialog(getShell(), node.getQuery(), "Edit Query");
//		int result = dialog.open();
//		if (result == Dialog.OK) {
//			if (dialog.isStructureModified()) {
//				// old results are now hidden (by ResultsTreeBuilder) rather
//				// than deleted here.
//				// node.removeAllChildren();
//				execute(node);
//				// return;
//			} else if (dialog.isLabelModified()) {
//				treeViewer.update(node, null);
//			}
//
//			treeViewer.refresh(node, false);
//		}
//		treeViewer.reveal(node);
//	}
//
//	/**
//	 * doFilterQueryAction method: Filters the selected node according to user inputted filter. The filter must match a string of characters in the selected node in the Query result tree.
//	 * 
//	 */
//	void doFilterQueryAction() {
//		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
//		if (selection.isEmpty()) {
//			JQueryTreeBrowserPlugin.error("Cannot filter query: no result node selected");
//			return;
//		}
//		Iterator itr = selection.iterator();
//		QueryResultNode nodeResult = (QueryResultNode) itr.next();
//
//		// get the query responsible for the currently selected node in the view
//		QueryNode node = (QueryNode) nodeResult.getQueryNode();
//
//		// new Query is popped up
//		RegexDialog newQueryDialog = new RegexDialog(getShell(), node.getQuery(), "Filter Query", nodeResult.getElementSource().toString());
//
//		int result = newQueryDialog.open();
//		if (result == Dialog.OK) {
//			if (newQueryDialog.isStructureModified()) {
//				execute(node);
//			} else if (newQueryDialog.isLabelModified()) {
//				treeViewer.update(node, null);
//			}
//		}
//		treeViewer.refresh(node, false);
//		treeViewer.reveal(node);
//	}// end doFilterQueryAction()
//
//	void doReExecuteAction() {
//		IStructuredSelection queryNodes = (IStructuredSelection) treeViewer.getSelection();
//		for (Iterator iter = queryNodes.iterator(); iter.hasNext();) {
//			Object obj = iter.next();
//			if (obj instanceof QueryNode) {
//				execute((QueryNode) obj);
//			}
//		}
//	}
//
//	void doDeleteQueryAction() {
//		IStructuredSelection nodes = (IStructuredSelection) treeViewer.getSelection();
//		ArrayList queryNodes = new ArrayList(nodes.size());
//
//		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
//			Object obj = iter.next();
//			if (obj instanceof QueryNode) {
//				QueryNode node = (QueryNode) obj;
//				node.getParent().removeChild(node);
//				queryNodes.add(obj);
//			}
//		}
//		// this should technically be done by the content provider
//		treeViewer.remove(queryNodes.toArray());
//	}
//
//	void doNewTopLevelQueryAction() {
//		QueryDialog newQueryDialog = new QueryDialog(getShell(), ROOT.getElement(), "New Top-Level Query");
//
//		int result = newQueryDialog.open();
//		if (result == Dialog.OK) {
//			// create a new node to display the query results
//			QueryResults newQuery = newQueryDialog.getQuery();
//			if (newQueryDialog.isStructureModified()) {
//				createAndExecute(newQuery);
//			}
//			// setCurrentQuery(node);
//		}
//	}
//
//	void doTopLevelQuery(String label, String ruleText, Object[] varStrings) {
//		QueryResults menuQuery;
//		try {
//			menuQuery = new QueryResults(ROOT.getElement(), ruleText, label);
//			List varsList = new ArrayList(varStrings.length);
//			for (int i = 0; i < varStrings.length; i++) {
//				varsList.add(varStrings[i]);
//			}
//			menuQuery.setChosenVars(varsList);
//		} catch (Exception e) {
//			JQueryTreeBrowserPlugin.error("Error creating Top level Query object: ", e);
//			JQueryTreeBrowserPlugin.traceUI("doTopLevelQueryAction: parseError creating Query object (this shouldn't happen: fix code/rules: " + e.getMessage());
//			return;
//		}
//		createAndExecute(menuQuery);
//	}
//
//	void doNewSubQueryAction() {
//		IStructuredSelection targetNodes = (IStructuredSelection) treeViewer.getSelection();
//		if (targetNodes.isEmpty()) {
//			JQueryTreeBrowserPlugin.error("Cannot create sub-query: no result node selected");
//			return;
//		}
//		ResultsTreeNode target = (ResultsTreeNode) targetNodes.getFirstElement();
//		// Query subQuery = new Query(target);
//		QueryDialog newQueryDialog = new QueryDialog(getShell(), target.getElement(), "New Sub-Query: " + labelProvider.getText(target));
//
//		int result = newQueryDialog.open();
//		if (result == Dialog.OK) {
//			if (newQueryDialog.isStructureModified()) {
//				for (Iterator iter = targetNodes.iterator(); iter.hasNext();) {
//					ResultsTreeNode targetNode = (ResultsTreeNode) iter.next();
//					QueryResults subQuery = newQueryDialog.getQuery();
//					QueryResults copy = (QueryResults) subQuery.clone();
//					copy.setTarget(targetNode);
//					createAndExecute(copy, targetNode);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Sets the focus to this control.
//	 */
//	public boolean setFocus() {
//		return tabFolder.setFocus();
//	}
//
//	/**
//	 * Creates a QueryNode having the given query, sets it as a child of parentNode and calls execute(QueryNode) on the newly created node.
//	 * 
//	 * @param query
//	 * @param targetNode
//	 */
//	public QueryNode createAndExecute(QueryResults query, ResultsTreeNode parentNode) {
//		QueryNode newNode = new QueryNode(query);
//		parentNode.addChild(newNode);
//		treeViewer.refresh(parentNode, false);
//		execute(newNode);
//		return newNode;
//	}
//
//	/**
//	 * Convenience method: Just calls createAndExecute(Query, ResultsTreeNode) with the tree's root as the parentNode parameter.
//	 */
//	public void createAndExecute(QueryResults topLevelQuery) {
//		createAndExecute(topLevelQuery, ROOT);
//	}
//
//	/**
//	 * Executes the query contained in this QueryNode against the current rulebase. This method is the main entry point to the results tree; that is, execute is the only method that should be called when wanting to modify the results tree.
//	 * 
//	 * To esure that the queryNode is propperly nested/displayed, avoid creating/adding the node to the tree manually. Instead call createAndExecute with the query you want to run and the desired parentNode.
//	 * 
//	 * @vsf+ queries
//	 */
//	public void execute(QueryNode queryNode) {
//
//		// topSash.setCursor(busyCursor);
//		// querySash.setCursor(busyCursor);
//		tabFolder.setCursor(busyCursor);
//
//		JQueryTreeBrowserPlugin.traceQueries("Executing query:");
//
//		try {
//
//			// stop a currently executing query before starting
//			// a new one
//			if (queryStatus != null) {
//				// expandedElements = treeViewer.getExpandedElements();
//				// don't want the monitor to notify us that the old query is
//				// done.
//				if (monitor != null)
//					monitor.doCallback = false;
//
//				// must report abortion here since doCallback is false
//				ActionsLog.logResult("QUERY ABORTED", 0, "A NEW QUERY WAS ATTEMPTED");
//
//				queryStatus.abort(null, null);
//
//			}
//			// clear the console
//			consoleOutput.clear();
//			console.setText("");
//
//			queryStatus = new QueryStatus();
//			queryStatus.setStatus(QueryStatus.RUNNING);
//
//			// start a new resultsTreeBuilder thread to find/build results
//			resultsThread = new ResultsTreeBuilder(queryNode, queryStatus);
//			resultsThread.setDaemon(true);
//			resultsThread.start();
//			Display display = getShell().getDisplay();
//			// monitor = new ResultsTreeMonitor(this, treeViewer, queryNode,
//			// console, consoleOutput, display, queryStatus);
////			monitor = new ResultsTreeMonitor(this, treeViewer, queryNode, display, queryStatus);
//			display.timerExec(100, monitor);
//
//			// log query action
//			ActionsLog.logExecute(queryNode.getQuery());
//			treeViewer.reveal(queryNode);
//		} catch (Error e) {
//			JQueryTreeBrowserPlugin.traceQueries(e);
//			queryStatus.abort("An error occurred", e);
//			if (resultsThread != null) {
//				resultsThread.shouldStop = true;
//			}
//			// bottomSash.setCursor(normalCursor);
//		} catch (Exception e) {
//			JQueryTreeBrowserPlugin.traceQueries("QueryControl.execute(): ParseException - " + e.getMessage());
//			JQueryTreeBrowserPlugin.error(e);
//			queryStatus.abort("An error occurred", e);
//			if (resultsThread != null) {
//				resultsThread.shouldStop = true;
//			}
//		}
//	}
//
//	/**
//	 * Halts execution of the query.
//	 * 
//	 * @vsf+ queries
//	 */
//	public void stopQuery() {
//		if (queryStatus != null) {
//			queryStatus.abort("Stopped", null);
//		}
//		if (resultsThread != null) {
//			resultsThread.shouldStop = true;
//		}
//	}
//
//	/**
//	 * Called by ResultsTreeMonitor when the query is finished.
//	 * 
//	 * @vsf+ queries
//	 */
//	public void queryDone() {
//
//		/**
//		 * @author wannop
//		 */
//		String ld = console.getLineDelimiter();
//		String exitStatus;
//		String exitReason;
//
//		// output query statistics
//		console.append(ld + "------------------------------" + ld);
//
//		// end time
//		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
//		Date endTime = queryStatus.getEndTime();
//		console.append(dateFormat.format(endTime) + ld);
//
//		// number of results
//		int numResults = queryStatus.getNumResults();
//		console.append("Number of results = " + numResults + ld);
//		JQueryTreeBrowserPlugin.traceQueries("Number of results = " + numResults);
//
//		// number of nodes
//		int numNodes = queryStatus.getNumNodes();
//		console.append("Number of nodes = " + numNodes + ld);
//		JQueryTreeBrowserPlugin.traceQueries("Number of nodes = " + numNodes);
//
//		// execution time
//		long execTime = queryStatus.getExecutionTime();
//		long minutes = execTime / 60000;
//		double seconds = (double) (execTime - (minutes * 60000)) / 1000.0;
//		console.append("Execution time = " + minutes + " minutes " + seconds + " seconds" + ld);
//		JQueryTreeBrowserPlugin.traceQueries("Execution time = " + minutes + " minutes " + seconds + " seconds");
//
//		// output exit status
//		console.append(ld);
//
//		// query finished successfully
//		if (queryStatus.getStatus() == QueryStatus.DONE) {
//			exitStatus = "QUERY FINISHED SUCCESSFULLY";
//			exitReason = "";
//			console.append(exitStatus + ld + ld);
//			JQueryTreeBrowserPlugin.traceQueries(exitStatus);
//		}
//		// query was aborted
//		else if (queryStatus.getStatus() == QueryStatus.ABORTED) {
//			exitStatus = "QUERY ABORTED";
//			console.append(exitStatus + ld + ld);
//			JQueryTreeBrowserPlugin.traceQueries("QueryControl.queryDone: Exit Status:" + exitStatus);
//
//			// output reason
//			exitReason = queryStatus.getExitReason();
//			if (exitReason != null) {
//				console.append(exitReason + ld + ld);
//				JQueryTreeBrowserPlugin.traceQueries("QueryControl.queryDone: Exit Reason: " + exitReason);
//			}
//
//			// output details
//			Object exitDetails = queryStatus.getExitDetails();
//			if (exitDetails != null) {
//				console.append(exitDetails.toString() + ld + ld);
//				JQueryTreeBrowserPlugin.traceQueries(exitDetails);
//			}
//			tabFolder.setSelection(consoleTabItem);
//		}
//		// queryStatus should not have any other values
//		else {
//			exitStatus = "UNKNOWN";
//			exitReason = "INTERNAL ERROR: INVALID QUERY STATUS.";
//			console.append(exitReason + ld);
//			JQueryTreeBrowserPlugin.traceQueries("QueryControl.queryDone:" + exitReason);
//		}
//
//		// log results
//		ActionsLog.logResult(exitStatus, numNodes, exitReason);
//
//		// clean up
//		tabFolder.setCursor(normalCursor);
//		resultsThread = null;
//		monitor = null;
//		queryStatus = null;
//	}
//
//	/**
//	 * Adds the given object to the list of event listeners.
//	 */
//	public void addEventListener(IQueryEventListener eventListener) {
//		eventListeners.remove(eventListener);
//		eventListeners.add(eventListener);
//	}
//
//	/**
//	 * Removes the given object from the list of event listeners.
//	 */
//	public void removeEventListener(IQueryEventListener eventListener) {
//		eventListeners.remove(eventListener);
//	}
//
//	/**
//	 * Calls the processQuery() method of all event listeners.
//	 */
//	private void fireEvent(int type) {
//		QueryEvent event = new QueryEvent(type);
//		// QueryEvent event = new QueryEvent(this, type);
//		Iterator itr = eventListeners.iterator();
//		while (itr.hasNext()) {
//			IQueryEventListener listener = (IQueryEventListener) itr.next();
//			listener.queryEvent(event);
//		}
//	}
//
//	/**
//	 * Performs OS cleanup.
//	 */
//	public void dispose() {
//		normalCursor.dispose();
//		busyCursor.dispose();
//		close.dispose();
//	}
//
//	public ResultsTreeNode getTreeRoot() {
//		return ROOT;
//	}
//}