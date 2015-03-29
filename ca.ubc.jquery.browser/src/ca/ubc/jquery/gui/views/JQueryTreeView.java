package ca.ubc.jquery.gui.views;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.ActionsLog;
import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryEvent;
import ca.ubc.jquery.api.JQueryEventListener;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryObjectInputStream;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.browser.menu.DisplayItemMenuProvider;
import ca.ubc.jquery.browser.menu.EclipseMenuProvider;
import ca.ubc.jquery.browser.menu.LayeredMenuProviderFactory;
import ca.ubc.jquery.browser.menu.FilterMenuProvider;
import ca.ubc.jquery.browser.menu.GroupSpecificMenuProvider;
import ca.ubc.jquery.browser.menu.JQueryMenuProvider;
import ca.ubc.jquery.browser.menu.NodeSpecificMenuProvider;
import ca.ubc.jquery.browser.menu.TopQueryMenuProvider;
import ca.ubc.jquery.gui.JQueryResultsLabelProvider;
import ca.ubc.jquery.gui.JQuerySelectionProviderAdapter;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.JQueryViewer;
import ca.ubc.jquery.gui.QueryNodeUpdateJob;
import ca.ubc.jquery.gui.dnd.DragOperations;
import ca.ubc.jquery.gui.dnd.JQueryFilterTransfer;
import ca.ubc.jquery.gui.dnd.JQueryResultsDragListener;
import ca.ubc.jquery.gui.dnd.JQueryResultsTreeTransfer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.results.ResultsTreeRootNode;
import ca.ubc.jquery.gui.tree.ResultsTreeClickListener;
import ca.ubc.jquery.gui.tree.ResultsTreeContentProvider;
import ca.ubc.jquery.gui.tree.ResultsTreeDropAdapter;
import ca.ubc.jquery.gui.tree.ResultsTreeListener;
import ca.ubc.jquery.gui.tree.ResultsTreeSorter;
import ca.ubc.jquery.gui.tree.TreeRegExpFilterJob;

/**
 * JQueryView is the user interface that allows the user to enter queries and manage 
 * the rules files against which the queries will be run.
 * 
 * A query has two main parts: 
 * 	1) A body, which must be a valid query.
 * 	2) An ordered list of variables that define how the results are to be displayed.
 * 
 * @see ViewPart
 */
public class JQueryTreeView extends ViewPart implements IShowInSource, IShowInTarget, IPartListener, JQueryEventListener, ISelectionListener {

	/** Delay before regular expression filter updates */
	public final static int RegularExpressionUpdateDelay = 500;

	private IPath saveFile;

	private JQueryTreeViewer treeViewer;

	private String regExpFilter;

	/** The root node in the results tree */
	private ResultsTreeNode rootNode;

	/** The label provider for the results tree viewer. */
	private JQueryResultsLabelProvider labelProvider;

	private JQuerySelectionProviderAdapter selectionProvider;

	/** The sorter for the results tree viewer. */
	private ResultsTreeSorter resultsTreeSorter;

	private JQueryUpdateTarget updateTarget;

	private List menuProviders;

	protected QueryNodeUpdateJob browserUpdater;

	private int autoExpandDepth;

	//
	// -- SWT + Eclipse Stuff --
	// 
	/** The busy cursor for the treeViewer while results are being computed. */
	private Cursor busyCursor;

	/** The normal cursor for the treeViewer. */
	private Cursor normalCursor;

	/** The context menu for treeViewer */
	private MenuManager mnuMainContext;

	protected LinkBrowserAction linkBrowserAction;

	protected Action autoRefreshAction;

	private Job renamePartJob;

	private Job refreshViewJob;

	private Text regExpText;

	private Label regExpErrorIcon;

	private Composite regExpArea;

	private boolean enableRegExpFilter;

	private IMemento savedState;

	/**
	 * Contructs a JQueryView object.
	 */
	public JQueryTreeView() {
		autoExpandDepth = 0;

		rootNode = null;

		normalCursor = null;
		busyCursor = null;

		menuProviders = new ArrayList();

		regExpFilter = "";
		enableRegExpFilter = true;
		updateTarget = null;

		renamePartJob = null;
		refreshViewJob = null;

		savedState = null;
	}

	protected void setSaveFile(String file) {
		saveFile = JQueryTreeBrowserPlugin.getDefault().getStateLocation().append(".treeBrowser." + file);
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void saveState(IMemento memento) {
		// save state here
		memento.putString("saveFile", saveFile.toString());

		memento.putString("title", getPartName());

		memento.putBoolean("autoRefresh", autoRefreshAction.isChecked());
		memento.putInteger("autoExpand", autoExpandDepth);

		memento.putBoolean("filterVisible", enableRegExpFilter);
		memento.putString("filter", regExpFilter);

		// save query root information (if necessary)
		if (treeViewer.isRootTree()) {
			memento.putInteger("queryRootedView", 0);
		} else {
			memento.putInteger("queryRootedView", 1);

			IMemento child = memento.createChild("queryNode");
			try {
				((QueryNode) treeViewer.getTreeRoot()).saveState(child);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Unable to save state: ", e);
			}

			child = memento.createChild("linkTargets");
			if (linkBrowserAction.isChecked()) {
				Object[] linkTargets = linkBrowserAction.getLinkParts().toArray();
				child.putInteger("targetCount", linkTargets.length);
				for (int i = 0; i < linkTargets.length; i++) {
					child.putString("target" + i, ((JQueryUpdateTarget) linkTargets[i]).getName());
				}
			} else {
				child.putInteger("targetCount", 0);
			}

			memento.putString("inputFilter", browserUpdater.getInputFilter());
			memento.putString("selectionFilter", browserUpdater.getSelectionFilter());
		}

		File file = saveFile.toFile();
		saveImportantState(file);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		// restore from state here
		if (memento != null) {
			setSaveFile(memento.getString("saveFile"));

			Integer qrv = memento.getInteger("queryRootedView");
			if (qrv != null && qrv.intValue() == 1) {
				// restore query root information
				savedState = memento;
			}
		}
	}

	private void restoreState(IMemento memento) {
		IMemento temp = memento.getChild("queryNode");
		QueryNode n = new QueryNode(temp);
		rootNode = n;

		setPartName(memento.getString("title"));

		autoRefreshAction.setChecked(memento.getBoolean("autoRefresh"));
		autoExpandDepth = memento.getInteger("autoExpand");

		regExpFilter = memento.getString("filter");
		setRegExpFilterDisplay(memento.getBoolean("filterVisible"));

		temp = memento.getChild("linkTargets");
		int count = temp.getInteger("targetCount").intValue();
		JQueryUpdateTarget[] jut = new JQueryUpdateTarget[count];
		for (int i = 0; i < count; i++) {
			jut[i] = JQueryAPI.getUpdateTarget(temp.getString("target" + i));
		}
		linkBrowserAction.link(jut);

		browserUpdater.setInputFilter(memento.getString("inputFilter"));
		browserUpdater.setSelectionFilter(memento.getString("selectionFilter"));
	}

	/**
	 * Creates the widgets.
	 * 
	 * @see ViewPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		JQueryTreeBrowserPlugin.traceUI("Creating JQueryView Part");

		JQueryTreeBrowserPlugin.PLUGIN_COUNTER++;

		// listen for part life cycle events
		getViewSite().getPage().addPartListener(this);

		busyCursor = new Cursor(JQueryTreeBrowserPlugin.getShell().getDisplay(), SWT.CURSOR_APPSTARTING);
		normalCursor = new Cursor(JQueryTreeBrowserPlugin.getShell().getDisplay(), SWT.CURSOR_ARROW);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 1;
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(data);

		regExpArea = new Composite(parent, SWT.NONE);
		layout = GridLayoutFactory.fillDefaults().create();
		layout.numColumns = 2;
		regExpArea.setLayout(layout);
		data = GridDataFactory.fillDefaults().create();
		data.grabExcessVerticalSpace = false;
		regExpArea.setLayoutData(data);

		regExpText = new Text(regExpArea, SWT.NONE);
		regExpArea.setBackground(regExpText.getBackground());

		data = GridDataFactory.fillDefaults().create();
		data.grabExcessHorizontalSpace = true;
		regExpText.setLayoutData(data);
		regExpText.setToolTipText("Regular expression filter box");
		regExpText.addModifyListener(new ModifyListener() {

			private UIJob applyFilterJob = null;

			public void modifyText(ModifyEvent e) {
				try {
					Text et = (Text) e.widget;
					String t = et.getText();
					Pattern x = Pattern.compile(".*(" + t + ").*");

					if (enableRegExpFilter) {
						regExpFilter = et.getText();
					}

					if (applyFilterJob != null) {
						applyFilterJob.cancel();
					}
					if (("").equals(t)) {
						applyFilterJob = new TreeRegExpFilterJob("Update regular expression text", treeViewer, null);
					} else {
						applyFilterJob = new TreeRegExpFilterJob("Update regular expression text", treeViewer, x);
					}
					applyFilterJob.schedule(RegularExpressionUpdateDelay);

					// hide error icon
					((GridData) regExpErrorIcon.getLayoutData()).exclude = true;
					regExpErrorIcon.setVisible(false);
					((GridLayout) regExpErrorIcon.getParent().getLayout()).numColumns = 1;
					regExpErrorIcon.getParent().layout(false);
				} catch (PatternSyntaxException ex) {
					// show error icon
					regExpErrorIcon.setToolTipText(ex.getDescription());
					((GridData) regExpErrorIcon.getLayoutData()).exclude = false;
					regExpErrorIcon.setVisible(true);
					((GridLayout) regExpErrorIcon.getParent().getLayout()).numColumns = 2;
					regExpErrorIcon.getParent().layout(false);
				}
			}
		});

		regExpErrorIcon = new Label(regExpArea, SWT.NONE);
		regExpErrorIcon.setBackground(regExpText.getBackground());
		regExpErrorIcon.setImage(JQueryAPI.getElementImage("Error"));
		regExpErrorIcon.setVisible(false);
		data = GridDataFactory.fillDefaults().create();
		data.exclude = true;
		regExpErrorIcon.setLayoutData(data);
		((GridLayout) regExpErrorIcon.getParent().getLayout()).numColumns = 1;
		regExpErrorIcon.getParent().layout(false);

		if (rootNode == null) {
			rootNode = new ResultsTreeRootNode(JQueryAPI.getFactBase());
		}

		treeViewer = new JQueryTreeViewer(parent, SWT.MULTI, this);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeViewer.getControl().setLayoutData(data);
		resultsTreeSorter = new ResultsTreeSorter();
		labelProvider = new JQueryResultsLabelProvider(treeViewer);
		treeViewer.setContentProvider(new ResultsTreeContentProvider());
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.addOpenListener(new ResultsTreeClickListener(treeViewer));
		treeViewer.setSorter(resultsTreeSorter);
		treeViewer.addTreeListener(new ResultsTreeListener());
		treeViewer.setInput(rootNode);
		// drag and drop support
		//
		// DROP_MOVE isn't really moving, it's copying but for some reason transfers won't
		// work if I only put DROP_COPY.
		int operation = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		Transfer[] types = new Transfer[] { JQueryResultsTreeTransfer.getInstance(), JQueryFilterTransfer.getInstance(), PluginTransfer.getInstance() };
		DragSourceListener ds = new JQueryResultsDragListener(treeViewer) {
			protected void onDragStart(DragSourceEvent event, IStructuredSelection selection) {
				// Special handling for dragging 1 single query node
				if (selection.size() == 1 && selection.getFirstElement() instanceof QueryNode) {
					QueryNode n = (QueryNode) selection.getFirstElement();
					try {
						createNewTreeView(n, true);
						event.doit = false;
					} catch (PartInitException ex) {
						JQueryTreeBrowserPlugin.error(ex);
					}
				}
			}
		};
		treeViewer.addDropSupport(operation, types, new ResultsTreeDropAdapter(treeViewer, this));
		treeViewer.addDragSupport(operation, types, ds);
		selectionProvider = new JQuerySelectionProviderAdapter(treeViewer);
		getSite().setSelectionProvider(selectionProvider);

		//		treeViewer = new JQueryRGTreeViewer(parent, SWT.MULTI, this);
		//		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		//		treeViewer.getControl().setLayoutData(data);
		//		resultsTreeSorter = new ResultsTreeSorter();
		//		labelProvider = new JQueryResultsLabelProvider(treeViewer);
		//		treeViewer.setLabelProvider(labelProvider);
		//		treeViewer.addOpenListener(new ResultsTreeClickListener(treeViewer));
		//		treeViewer.setSorter(resultsTreeSorter);
		//		treeViewer.addTreeListener(new ResultsTreeListener());
		//		treeViewer.setInput(rootNode);
		//		// drag and drop support
		//		//
		//		// DROP_MOVE isn't really moving, it's copying but for some reason transfers won't
		//		// work if I only put DROP_COPY.
		//		int operation = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		//		Transfer[] types = new Transfer[] { JQueryResultsTreeTransfer.getInstance(), JQueryFilterTransfer.getInstance(), PluginTransfer.getInstance() };
		//		DragSourceListener ds = new JQueryResultsDragListener(treeViewer) {
		//			protected void onDragStart(DragSourceEvent event, IStructuredSelection selection) {
		//				// Special handling for dragging 1 single query node
		//				if (selection.size() == 1 && selection.getFirstElement() instanceof QueryNode) {
		//					QueryNode n = (QueryNode) selection.getFirstElement();
		//					try {
		//						JQueryTreeView v = createNewTreeView(n, true);
		//						event.doit = false;
		//					} catch (PartInitException ex) {
		//						JQueryTreeBrowserPlugin.error(ex);
		//					}
		//				}
		//			}
		//		};
		//		treeViewer.addDropSupport(operation, types, new ResultsTreeDropAdapter(treeViewer, this));
		//		treeViewer.addDragSupport(operation, types, ds);
		//		selectionProvider = new JQuerySelectionProviderAdapter(treeViewer);
		//		getSite().setSelectionProvider(selectionProvider);

		browserUpdater = new QueryNodeUpdateJob("Update JQuery Browser", treeViewer);
		browserUpdater.setSystem(true);
		browserUpdater.setPriority(Job.INTERACTIVE);
		browserUpdater.setRule(JQueryAPI.getRule());

		linkBrowserAction = new LinkBrowserAction("Link Browser", this, browserUpdater);
		ImageDescriptor syncIcon = JQueryTreeBrowserPlugin.getImageDescriptor("eclipse/synced.gif");
		linkBrowserAction.setImageDescriptor(syncIcon);
		linkBrowserAction.setHoverImageDescriptor(syncIcon);
		linkBrowserAction.setToolTipText("Links the query target with the selection from another window");

		final JQueryEventListener t = this;
		autoRefreshAction = new Action("Auto Refresh View", Action.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
					JQueryAPI.addListener(t);
				} else {
					JQueryAPI.removeListener(t);
				}
			}
		};

		// disable reg exp display by default
		setRegExpFilterDisplay(false);

		//
		// Restore previouse state
		setSaveFile(JQueryTreeBrowserPlugin.PLUGIN_ID + "." + getPartName() + "." + getViewSite().getSecondaryId());
		checkXMLConfiguration();

		// try to restore state from IMemento first
		if (savedState != null) {
			restoreState(savedState);
		}
		// else restore from the saved file
		File restoreFile = saveFile.toFile();
		if (restoreFile.exists()) {
			restoreImportantState(restoreFile);
		} else {
			updateTarget = JQueryAPI.createUpdateTarget(getPartName());
		}

		setTreeRoot(rootNode);
		treeViewer.performNodeExpansion(treeViewer.getTreeRoot());

		if (!treeViewer.isRootTree()) {
			browserUpdater.setQuery((QueryNode) treeViewer.getTreeRoot());
			JQueryAPI.addListener(this);
		}

		createJQueryContextMenu();

		// add selection listeners so it can update it's target in the API
		//		getSite().getPage().addPostSelectionListener(getPartName(), this);
		getSite().getPage().addPostSelectionListener(this);
	}

	private void setRegExpFilterDisplay(boolean visible) {
		enableRegExpFilter = visible;
		regExpArea.setVisible(visible);
		if (visible) {
			regExpText.setText(regExpFilter);
		} else {
			regExpText.setText("");
		}

		Composite cp = regExpArea.getParent();
		GridData gd = (GridData) regExpArea.getLayoutData();
		gd.exclude = !visible;
		cp.layout(false);
	}

	/** 
	 * @return true if there are additional information retrieved from the XML configuration
	 */
	protected boolean checkXMLConfiguration() {
		return false;
	}

	public void setTreeRoot(ResultsTreeNode object) {
		rootNode = object;
		treeViewer.setInput(object);

		getViewSite().getActionBars().getMenuManager().removeAll();
		getViewSite().getActionBars().getToolBarManager().removeAll();
		createViewMenu();

		getViewSite().getActionBars().getToolBarManager().update(true);
		treeViewer.refresh();
	}

	private void createViewMenu() {
		//
		// query actions
		//
		Action stopQueryAction = new Action("Stop Query") {
			public void run() {
				treeViewer.cancelAllJobs();
			}
		};
		stopQueryAction.setImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("Delete.gif"));
		stopQueryAction.setHoverImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("Delete_hover.gif"));
		stopQueryAction.setToolTipText("Stop currently executing query.");

		Action refreshViewAction = new Action("Refresh View") {
			public void run() {
				treeViewer.doReExecuteAction(new StructuredSelection(treeViewer.getTreeRoot()));
			}
		};
		refreshViewAction.setImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("ReloadRules.gif"));

		Action editQueryNodeAction = new Action("Edit Query") {
			public void run() {
				QueryNode q = (QueryNode) treeViewer.getInput();
				treeViewer.doEditQuery(q);
				renamePart(q.getLabel());
			}
		};
		editQueryNodeAction.setImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("NewQuery.gif"));
		editQueryNodeAction.setHoverImageDescriptor(JQueryTreeBrowserPlugin.getImageDescriptor("NewQuery_hover.gif"));

		//
		// browser actions
		//
		Action cloneBrowserAction = new Action("Clone Browser") {
			public void run() {
				try {
					JQueryTreeView v = createNewTreeView(treeViewer.getTreeRoot(), false);
					v.doRenameBrowser();
				} catch (Exception e) {
					JQueryTreeBrowserPlugin.message(e);
				}
			}
		};

		Action renameBrowserAction = new Action("Rename Browser") {
			public void run() {
				doRenameBrowser();
			}
		};

		Action autoExpandDepthAction = new Action("Set Auto-Expansion") {
			public void run() {
				QueryNode qn = (QueryNode) treeViewer.getTreeRoot();
				qn.setAutoExpansionDepth();
			}
		};

		//
		// factbase actions
		//
		Action selectFactbaseAction = new Action("Select Factbase") {
			public void run() {
				try {
					JQueryAPI.selectFactBase();
				} catch (JQueryException e) {
					JQueryTreeBrowserPlugin.error("Select factbase error", e);
				}
			}
		};
		selectFactbaseAction.setToolTipText("Select a facbase from the list.");

		Action reloadAllRulesAction = new Action("Reload All Sets' Rules") {
			public void run() {
				doReloadAllRulesAction();
			}
		};
		reloadAllRulesAction.setToolTipText("Reload rules and initialization files for all active working sets");

		Action refreshAction = new Action("Force Complete Factbase Refresh") {
			public void run() {
				doRefreshRuleBaseAction();
			}
		};
		refreshAction.setToolTipText("Force a complete regeneration of facts for current working set");

		// get menu manager
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();

		// add actions to menu
		menuMgr.add(cloneBrowserAction);
		if (!treeViewer.isRootTree()) {
			menuMgr.add(autoExpandDepthAction);
		}
		menuMgr.add(renameBrowserAction);
		menuMgr.add(new Action("Toggle text filter") {
			public void run() {
				setRegExpFilterDisplay(!enableRegExpFilter);
			}
		});

		menuMgr.add(new Separator());
		if (!treeViewer.isRootTree()) {
			menuMgr.add(autoRefreshAction);
			menuMgr.add(editQueryNodeAction);
			//			menuMgr.add(mnuHistory);
			menuMgr.add(linkBrowserAction);
			menuMgr.add(refreshViewAction);
		}
		menuMgr.add(stopQueryAction);

		menuMgr.add(new Separator());
		menuMgr.add(reloadAllRulesAction);
		menuMgr.add(refreshAction);
		menuMgr.add(selectFactbaseAction);

		// add actions to toolbar
		IToolBarManager toolMgr = getViewSite().getActionBars().getToolBarManager();
		//		// TODO Get this bar the right size...
		//		IContributionItem x = new ControlContribution("filter") {
		//			protected int computeWidth(Control control) {
		//				return control.computeSize(SWT.FILL, SWT.DEFAULT, true).x;
		//			}
		//
		//			protected Control createControl(Composite parent) {
		//				String text = regExpFilterText.getText();
		//				regExpFilterText = new Text(parent, SWT.BORDER);
		//				regExpFilterText.setText(text);
		//				regExpFilterText.addModifyListener(new ModifyListener() {
		//
		//					private UIJob j = null;
		//
		//					public void modifyText(ModifyEvent e) {
		//						String t = ((Text) e.widget).getText();
		//						try {
		//							RE x = new RE(t);
		//
		//							if (j != null) {
		//								j.cancel();
		//							}
		//
		//							if (("").equals(t)) {
		//								j = new TreeRegExpFilterJob("Update regular expression text", JQueryTreeView.this.treeViewer, null);
		//							} else {
		//								j = new TreeRegExpFilterJob("Update regular expression text", JQueryTreeView.this.treeViewer, x);
		//							}
		//							j.schedule(250);
		//						} catch (RESyntaxException ex) {
		//							return;
		//						}
		//					}
		//				});
		//				return regExpFilterText;
		//			}
		//		};
		//		toolMgr.add(x);

		toolMgr.add(stopQueryAction);

		if (!treeViewer.isRootTree()) {
			toolMgr.add(refreshViewAction);
			toolMgr.add(editQueryNodeAction);
			toolMgr.add(linkBrowserAction);
		}
	}

	private void doRenameBrowser() {
		String oldName = getPartName();
		InputDialog d = new InputDialog(JQueryTreeBrowserPlugin.getShell(), "Browser name", "Enter the new name for this browser view", oldName, null);
		if (d.open() == Window.OK) {
			String newName = d.getValue();
			renamePart(newName);
		}
	}

	/**
	 * Reloads the rules files for all projects
	 * 
	 * @author wannop
	 * @author lmarkle
	 */
	private void doReloadAllRulesAction() {
		JQueryTreeBrowserPlugin.traceUI("Reload All Sets' Rules Files");
		ActionsLog.logGeneric("All Rules reloaded");
		JQueryAPI.getFactBase().reloadRules();
	}

	private void doRefreshRuleBaseAction() {
		JQueryAPI.getFactBase().reloadFacts();
	}

	/**
	 * Restores the tab folder with saved queries.
	 * 
	 * @vsf+ persistence
	 */
	private void restoreImportantState(File file) {
		try {
			JQueryTreeBrowserPlugin.traceUI("Restoring saved state");
			JQueryObjectInputStream ois = new JQueryObjectInputStream(getClass().getClassLoader(), new BufferedInputStream(new FileInputStream(file)));

			try {
				setPartName(ois.readUTF());

				ResultsTreeNode n = (ResultsTreeNode) ois.readObject();
				if (n instanceof ResultsTreeRootNode) {
					((ResultsTreeRootNode) n).setElement(JQueryAPI.getFactBase());
				}

				rootNode = n;
				treeViewer.setInput(n);
			} finally {
				ois.close();
			}
		} catch (Throwable e) {
			JQueryTreeBrowserPlugin.error("An error occured while restoring JQuery's saved state: ", e);
		}
	}

	private void saveImportantState(File saveTreeFile) {
		try {
			JQueryTreeBrowserPlugin.traceUI("Saving state");
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveTreeFile)));

			try {
				oos.writeUTF(getPartName());
				oos.writeObject(treeViewer.getInput());
			} finally {
				oos.close();
			}
		} catch (IOException ioe) {
			JQueryTreeBrowserPlugin.error("Error saving tree: ", ioe);
			JQueryTreeBrowserPlugin.traceUI("Error saving tree: " + ioe.getMessage());
		}
	}

	/**
	 * @see IPartListener#partActivated
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part.equals(this)) {
			JQueryTreeBrowserPlugin.traceUI("JQueryView Activated");
		}
	}

	/**
	 * @see IPartListener#partBroughtToTop
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part.equals(this)) {
			JQueryTreeBrowserPlugin.traceUI("JQueryView BroughtToTop");
		}
	}

	/**
	 * @see IPartListener#partClosed
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(this)) {
			JQueryTreeBrowserPlugin.traceUI("JQueryView Closed");

			treeViewer.cancelAllJobs();
			if (renamePartJob != null) {
				renamePartJob.cancel();
			}
			if (refreshViewJob != null) {
				refreshViewJob.cancel();
			}
			//			File file = saveFile.toFile();
			//			saveImportantState(file);

			JQueryAPI.removeListener(this);
			linkBrowserAction.unlink();
		}
	}

	/**
	 * @see IPartListener#partDeactivated
	 */
	public void partDeactivated(IWorkbenchPart part) {
		if (part.equals(this)) {
			JQueryTreeBrowserPlugin.traceUI("JQueryView Deactivated");
		}
	}

	/**
	 * @see IPartListener#partOpened
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part.equals(this)) {
			JQueryTreeBrowserPlugin.traceUI("JQueryView Opened");
		}
	}

	public void dispose() {
		super.dispose();

		if (linkBrowserAction != null) {
			linkBrowserAction.unlink();
		}

		JQueryAPI.removeListener(this);

		busyCursor.dispose();
		normalCursor.dispose();

		JQueryAPI.removeUpdateTarget(getUpdateTarget());

		// TODO This will stop reset perspective from working but it clears out all the useless
		// files generated by old views.  I'm not sure what the best thing to do here is... 
		// perhaps I should move all the save information to IMemento's but that requires
		// serializing the tree structure.  Alternatively I could simply lose the tree structure. 
		// There doesn't seem to be a good option.  I'll keep it in mind.  For now, I'll put
		// up with a few hundred (or thousand) small files in favour of being able to restore
		// my perspective and see how it goes.
		//
		// erase save file (if the workbench is not closing)
		//		if (!PlatformUI.getWorkbench().isClosing()) {
		//			saveFile.toFile().delete();
		//		}
	}

	public static JQueryTreeView createTreeView(IWorkbenchPart origin, ResultsTreeNode node, boolean doDrag) throws PartInitException {
		IWorkbenchPage p = origin.getSite().getPage();
		JQueryTreeView v = (JQueryTreeView) p.showView(JQueryTreeBrowserPlugin.PLUGIN_ID, Integer.toString(JQueryTreeBrowserPlugin.PLUGIN_COUNTER), IWorkbenchPage.VIEW_CREATE);

		if (doDrag) {
			DragOperations.drag(origin, v, false);
		}

		ResultsTreeNode n = (ResultsTreeNode) node.clone();
		v.setTreeRoot(n);

		if (n instanceof QueryNode) {
			QueryNode qn = (QueryNode) n;
			v.treeViewer.execute(qn);
			// make sure we create a new update target for this view
			// v.updateTarget = null;
			v.browserUpdater.setQuery(qn);

			// use this line to remove a node from the old tree after you've created the view
			//			treeViewer.doDeleteQueryAction(new StructuredSelection(node));
		}

		p.activate(v);

		return v;
	}

	public JQueryTreeView createNewTreeView(ResultsTreeNode node, boolean doDrag) throws PartInitException {
		return JQueryTreeView.createTreeView(this, node, doDrag);
	}

	public QueryNodeUpdateJob getBrowserUpdater() {
		return browserUpdater;
	}

	public void setBrowserTarget(Object target) {
		setBrowserTarget(target, 0);
	}

	public void setBrowserTarget(Object target, int updateDelay) {
		browserUpdater.setQueryTarget(target);
		browserUpdater.schedule();
	}

	public void link(JQueryUpdateTarget[] targets) {
		if (targets != null) {
			linkBrowserAction.link(targets);
		}
	}

	public void unlink() {
		linkBrowserAction.unlink();
	}

	public ISelection getSelection() {
		return ((JQueryViewer) treeViewer).getSelection();
	}

	public void setSelection(ISelection s) {
		treeViewer.setSelection(s, true);
	}

	public void handleEvent(JQueryEvent e) {
		// TODO Should this be handled in the JQueryTreeViewer instead?
		if (e.getType().equals(JQueryEvent.EventType.Refresh) && autoRefreshAction.isChecked()) {
			if (refreshViewJob != null) {
				refreshViewJob.cancel();
			}

			// update the query
			Job j = new UIJob("Refresh View") {
				public IStatus runInUIThread(IProgressMonitor mon) {
					if (treeViewer.getTreeRoot() != null) {
						treeViewer.doReExecuteAction(new StructuredSelection(treeViewer.getTreeRoot()));
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
			refreshViewJob = j;
		}
	}

	private void createMenuGroups(IMenuManager menu) {
		if (!menu.isEmpty()) {
			return;
		}

		for (Iterator it = menuProviders.iterator(); it.hasNext();) {
			((JQueryMenuProvider) it.next()).addMenuGroup(menu);
		}
	}

	/**
	 * 
	 * @vsf+ contextMenu
	 */
	private void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		for (Iterator it = menuProviders.iterator(); it.hasNext();) {
			((JQueryMenuProvider) it.next()).addAvailableMenu(menu, selection);
		}
	}

	/**
	 * Creates the context menu for the results tree.
	 * 
	 * @vsf+ contextMenu
	 * @vsf+ treeviewer
	 */
	protected void createJQueryContextMenu() {
		// create context menu
		mnuMainContext = new MenuManager();
		mnuMainContext.setRemoveAllWhenShown(true);

		mnuMainContext.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menu) {
				JQueryTreeBrowserPlugin.getShell().setCursor(busyCursor);
				createMenuGroups(menu);
				fillContextMenu(menu);
				JQueryTreeBrowserPlugin.getShell().setCursor(normalCursor);
			}
		});

		Menu menu = mnuMainContext.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
		//		treeViewer.getControl().addMouseListener(new JQueryTreeViewMouseListener(this));
		//		treeViewer.getControl().addKeyListener(new JQueryTreeViewKeyListener(this));
		createJQueryMenuProviders();
	}

	protected void activateFilter() {
		regExpText.setFocus();
	}

	private void createJQueryMenuProviders() {
		menuProviders.add(new NodeSpecificMenuProvider(treeViewer, mnuMainContext));
		menuProviders.add(new TopQueryMenuProvider(treeViewer, mnuMainContext));
		menuProviders.add(new FilterMenuProvider(treeViewer, mnuMainContext, labelProvider));
		//		menuProviders.add(new InsertAnnotationMenuProvider(this, mnuMainContext));
		menuProviders.add(new GroupSpecificMenuProvider(treeViewer));

		// require that these menu providers have a JQueryTreeViewer as their structured viewer
		menuProviders.add(new DisplayItemMenuProvider(this, mnuMainContext, labelProvider, resultsTreeSorter));
		
		createJQueryExtensionMenuProviders();
		
		menuProviders.add(new EclipseMenuProvider(this));
	}
	
	private void createJQueryExtensionMenuProviders() {
		for (Object obj : JQueryTreeBrowserPlugin.getMenuProviderExtensions()) {
			LayeredMenuProviderFactory menuProviderFactory = (LayeredMenuProviderFactory)obj;
			menuProviders.add(menuProviderFactory.create(treeViewer, mnuMainContext));
		}
	}

	public StructuredViewer getViewer() {
		return treeViewer;
	}

	public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
		if (sourcepart.equals(this)) {

			// array of node.getElements() of all selected items
			List itemSelection = new ArrayList();
			// first item in a selection (to make sure we don't mix types)
			Object firstItem = null;

			IStructuredSelection sel = (IStructuredSelection) getSelection();
			firstItem = sel.getFirstElement();

			// build a list of all selected elements
			Iterator it = sel.iterator();
			for (int i = 0; it.hasNext(); i++) {
				ResultsTreeNode node = (ResultsTreeNode) it.next();
				Object t = node.getElement();
				itemSelection.add(t);

				// don't let this thing perform a selection with a group inside it.
				if (t instanceof Object[]) {
					return;
				}
			}

			if (!(firstItem instanceof QueryNode) && !itemSelection.isEmpty()) {
				Object[] targ = itemSelection.toArray();
				getUpdateTarget().updateTarget(targ);
			}
		}
	}

	public void renamePart(final String newName) {
		if (renamePartJob != null) {
			renamePartJob.cancel();
		}

		// make sure this executes in the UI thread
		renamePartJob = new UIJob("View Rename Job") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!treeViewer.isRootTree() && treeViewer.getTreeRoot() != null) {
					((QueryNode) treeViewer.getTreeRoot()).setLabel(newName);
				}

				setPartName(newName);
				//		getSite().getPage().removePostSelectionListener(getPartName(), this);
				getUpdateTarget().setName(newName);

				return Status.OK_STATUS;
			}
		};
		renamePartJob.setSystem(true);
		renamePartJob.schedule();
	}

	protected LinkBrowserAction getLinker() {
		return linkBrowserAction;
	}

	@Override
	public String toString() {
		return getPartName() + ": " + (treeViewer.isRootTree() ? "Root" : ((QueryNode) treeViewer.getTreeRoot()));
	}

	protected JQueryUpdateTarget getUpdateTarget() {
		if (updateTarget == null) {
			updateTarget = JQueryAPI.getUpdateTarget(getPartName());
		}

		return updateTarget;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public ResultsTreeNode getTreeRoot() {
		return (ResultsTreeNode) rootNode;
	}

	public boolean show(ShowInContext context) {
		try {
			Object[] value = null;

			if (context.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) context.getSelection();
				value = ss.toArray();
				for (int i = 0; i < value.length; i++) {
					if (value[i] instanceof IJavaElement) {
						value[i] = JQueryAPI.getElementFromJavaModel((IJavaElement) value[i]);
					}
				}
			} else if (context.getSelection() instanceof ITextSelection) {
				ITextSelection sel = (ITextSelection) context.getSelection();
				Set c = new HashSet();
				Set e = new HashSet();
				FileEditorInput b = (FileEditorInput) context.getInput();
				JQueryAPI.getElementFromFile(b.getToolTipText(), sel.getOffset(), sel.getLength(), c, e);
				value = c.toArray();
			}

			if (value[0] != null) {
				QueryNode qn = new QueryNode(JQueryAPI.getIdentityQuery(), JQueryAPI.getElementLabel(value[0]));
				qn.getQuery().bind(JQueryAPI.getThisVar(), value);
				createNewTreeView(qn, false);
				return true;
			}
		} catch (PartInitException ex) {
			return false;
		} catch (JQueryException ex) {
			return false;
		}

		return false;
	}

	public ShowInContext getShowInContext() {
		ISelection x = treeViewer.getJMSelection();
		return new ShowInContext(x, x);
	}
}
