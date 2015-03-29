package ca.ubc.jquery.gui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.QueryNodeUpdateJob;
import ca.ubc.jquery.gui.views.JQueryTreeView;

public class LinkBrowserDialogController {
	// ---- Non-SWT components
	// ------------------------------------------------------------------------------------/
	/** A list of available browsers to link to */
	private List availableBrowsers;

	private JQueryUpdateTarget[] checkedParts;

	private QueryNodeUpdateJob browserUpdater;

	private String inputFilter;

	private String selectionFilter;

	private JQueryTreeView view;

	private Pattern filterText;

	//	private static final boolean UP = true;
	//
	//	private static final boolean DOWN = false;

	// ---- SWT components
	// ----------------------------------------------------------------------------------------/
	/**
	 * The table containing the checkable list of available variables. The order and number of checked variables determines the variables to be used when constructing a results tree.
	 */
	private CheckboxTableViewer viewsTable;

	//	/** Move one or more variables up or down in the table */
	//	private Button fUpButton;
	//
	//	private Button fDownButton;

	private Combo cboLinkFilter;

	private Combo cboSelectionFilter;

	// ---- Content/Label Provider
	// -----------------------------------------------------------------------------------/
	private static class LinkOptionsDataProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {
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
			return ((JQueryUpdateTarget) element).getName();
		}

	}

	// ---- Constructor
	// ----------------------------------------------------------------------------------------------/
	/**
	 * @param selected Is the selected parts in the dialog
	 */
	public LinkBrowserDialogController(JQueryTreeView view, List selected, QueryNodeUpdateJob browserUpdater) {
		availableBrowsers = new ArrayList();

		this.browserUpdater = browserUpdater;
		inputFilter = browserUpdater.getInputFilter();
		selectionFilter = browserUpdater.getSelectionFilter();

		checkedParts = new JQueryUpdateTarget[selected.size()];
		Iterator it = selected.iterator();
		for (int i = 0; it.hasNext(); i++) {
			checkedParts[i] = (JQueryUpdateTarget) it.next();
		}

		this.view = view;
		filterText = null;
	}

	protected void applyChanges() {
		if (checkedParts.length > 0) {
			view.link(checkedParts);
		} else {
			// use this to clear old selection (if necessary, I'm not sure it is)
			// view.link(checkedParts);
			view.unlink();
		}

		browserUpdater.setInputFilter(inputFilter);
		browserUpdater.setSelectionFilter(selectionFilter);
	}

	protected Composite createBrowserListTable(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().create());

		GridData data;

		Text filter = new Text(composite, SWT.BORDER);
		data = GridDataFactory.fillDefaults().create();
		filter.setLayoutData(data);
		filter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					filterText = Pattern.compile(".*(" + ((Text) e.widget).getText() + ").*");
					viewsTable.refresh();
				} catch (PatternSyntaxException ex) {
					// ignore this exception
				}
			}
		});
		filter.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
					viewsTable.getControl().setFocus();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		viewsTable = CheckboxTableViewer.newCheckList(composite, SWT.V_SCROLL | SWT.BORDER);
		LinkOptionsDataProvider provider = new LinkOptionsDataProvider();
		viewsTable.setContentProvider(provider);
		viewsTable.setLabelProvider(provider);
		viewsTable.setInput(availableBrowsers);
		viewsTable.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getParts();
			}
		});
		viewsTable.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				String name = ((JQueryUpdateTarget) element).getName();
				if (filterText == null) {
					return true;
				} else {
					return filterText.matcher(name).matches();
				}
			}
		});

		data = GridDataFactory.fillDefaults().create();
		viewsTable.getTable().setLayoutData(data);

		populateLinkList();
		restoreSelectedParts(checkedParts);
		filter.setText("");

		return composite;
	}

	protected Control createLinkFilter(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().create());

		Label lbl = new Label(composite, SWT.NULL);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		lbl.setLayoutData(data);
		lbl.setText("Link Input Filter");

		cboLinkFilter = new Combo(composite, SWT.NONE);
		cboLinkFilter.setText("(none)");
		cboLinkFilter.setLayoutData(new GridData(GridData.FILL_BOTH));
		cboLinkFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String obj = cboLinkFilter.getItem(cboLinkFilter.getSelectionIndex());
				if (("(none)").equals(obj)) {
					inputFilter = "";
				} else {
					inputFilter = JQueryAPI.getStringProperty(obj, "updateTargetFilter");
				}
			}
		});

		return composite;
	}

	protected Control createSelectionFilter(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().create());

		Label lbl = new Label(composite, SWT.NULL);
		lbl.setLayoutData(new GridData(GridData.FILL_BOTH));
		lbl.setText("Item selection Filter");

		cboSelectionFilter = new Combo(composite, SWT.NONE);
		cboSelectionFilter.setText("(none)");
		cboSelectionFilter.setLayoutData(new GridData(GridData.FILL_BOTH));
		cboSelectionFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String obj = cboSelectionFilter.getItem(cboSelectionFilter.getSelectionIndex());
				if (("(none)").equals(obj)) {
					selectionFilter = "";
				} else {
					selectionFilter = JQueryAPI.getStringProperty(obj, "updateTargetFilter");
				}
			}
		});

		return composite;
	}

	protected void finalizeSetup() {
		populateFilterComboBoxes();

	}

	private void populateFilterComboBoxes() {
		JQueryResultSet rs = null;

		try {
			// populate the combo boxes
			JQueryUpdateTarget.Filter[] filters = JQueryAPI.getUpdateTarget("Editor").getFilters();

			cboLinkFilter.add("(none)");
			cboSelectionFilter.add("(none)");

			for (int i = 0; i < filters.length; i++) {
				cboLinkFilter.add(filters[i].name);
				cboSelectionFilter.add(filters[i].name);

				if (filters[i].filter.equals(inputFilter)) {
					cboLinkFilter.select(i + 1);
				}

				if (filters[i].filter.equals(selectionFilter)) {
					cboSelectionFilter.select(i + 1);
				}
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Getting input and selection filters: ", e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	private void restoreSelectedParts(JQueryUpdateTarget[] part) {
		List selected = Arrays.asList(part);

		availableBrowsers.removeAll(selected);
		availableBrowsers.addAll(0, selected);
		for (int i = 0; i < part.length; i++) {
			viewsTable.setChecked(part[i], true);
		}
		viewsTable.refresh(false);
	}

	private void getParts() {
		List checked = new ArrayList();

		for (Iterator it = availableBrowsers.iterator(); it.hasNext();) {
			Object temp = it.next();
			if (viewsTable.getChecked(temp)) {
				checked.add(temp);
			}
		}

		checkedParts = new JQueryUpdateTarget[checked.size()];
		Iterator it = checked.iterator();
		for (int i = 0; it.hasNext(); i++) {
			checkedParts[i] = (JQueryUpdateTarget) it.next();
		}
	}

	private String[] getSelectedItems() {
		ISelection selection = viewsTable.getSelection();
		if (selection == null)
			return new String[0];

		if (!(selection instanceof IStructuredSelection))
			return new String[0];

		List selected = ((IStructuredSelection) selection).toList();
		return (String[]) selected.toArray(new String[selected.size()]);
	}

	private void populateLinkList() {
		availableBrowsers.clear();

		for (Iterator it = JQueryAPI.getUpdateTargets().iterator(); it.hasNext();) {
			availableBrowsers.add(it.next());
		}
		viewsTable.refresh();
	}
}
