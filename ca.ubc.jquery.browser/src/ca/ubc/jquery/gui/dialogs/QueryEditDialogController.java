package ca.ubc.jquery.gui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

public abstract class QueryEditDialogController {

	private static final int ErrorDisplayUpdateDelay = 1000;

	// ---- Non-SWT components
	// ------------------------------------------------------------------------------------/
	/** The Query object that this composite operates on */
	private JQuery query;

	/** Label for the query */
	private String queryLabel;

	/** A list of available variables, obtained from the Query object */
	private List varsAvailable = new ArrayList();

	private List removeFilters = new ArrayList();

	/**
	 * true if the Query object's query or variables have been modified during the lifetime of this dialog. Changes to the Query's label do not change this field, but can be detected using labelModified
	 */
	private boolean structureModified = false;

	/**
	 * true if the Query's label has been modified during the lifetime of this dialog.
	 */
	private boolean labelModified = false;

	private static final boolean UP = true;

	private static final boolean DOWN = false;

	// ---- SWT components
	// ----------------------------------------------------------------------------------------/

	//
	// Filters stuff
	//
	private org.eclipse.swt.widgets.List filtersList;

	private ContentProposalAdapter contentProposer;

	/**
	 * The table containing the checkable list of available variables. The order and number of checked variables determines the variables to be used when constructing a results tree.
	 */
	private CheckboxTableViewer varsTableViewer;

	/** Move one or more variables up or down in the table */
	private Button fUpButton;

	private Button fDownButton;

	private Text queryText;

	private Text queryTitle;

	private String errorMessage;

	private Job errorDisplayUpdater;

	private Button recursiveQueryCheck;

	private Combo recursiveQueryBox;

	// ---- Content/Label Provider
	// -----------------------------------------------------------------------------------/
	protected static class StringListDataProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {
		/** @see IStructuredContentProvider#getElements(java.lang.Object) */
		public Object[] getElements(Object inputElement) {
			return ((List) inputElement).toArray();
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

	}

	protected QueryEditDialogController(String queryLabel, JQuery query) {
		filtersList = null;
		errorMessage = null;
		errorDisplayUpdater = null;

		this.query = (JQuery) query.clone();
		this.queryLabel = queryLabel;
	}

	protected Control getFocusControl() {
		return queryText;
	}

	/**
	 * Reports that an error has occured in the query and should be displayed but it's up 
	 * to the class using this dialog to figure out how to display it.
	 * 
	 * Generally this method is more useful as a call back method.
	 * 
	 * @param error Error message to be displayed, null if none (can be used to cancel
	 * the previously displayed error message)
	 */
	protected abstract void onError(String error);

	/**
	 * Method is called when the dialog is closed.
	 */
	protected abstract void onClose();

	protected Control createFiltersArea(Composite parent) {
		Control result;

		Group grp = new Group(parent, SWT.SHADOW_NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		grp.setLayoutData(data);
		GridLayout layout = new GridLayout();
		grp.setText("Filters");
		grp.setLayout(layout);

		filtersList = new org.eclipse.swt.widgets.List(grp, SWT.BORDER | SWT.SINGLE);
		filtersList.setLayoutData(new GridData(GridData.FILL_BOTH));
		populateFiltersList();

		Button b = new Button(grp, SWT.PUSH);
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.setText("&Remove");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (filtersList.getSelectionCount() == 1) {
					removeFilters.add(filtersList.getSelection()[0]);
					query.removeFilter(filtersList.getSelection()[0]);
					populateFiltersList();
					structureModified = true;
				}
				filtersList.setFocus();
			}
		});

		result = grp;
		return result;
	}

	protected Control createQueryArea(Composite parent, boolean hasBorder) {
		int style = SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
		style |= (hasBorder) ? SWT.BORDER : 0;

		queryText = new Text(parent, style);
		GridData d = GridDataFactory.fillDefaults().create();
		d.grabExcessHorizontalSpace = true;
		d.grabExcessVerticalSpace = true;
		d.heightHint = 75;
		d.widthHint = 250;
		d.horizontalSpan = 2;
		queryText.setLayoutData(d);
		queryText.setText(query.getString());
		// enable tab to switch components instead of inserting tab character into text
		// who wants a tab in their query anyway?
		queryText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});
		queryText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (errorDisplayUpdater != null) {
					errorDisplayUpdater.cancel();
				}

				errorDisplayUpdater = new UIJob("update query") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						updateQuery();
						return Status.OK_STATUS;
					}
				};
				errorDisplayUpdater.setSystem(true);
				errorDisplayUpdater.schedule(ErrorDisplayUpdateDelay);

				onError(null);
			}
		});

		if (JQueryTreeBrowserPlugin.useAutoComplete()) {
			IControlContentAdapter tc = new TextContentAdapter();
			IContentProposalProvider cp = new JQueryEditorContentProposer();
			contentProposer = new ContentProposalAdapter(queryText, tc, cp, null, null);
			createQueryContextMenu(queryText);
			contentProposer.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}

		return queryText;
	}

	protected void updateQuery() {
		try {
			query.setString(queryText.getText());
			doGetVariablesAction(true);

			onError(null);
		} catch (Throwable e) {
			onError(e.getMessage());
		}
	}

	protected void setAutoHelpDelay(int delay) {
		if (contentProposer != null) {
			contentProposer.setAutoActivationDelay(delay);
		}
	}

	protected Control createTitleArea(Composite parent, boolean hasBorder) {
		Control result;

		int style = SWT.NONE;
		style |= (hasBorder) ? SWT.BORDER : 0;

		queryTitle = new Text(parent, style);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.verticalAlignment = SWT.CENTER;
		data.widthHint = 200;
		queryTitle.setLayoutData(data);
		queryTitle.setText(queryLabel);
		queryTitle.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				queryLabel = queryTitle.getText();
				labelModified = true;
			}
		});

		result = queryTitle;
		return result;
	}

	protected Control createRecursionArea(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().create();
		layout.numColumns = 2;
		GridData data = GridDataFactory.fillDefaults().create();
		result.setLayout(layout);
		result.setLayoutData(data);

		recursiveQueryCheck = new Button(result, SWT.CHECK);
		data = new GridData(SWT.NONE, SWT.NONE, false, false);
		recursiveQueryCheck.setLayoutData(data);
		recursiveQueryCheck.setText("Recursive &query");
		recursiveQueryCheck.setSelection(query.isRecursive());
		recursiveQueryCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				recursiveQueryBox.setVisible(recursiveQueryCheck.getSelection());
				if (recursiveQueryBox.isVisible()) {
					recursiveQueryBox.setFocus();
				}

				structureModified = true;
			}
		});

		recursiveQueryBox = new Combo(result, SWT.SINGLE);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.widthHint = 100;
		recursiveQueryBox.setLayoutData(data);
		recursiveQueryBox.setVisible(query.isRecursive());
		recursiveQueryBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				structureModified = true;
			}
		});

		return result;
	}

	protected Control createEditVariablesArea(Composite parent) {
		Control result;

		Group varGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		varGroup.setText("Order of Variables");
		GridLayout layout = new GridLayout(2, false);
		varGroup.setLayout(layout);

		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		varGroup.setLayoutData(data);

		varsTableViewer = CheckboxTableViewer.newCheckList(varGroup, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		StringListDataProvider provider = new StringListDataProvider();
		varsTableViewer.setContentProvider(provider);
		varsTableViewer.setLabelProvider(provider);
		varsTableViewer.setInput(varsAvailable);

		data = new GridData(GridData.FILL_BOTH);

		data.horizontalSpan = 1;
		data.verticalSpan = 3;
		varsTableViewer.getTable().setLayoutData(data);

		varsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				doRefreshButtonsAction(varsTableViewer, fUpButton, fDownButton);
			}
		});

		// create buttons for modifying variables list

		// "move up" button
		fUpButton = new Button(varGroup, SWT.PUSH);
		fUpButton.setText("Move &up");
		fUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doMoveUpAction(varsAvailable, varsTableViewer, fUpButton, fDownButton);
				varsTableViewer.getControl().setFocus();
				structureModified = true;
			}
		});
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		fUpButton.setLayoutData(data);

		// "move down" button
		fDownButton = new Button(varGroup, SWT.PUSH);
		fDownButton.setText("Move &down");
		fDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doMoveDownAction(varsAvailable, varsTableViewer, fUpButton, fDownButton);
				varsTableViewer.getControl().setFocus();
				structureModified = true;
			}
		});
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		fDownButton.setLayoutData(data);

		result = varGroup;
		return result;
	}

	protected void finalizeDialogSetup() {
		// Synchronize dialog with the Query
		try {
			doRestoreVarsAction();
			onError(null);
		} catch (Throwable e) {
			onError(e.getMessage());
		}
		setupRecursiveQueryBox();

		doRefreshButtonsAction(varsTableViewer, fUpButton, fDownButton);

		// Listen for changes in variables' "checked" state
		varsTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				structureModified = true;
				doSetVariablesAction(query);
				setupRecursiveQueryBox();
				onError(null);
			}
		});
	}

	protected void applyChanges(JQuery query) throws Throwable {
		if (errorMessage == null && isLabelModified()) {
			queryLabel = queryTitle.getText();
		}

		if (errorMessage == null && isStructureModified()) {
			query.setString(queryText.getText());
			if (recursiveQueryBox.isVisible()) {
				Combo b = recursiveQueryBox;
				String item = b.getItem(b.getSelectionIndex());
				query.setRecursiveVar(item);
			} else {
				query.setRecursiveVar(null);
			}

			for (Iterator it = removeFilters.iterator(); it.hasNext();) {
				query.removeFilter((String) it.next());
			}

			doSetVariablesAction(query);
		}

		close();
		onClose();
	}

	protected void setTabOrder(Composite composite) {
		// set tab order for whole view
		composite.setTabList(new Control[] { queryText.getParent() });

		// set tab order for editing part
		composite = queryText.getParent();
		if (filtersList == null) {
			composite.setTabList(new Control[] { queryTitle.getParent(), queryText, varsTableViewer.getControl().getParent(), recursiveQueryCheck.getParent() });
		} else {
			composite.setTabList(new Control[] { queryTitle.getParent(), queryText, filtersList.getParent(), varsTableViewer.getControl().getParent(), recursiveQueryCheck.getParent() });

			// set tab order for filters list
			composite = filtersList.getParent();
			composite.setTabList(new Control[] { filtersList });
		}

		// set tab order for vars table part
		composite = varsTableViewer.getControl().getParent();
		composite.setTabList(new Control[] { varsTableViewer.getControl() });
	}

	protected void close() {
		if (errorDisplayUpdater != null) {
			errorDisplayUpdater.cancel();
		}
	}

	private void setupRecursiveQueryBox() {
		// clear old items
		recursiveQueryBox.setItems(new String[0]);

		// update display
		try {
			Object[] x = query.getChosenVars().toArray();
			for (int i = 0; i < x.length; i++) {
				recursiveQueryBox.add(x[i].toString());
			}

			// update selection
			String[] y = recursiveQueryBox.getItems();
			for (int i = 0; i < y.length; i++) {
				if (y[i].equals(query.getRecursiveVar())) {
					recursiveQueryBox.select(i);
					return;
				}
			}
			recursiveQueryBox.select(0);
			onError(null);
		} catch (Throwable e) {
			onError(e.getMessage());
		}
	}

	private void populateFiltersList() {
		//		QueryResultsAndFilter qrf = (QueryResultsAndFilter) query;
		filtersList.removeAll();
		for (Iterator it = query.getFilters().iterator(); it.hasNext();) {
			filtersList.add((String) it.next());
		}
	}

	private JQuery getQuery() {
		return query;
	}

	/**
	 * Method doRestoreVarsAction. Obtains the current list of variables from the previously constructed Query, and restores the order and check state of previously selected vars. Removed variables will be removed from the table view, even
	 * if they are checked. Unaffected variables will maintain their checked state.
	 */
	private void doRestoreVarsAction() throws JQueryException {
		List varsSelected = query.getChosenVars();

		// remove the variables if they already exist in the list
		varsAvailable.removeAll(varsSelected);
		varsAvailable.addAll(0, varsSelected);
		varsTableViewer.refresh(false);
		checkVariables(varsSelected);
		doGetVariablesAction(false);
	}

	private Set getVarsFromQuery(JQuery q) throws JQueryException {
		HashSet result = new HashSet();

		String[] t;
		t = q.getVariables();
		if (t != null) {
			for (int i = 0; i < t.length; i++) {
				result.add(t[i]);
			}

			if (q.getString().contains(JQueryAPI.getThisVar())) {
				result.add(JQueryAPI.getThisVar());
			}
		}

		return result;
	}

	/**
	 * Method doGetVariablesAction. Obtains the current list of variables from the Query. Removed variables will be removed from the table view, even if they are checked. Added variables will be added, and "checked" if checkNewVars = true.
	 * Unaffected variables will maintain their checked state.
	 */
	private void doGetVariablesAction(boolean checkNewVars) throws JQueryException {
		Set updatedVars = getVarsFromQuery(query);

		// /Remove variables no longer present in query
		HashSet removedVars = new HashSet(varsAvailable);
		removedVars.removeAll(updatedVars);
		varsAvailable.removeAll(removedVars);

		// Add newly occuring variables
		HashSet newVars = new HashSet(updatedVars);
		newVars.removeAll(varsAvailable);
		varsAvailable.addAll(newVars);

		// refresh the table
		varsTableViewer.refresh(false);
		setupRecursiveQueryBox();

		doRefreshButtonsAction(varsTableViewer, fUpButton, fDownButton);
	}

	/** Sets each of the given collection of variables in the TableViewer checked */
	private void checkVariables(Collection vars) {
		if (!vars.isEmpty()) {
			for (Iterator iter = vars.iterator(); iter.hasNext();) {
				varsTableViewer.setChecked(iter.next(), true);
			}
		}
	}

	private void doSetVariablesAction(JQuery query) {
		try {
			doGetVariablesAction(true);
			List checkedVars = Arrays.asList(varsTableViewer.getCheckedElements());
			query.setChosenVars(checkedVars);
			structureModified = true;
			onError(null);
		} catch (Throwable e) {
			onError(e.getMessage());
		}
	}

	// ---- change order
	// ----------------------------------------------------------------------------------------
	/**
	 * Finds the items that are currently "selected" in the list. Note that an item being "selected" is not the same as an item being "checked"
	 */
	private String[] getSelectedItems(TableViewer viewer) {
		ISelection selection = viewer.getSelection();
		if (selection == null)
			return new String[0];

		if (!(selection instanceof IStructuredSelection))
			return new String[0];

		List selected = ((IStructuredSelection) selection).toList();
		return (String[]) selected.toArray(new String[selected.size()]);
	}

	private boolean canMove(TableViewer viewer, boolean up) {

		Table table = viewer.getTable();
		int[] indc = table.getSelectionIndices();
		if (indc.length == 0)
			return false;
		int invalid = up ? 0 : table.getItemCount() - 1;
		for (int i = 0; i < indc.length; i++) {
			if (indc[i] == invalid)
				return false;
		}
		return true;
	}

	protected void doMoveUpAction(List set, TableViewer viewer, Button up, Button down) {
		moveUp(set, getSelectedItems(viewer));
		viewer.refresh(false);
		doRefreshButtonsAction(viewer, up, down);

		doSetVariablesAction(query);
		setupRecursiveQueryBox();
	}

	protected void doMoveDownAction(List set, TableViewer viewer, Button up, Button down) {
		moveDown(set, getSelectedItems(viewer));
		viewer.refresh(false);
		doRefreshButtonsAction(viewer, up, down);

		doSetVariablesAction(query);
		setupRecursiveQueryBox();
	}

	protected void doRefreshButtonsAction(TableViewer viewer, Button upButton, Button downButton) {
		if (canMove(viewer, UP)) {
			upButton.setEnabled(true);
		} else {
			upButton.setEnabled(false);
		}
		if (canMove(viewer, DOWN)) {
			downButton.setEnabled(true);
		} else {
			downButton.setEnabled(false);
		}
	}

	private void moveUp(List set, String[] selection) {
		moveUp(set, Arrays.asList(selection));
	}

	private void moveDown(List set, String[] selection) {
		Collections.reverse(set);
		moveUp(set, Arrays.asList(selection));
		Collections.reverse(set);
	}

	private static void moveUp(List elements, List move) {
		List res = new ArrayList(elements.size());
		Object floating = null;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			Object curr = iter.next();
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null)
					res.add(floating);
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		elements.clear();
		for (Iterator iter = res.iterator(); iter.hasNext();) {
			elements.add(iter.next());
		}
	}

	/**
	 * Creates the context menu for the query text widget.
	 * 
	 * @author wannop
	 */
	private void createQueryContextMenu(Text control) {
		// Menu textMenu = query.getMenu();
		MenuManager textMenuMgr = new MenuManager();
		textMenuMgr.setRemoveAllWhenShown(true);
		textMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Vector menuActions = getQueryMenuItems();
				Iterator items = menuActions.iterator();
				while (items.hasNext()) {
					manager.add((Action) items.next());
				}
			}
		});

		Menu defQMenu = textMenuMgr.createContextMenu(control);
		control.setMenu(defQMenu);
	}

	/**
	 * Method createQueryMenuItems.
	 * 
	 * @param defQMenu
	 */
	private Vector getQueryMenuItems() {
		Vector items = new Vector();

		SortedMap snippets = new TreeMap();
		JQueryResultSet results = null;

		try {
			JQuery q = JQueryAPI.queryPredicates();
			results = q.execute();

			while (results.hasNext()) {
				JQueryResult result = results.next();
				Object[] value = result.get();
				// get the query snippet
				String rule = (String) value[0];
				// get the tool tip label
				String toolTip = (String) value[1];

				snippets.put(rule, toolTip);
			}

			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {

				String rule = (String) iter.next();
				String toolTip = (String) snippets.get(rule);

				// create the menu item
				Action action = new Action(rule) {
					public void run() {
						queryText.insert(this.getText());
					}
				};
				action.setToolTipText(toolTip);
				// add action to the list
				items.add(action);
			}
			onError(null);
		} catch (Throwable e) {
			// this shouldn't happen unless rules are written incorrectly
			onError(e.getMessage());
		} finally {
			if (results != null) {
				results.close();
			}
		}

		return items;
	}

	/**
	 * @return true iff the Query's label has been modified during the lifetime of this dialog.
	 */
	protected boolean isLabelModified() {
		return labelModified;
	}

	/**
	 * @return true iff the Query's query text or variables have been modified during the lifetime of this dialog. Changes to the Query's label do not change this field, but can be detected using labelModified
	 */
	protected boolean isStructureModified() {
		return structureModified;
	}

	protected String getQueryLabel() {
		return queryLabel;
	}
}
