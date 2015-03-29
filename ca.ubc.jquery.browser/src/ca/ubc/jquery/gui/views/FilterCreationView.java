package ca.ubc.jquery.gui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.dnd.JQueryFilterTransfer;
import ca.ubc.jquery.gui.dnd.JQueryResultsDragListener;
import ca.ubc.jquery.gui.dnd.JQueryResultsTreeTransfer;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class FilterCreationView extends ViewPart {
	private class FiltersTableLabelProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String name = ((Map.Entry) element).getKey().toString();
			return name;
		}

		public Object[] getElements(Object inputElement) {
			return ((Map) inputElement).entrySet().toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class VarsTableLabelProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String name = element.toString();
			if (variableValues.containsKey(name)) {
				return name + " bound to " + variableValueToString(name);
			} else {
				return name;
			}
		}

		public Object[] getElements(Object inputElement) {
			return ((List) inputElement).toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private final static int MaxErrorLabelStringLength = 40;

	//
	// SWT Stuff
	//
	private Text filterName;

	private Button createFilter;

	private Text filterText;

	private TableViewer varsTable;

	private TableViewer filtersTable;

	private Button deleteFilter;

	private Composite errorDisplay;

	private Label errorLabel;

	// 
	// Other Fields
	//
	private List variables;

	private Map filters;

	private Map variableValues;

	private Job updateJob;

	private JQuery filter;

	public FilterCreationView() {
		try {
			filter = JQueryAPI.createQuery("");
		} catch (JQueryException e) {
			// should never happen anyway...
			JQueryTreeBrowserPlugin.error("Creating filter: ", e);
		}

		variables = new ArrayList();
		variableValues = new HashMap();

		filters = JQueryTreeBrowserPlugin.getFiltersList();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory layoutFactory = GridLayoutFactory.fillDefaults();
		GridDataFactory dataFactory = GridDataFactory.fillDefaults();

		dataFactory.grab(true, false);

		GridLayout layout = layoutFactory.create();
		GridData data = dataFactory.create();

		parent.setLayout(layout);
		data.grabExcessVerticalSpace = true;
		parent.setLayoutData(data);

		//
		// Error display part
		//
		errorDisplay = new Composite(parent, SWT.NONE);
		layout = layoutFactory.create();
		layout.numColumns = 2;
		errorDisplay.setLayout(layout);
		errorDisplay.setLayoutData(dataFactory.create());
		errorDisplay.setVisible(false);

		Label errorImage = new Label(errorDisplay, SWT.NONE);
		errorImage.setImage(JQueryAPI.getElementImage("Error"));

		errorLabel = new Label(errorDisplay, SWT.NONE);
		data = dataFactory.create();
		errorLabel.setLayoutData(data);

		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		data = dataFactory.create();
		data.grabExcessVerticalSpace = true;
		bar.setLayoutData(data);

		errorDisplay.setBackground(bar.getBackground());
		parent.setBackground(bar.getBackground());
		errorLabel.setBackground(bar.getBackground());
		errorImage.setBackground(bar.getBackground());

		//
		// Filter definition part
		//
		Composite composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		data = dataFactory.create();
		composite.setLayout(layout);
		composite.setLayoutData(data);

		filterName = new Text(composite, SWT.BORDER);
		data = dataFactory.create();
		filterName.setLayoutData(data);

		createFilter = new Button(composite, SWT.PUSH);
		data = dataFactory.create();
		createFilter.setLayoutData(data);
		createFilter.setText("Create");
		createFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					doCreateFilterAction();
				} catch (JQueryException ex) {
					JQueryTreeBrowserPlugin.error("Cannot create filter: ", ex);
				}
			}
		});

		filterText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		data = dataFactory.create();
		data.horizontalSpan = 2;
		data.heightHint = 100;
		filterText.setLayoutData(data);
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modifyFilterText();
			}
		});

		Group grp = new Group(composite, SWT.SHADOW_NONE);
		grp.setText("Free variables");
		layout = new GridLayout();
		grp.setLayout(layout);
		data = dataFactory.create();
		data.horizontalSpan = 2;
		grp.setLayoutData(data);

		varsTable = new TableViewer(grp, SWT.MULTI | SWT.BORDER);
		VarsTableLabelProvider provider = new VarsTableLabelProvider();
		varsTable.setLabelProvider(provider);
		varsTable.setContentProvider(provider);
		varsTable.setInput(variables);
		data = dataFactory.create();
		varsTable.getControl().setLayoutData(data);

		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		Transfer[] tt = new Transfer[] { JQueryResultsTreeTransfer.getInstance(), PluginTransfer.getInstance() };
		varsTable.addDropSupport(ops, tt, new ViewerDropAdapter(varsTable) {
			private String target;

			public boolean performDrop(Object data) {
				if (target != null) {
					ResultsTreeNode[] t = (ResultsTreeNode[]) data;
					Object[] d = new Object[t.length];
					for (int i = 0; i < t.length; i++) {
						d[i] = t[i].getElement();
					}

					variableValues.put(target, d);
					varsTable.refresh();
					return true;
				} else {
					return false;
				}
			}

			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				if (target != null && JQueryResultsTreeTransfer.getInstance().isSupportedType(transferType)) {
					this.target = target.toString();
					return true;
				} else {
					this.target = null;
					return false;
				}
			}
		});

		ExpandItem item = new ExpandItem(bar, SWT.NONE);
		item.setText("Create Filter");
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(composite);

		//		Button bind = new Button(grp, SWT.PUSH);
		//		data = dataFactory.create();
		//		bind.setLayoutData(data);
		//		bind.setText("&Bind");
		//		bind.addSelectionListener(new SelectionAdapter() {
		//			@Override
		//			public void widgetSelected(SelectionEvent e) {
		//				doBindVariableAction();
		//			}
		//		});

		//
		// Filter Display part
		// 
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout();
		data = dataFactory.create();
		composite.setLayout(layout);
		composite.setLayoutData(data);

		filtersTable = new TableViewer(composite, SWT.MULTI | SWT.BORDER);
		FiltersTableLabelProvider fprovider = new FiltersTableLabelProvider();
		filtersTable.setLabelProvider(fprovider);
		filtersTable.setContentProvider(fprovider);
		filtersTable.setInput(filters);
		data = dataFactory.create();
		data.grabExcessVerticalSpace = true;
		filtersTable.getControl().setLayoutData(data);
		filtersTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (filtersTable.getSelection().isEmpty()) {
					deleteFilter.setEnabled(false);
				} else {
					deleteFilter.setEnabled(true);
				}

				doEditFilter();
			}
		});

		deleteFilter = new Button(composite, SWT.PUSH);
		data = dataFactory.create();
		deleteFilter.setLayoutData(data);
		deleteFilter.setText("&Delete");
		deleteFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doDeleteFilterAction((IStructuredSelection) filtersTable.getSelection());
			}
		});

		ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		tt = new Transfer[] { JQueryFilterTransfer.getInstance() };
		DragSourceListener ds = new JQueryResultsDragListener(filtersTable);
		filtersTable.addDragSupport(ops, tt, ds);

		item = new ExpandItem(bar, SWT.NONE);
		item.setText("Filters");
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(composite);
		item.setExpanded(true);

		populateFiltersList();
	}

	@Override
	public void setFocus() {
	}

	private String variableValueToString(String name) {
		Object[] value = (Object[]) variableValues.get(name);
		StringBuilder b = new StringBuilder("[");

		for (int i = 0; i < value.length; i++) {
			b.append(value[i]);
			if (i + 1 < value.length) {
				b.append(",");
			}
		}

		b.append("]");
		return b.toString();
	}

	private void populateFiltersList() {

	}

	private void doCreateFilterAction() throws JQueryException {
		for (Iterator it = variableValues.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			filter.bind((String) e.getKey(), e.getValue());
		}

		String name = filterName.getText();
		// remove old filter of the same name...
		if (filters.get(name) != null) {
			MessageDialog d = new MessageDialog(JQueryTreeBrowserPlugin.getShell(), "Overwrite duplicate", null, "A filter with the same name already exists, do you want to overwrite it?", 0, new String[] { "Yes", "No" }, 1);
			if (d.open() == Window.OK) {
				filters.remove(name);
			} else {
				// abort this method if the user doesn't want to overwrite
				return;
			}
		}

		filters.put(name, new Object[] { name, filter.clone(), variableValues });
		variableValues = new HashMap();
		filterName.setText("");
		filterText.setText("");
		variables.clear();

		filtersTable.refresh();
		varsTable.refresh();
	}

	private void doDeleteFilterAction(IStructuredSelection selection) {
		for (Iterator its = selection.iterator(); its.hasNext();) {
			Map.Entry o = (Map.Entry) its.next();
			filters.remove(o.getKey());
		}

		filtersTable.refresh();
	}

	private void doEditFilter() {
		IStructuredSelection ss = (IStructuredSelection) filtersTable.getSelection();

		if (ss.size() == 1) {
			Map.Entry e = (Map.Entry) ss.getFirstElement();
			Object[] selection = (Object[]) e.getValue();

			if (ss.size() == 1) {
				JQuery q = (JQuery) selection[1];
				String name = selection[0].toString();

				filterName.setText(name);
				filterText.setText(q.getString());
				variableValues = (HashMap) selection[2];

				getVariables(q);
				varsTable.refresh();
			}
		}
	}

	private void getVariables(JQuery query) {
		try {
			List updatedVars = Arrays.asList(query.getVariables());

			HashSet removedVars = new HashSet(variables);
			removedVars.removeAll(updatedVars);
			variables.removeAll(removedVars);

			// Add newly occuring variables
			HashSet newVars = new HashSet(updatedVars);
			newVars.removeAll(variables);
			variables.addAll(newVars);

			// refresh the table
			varsTable.refresh(false);

			handleError(null);
		} catch (JQueryException e) {
			handleError(e.getMessage());
		}
	}

	private void modifyFilterText() {
		if (updateJob != null) {
			updateJob.cancel();
		}

		updateJob = new UIJob("Update filter query") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					filter.setString(filterText.getText());
					handleError(null);

					getVariables(filter);
				} catch (JQueryException e) {
					handleError(e.getMessage());
				}
				return Status.OK_STATUS;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule(1000);
	}

	private void handleError(String error) {
		if (error == null) {
			// cancel error display
			errorLabel.setText("");
			errorDisplay.setVisible(false);
		} else {
			// display error message (somewhere...)
			errorDisplay.setVisible(true);
			errorLabel.setToolTipText(error);

			error = (error.length() > MaxErrorLabelStringLength) ? error.substring(0, MaxErrorLabelStringLength) + "..." : error;
			errorLabel.setText(error);
		}
	}
}
