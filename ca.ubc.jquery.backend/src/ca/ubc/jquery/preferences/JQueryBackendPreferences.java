package ca.ubc.jquery.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryAPI;

public class JQueryBackendPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * JQuery Backend Preferences
	 */
	public static final String P_DEBUG_UI = "debugUI";

	public static final String P_DEBUG_QUERY = "debugQueries";

	public static final String P_LOG_QUERIES = "logQueries";

	public static final String P_JQUERY_BACKEND_FACTBASE = "Factbase";

	public static final String P_INSTALLED_RESOURCES = "ca.ubc.jquery.resources.installed";

	private org.eclipse.swt.widgets.List backendExtensions;

	private CheckboxTableViewer resourceExtensions;

	private boolean updateResources;

	private Button debugQuery;

	private Button debugUI;

	private Button logQuery;

	public JQueryBackendPreferences() {
		setDescription("Preferences for the JQuery Plugin");
	}

	public JQueryBackendPreferences(String title) {
		super(title);
	}

	public JQueryBackendPreferences(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		//Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Group grp = new Group(entryTable, SWT.SHADOW_NONE);
		grp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grp.setText("Backend Selection");

		layout = new GridLayout();
		grp.setLayout(layout);

		// 
		// Backend selection components
		//
		backendExtensions = new org.eclipse.swt.widgets.List(grp, SWT.BORDER);

		List l = JQueryBackendPlugin.getListOfAPIs();
		for (Iterator it = l.iterator(); it.hasNext();) {
			backendExtensions.add((String) it.next());
		}
		selectBackendByName(backendExtensions);
		backendExtensions.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button setBackend = new Button(grp, SWT.PUSH);
		setBackend.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END, GridData.VERTICAL_ALIGN_END, false, false));
		setBackend.setText("Select backend");
		setBackend.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String api = backendExtensions.getItem(backendExtensions.getSelectionIndex());
				JQueryBackendPlugin.activateAPI(api);
				getPreferenceStore().setValue(P_JQUERY_BACKEND_FACTBASE, api);
			}
		});

		// 
		// Resource extension selection components
		//
		resourceExtensions = CheckboxTableViewer.newCheckList(grp, SWT.BORDER | SWT.CHECK);
		updateResources = false;

		for (Iterator it = JQueryBackendPlugin.getListOfResourceExtensions().iterator(); it.hasNext();) {
			resourceExtensions.add(it.next());
		}
		selectInstalledResources(resourceExtensions);
		resourceExtensions.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		resourceExtensions.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				StringBuilder newPreference = new StringBuilder();
				String[] res = getUnSelectedResources();
				for (int i = 0; i < res.length; i++) {
					newPreference.append(res[i]);
					newPreference.append(";");
				}

				getPreferenceStore().setValue(P_INSTALLED_RESOURCES, newPreference.toString());
				updateResources = true;
			}
		});

		//
		// Logging/info components
		//
		grp = new Group(entryTable, SWT.SHADOW_NONE);
		grp.setText("Logging");
		grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layout = new GridLayout();
		grp.setLayout(layout);

		debugQuery = new Button(grp, SWT.CHECK);
		debugQuery.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		debugQuery.setText("Show debug messages for queries");
		debugQuery.setData(P_DEBUG_QUERY);
		debugQuery.addSelectionListener(new CheckboxListener());

		debugUI = new Button(grp, SWT.CHECK);
		debugUI.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		debugUI.setData(P_DEBUG_UI);
		debugUI.setText("Show debug messages for UI");
		debugUI.addSelectionListener(new CheckboxListener());

		logQuery = new Button(grp, SWT.CHECK);
		logQuery.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		logQuery.setData(P_LOG_QUERIES);
		logQuery.setText("Log all queries");
		logQuery.addSelectionListener(new CheckboxListener());

		updateUI();

		return entryTable;
	}

	private void updateUI() {
		selectBackendByName(backendExtensions);
		selectInstalledResources(resourceExtensions);

		debugQuery.setSelection(getPreferenceStore().getBoolean(P_DEBUG_QUERY));
		debugUI.setSelection(getPreferenceStore().getBoolean(P_DEBUG_UI));
		logQuery.setSelection(getPreferenceStore().getBoolean(P_LOG_QUERIES));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(JQueryBackendPlugin.getDefault().getPreferenceStore());
	}

	public static void forceDefaults(IPreferenceStore store) {
		store.setValue(P_JQUERY_BACKEND_FACTBASE, store.getDefaultString(P_JQUERY_BACKEND_FACTBASE));
		store.setValue(P_INSTALLED_RESOURCES, store.getDefaultString(P_INSTALLED_RESOURCES));
		store.setValue(P_DEBUG_QUERY, store.getDefaultBoolean(P_DEBUG_QUERY));
		store.setValue(P_DEBUG_UI, store.getDefaultBoolean(P_DEBUG_UI));
		store.setValue(P_LOG_QUERIES, store.getDefaultBoolean(P_LOG_QUERIES));
	}

	protected void performDefaults() {
		forceDefaults(getPreferenceStore());
		updateUI();

		super.performDefaults();
	}

	public static void activateJQueryResources() {
		String resources = JQueryBackendPlugin.getDefault().getPreferenceStore().getString(P_INSTALLED_RESOURCES);
		String[] res = resources.split(";");

		List x = JQueryBackendPlugin.getListOfResourceExtensions();
		for (int i = 0; i < res.length; i++) {
			x.remove(res[i]);
		}

		for (Iterator it = x.iterator(); it.hasNext();) {
			JQueryBackendPlugin.activateResource(it.next().toString());
		}
	}

	private void selectBackendByName(org.eclipse.swt.widgets.List l) {
		String target = getPreferenceStore().getString(P_JQUERY_BACKEND_FACTBASE);
		String[] list = l.getItems();
		for (int i = 0; i < list.length; i++) {
			if (target.equals(list[i])) {
				l.select(i);
				return;
			}
		}
		l.select(0);
	}

	private void selectInstalledResources(CheckboxTableViewer t) {
		String target = getPreferenceStore().getString(P_INSTALLED_RESOURCES);
		String[] res = target.split(";");

		Table l = t.getTable();
		int itemCount = l.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			for (int j = 0; j < res.length; j++) {
				TableItem item = l.getItem(i);

				// uncheck items which are in the list
				if (res[j].equals(item.getText())) {
					item.setChecked(false);
				} else {
					item.setChecked(true);
				}
			}
		}
	}

	private String[] getUnSelectedResources() {
		java.util.List x = new ArrayList();
		Table l = resourceExtensions.getTable();
		int itemCount = l.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			TableItem item = l.getItem(i);
			// uncheck items are in the list
			if (!item.getChecked()) {
				x.add(item.getText());
			}
		}

		return (String[]) x.toArray(new String[x.size()]);
	}

	protected void performApply() {
		JQueryBackendPlugin.getDefault().updatePreferences(false);
		updateFactbaseResources();
	}

	private void updateFactbaseResources() {
		if (updateResources) {
			updateResources = false;
			for (Iterator it = JQueryBackendPlugin.getListOfResourceExtensions().iterator(); it.hasNext();) {
				String s = (String) it.next();

				if (resourceExtensions.getChecked(s)) {
					JQueryBackendPlugin.activateResource(s);
				} else {
					JQueryBackendPlugin.disableResource(s);
				}
			}

			JQueryAPI.getFactBase().reloadFacts();

			// FIXME Should I reload facts here?
			//		JQueryAPI.getFactBase().reloadFacts();
		}
	}

	public boolean performOk() {
		JQueryBackendPlugin.getDefault().updatePreferences(false);
		updateFactbaseResources();
		return super.performOk();
	}

	private class CheckboxListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			String p = (String) e.widget.getData();
			getPreferenceStore().setValue(p, ((Button) e.widget).getSelection());
		}
	}
}
