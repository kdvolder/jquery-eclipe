package ca.ubc.jquery.preferences;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryAPI;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 */
public class JQueryTyrubaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	//	public static final String P_LOG_USER = "logUser";
	public static final String P_CACHE_SIZE = "cacheSize";

	public static final String P_CLASSFILES_DETAILED = "detailedClassFiles";

	public static final String P_PARSE_DEPENDENCIES = "parseDependencies";

	public static final String P_RULES_FILES = "rulesFiles";

	// Widgets
	private List rulesList;

	private boolean rewriteUserRulesFile;

	private Button parseDependencies;

	private Text cacheSizeText;

	//	private Text userRubFile;

	public JQueryTyrubaPreferencePage() {
		setDescription("Preferences for the JQuery Tyruba Backend Plugin");
	}

	protected Control createContents(Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Group grp = new Group(entryTable, SWT.SHADOW_NONE);
		grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grp.setText("Tyruba Settings");

		layout = new GridLayout();
		layout.numColumns = 3;
		grp.setLayout(layout);

		//
		// Tyruba Factbase Options
		//
		parseDependencies = new Button(grp, SWT.CHECK);
		parseDependencies.setText("Parse dependencies");
		parseDependencies.setData(P_PARSE_DEPENDENCIES);
		parseDependencies.addSelectionListener(new CheckboxListener());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 3;
		parseDependencies.setLayoutData(data);

		Label label = new Label(grp, SWT.HORIZONTAL);
		label.setText("Factbase cache size");

		cacheSizeText = new Text(grp, SWT.BORDER | SWT.SINGLE);
		cacheSizeText.setData(P_CACHE_SIZE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		cacheSizeText.setLayoutData(data);
		cacheSizeText.addModifyListener(new IntegerTextFieldListener(this));

		//
		// Rules Files Options
		//
		rewriteUserRulesFile = false;

		grp = new Group(entryTable, SWT.SHADOW_NONE);
		grp.setText("Custom Rules Files");
		grp.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		grp.setLayout(gridLayout);

		// create the list box
		rulesList = new List(grp, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalSpan = 4;
		rulesList.setLayoutData(data);
		restoreSavedRulesFiles();

		// Add button
		Button addButton = new Button(grp, SWT.PUSH);
		addButton.setText("Add");
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(JQueryBackendPlugin.getShell());
				fileDialog.setFilterExtensions(new String[] { "*.rub" });

				String fileName = fileDialog.open();
				if (fileName != null) {
					//System.out.println("file string from FileDialog: "+fileName);
					int idx = rulesList.getSelectionIndex();
					if (idx < 0) {
						rulesList.add(fileName);
					} else {
						rulesList.add(fileName, idx);
					}
					saveRulesFiles();
				}
			}
		});

		// remove button
		Button removeButton = new Button(grp, SWT.PUSH);
		removeButton.setText("Remove");
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		removeButton.setLayoutData(data);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = rulesList.getSelectionIndex();
				if (idx >= 0) {
					rulesList.remove(idx);
				}
				saveRulesFiles();
			}
		});

		// move up button
		Button moveUpButton = new Button(grp, SWT.PUSH);
		moveUpButton.setText("Move Up");
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		moveUpButton.setLayoutData(data);
		moveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = rulesList.getSelectionIndex();
				if (idx > 0) {
					String sel[] = rulesList.getSelection();
					rulesList.remove(idx);
					rulesList.add(sel[0], idx - 1);
					rulesList.select(idx - 1);
				}
				saveRulesFiles();
			}
		});

		// move down button
		Button moveDownButton = new Button(grp, SWT.PUSH);
		moveDownButton.setText("Move Down");
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		moveDownButton.setLayoutData(data);
		moveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = rulesList.getSelectionIndex();
				if (idx >= 0 && idx < rulesList.getItemCount() - 1) {
					String sel[] = rulesList.getSelection();
					rulesList.remove(idx);
					rulesList.add(sel[0], idx + 1);
					rulesList.select(idx + 1);
				}
				saveRulesFiles();
			}
		});

		updateUI();

		return entryTable;
	}

	private void updateUI() {
		parseDependencies.setSelection(getPreferenceStore().getBoolean(P_PARSE_DEPENDENCIES));
		cacheSizeText.setText("" + getPreferenceStore().getInt(P_CACHE_SIZE));

		restoreSavedRulesFiles();
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(JQueryBackendPlugin.getDefault().getPreferenceStore());
	}

	public static void forceDefaults(IPreferenceStore store) {
		store.setValue(P_PARSE_DEPENDENCIES, store.getDefaultBoolean(P_PARSE_DEPENDENCIES));
		store.setValue(P_CACHE_SIZE, store.getDefaultInt(P_CACHE_SIZE));

		// store rules files as a '*' seperated list, example: rulefile1*rulefile2*rulefile3*etc...
		store.setValue(P_RULES_FILES, store.getDefaultString(P_RULES_FILES));
		JQueryBackendPlugin.getDefault().updatePreferences(false);
	}

	@Override
	protected void performApply() {
		JQueryBackendPlugin.getDefault().updatePreferences(false);

		createGlobalIncludeFile();
		if (rewriteUserRulesFile) {
			JQueryAPI.getFactBase().reloadRules();
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		forceDefaults(getPreferenceStore());
		rewriteUserRulesFile = true;
		updateUI();

		super.performDefaults();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		boolean res = super.performOk();
		JQueryBackendPlugin.getDefault().updatePreferences(false);

		createGlobalIncludeFile();
		if (rewriteUserRulesFile) {
			JQueryAPI.getFactBase().reloadRules();
		}

		return res;
	}

	private static String[] getAdditionalRulesFiles() {
		String fileList = JQueryBackendPlugin.getDefault().getPreferenceStore().getString(P_RULES_FILES);
		String[] result = fileList.split("\\*");

		// don't return a list with a "" item...
		if (("").equals(result[0])) {
			result = new String[0];
		}

		return result;
	}

	/**
	* Method createGUIF. creates the global UI file
	* 
	* Note it would be nice not to have this user.rub file floating around.  The thing
	* is we can't just load(file) because it only processes it seems to only process
	* files the way we want if they're "#include"d.  So this method writes a file called
	* "user.rub" with all the files from this dialog in it.
	*/
	public static void createGlobalIncludeFile() {
		File guif = new File(JQueryBackendPlugin.getGlobalUserIncludeFile());

		try {
			PrintWriter writer = new PrintWriter(new FileWriter(guif));
			for (Object filename : JQueryBackendPlugin.getRequiredRuleExtensions()) {
				writer.println(makeIncludeLine((String)filename));
			}
			for (String defRuleFile : JQueryTyrubaPreferencePage.getAdditionalRulesFiles()) {
				writer.println(makeIncludeLine(defRuleFile));
			}
			
			writer.println();
			writer.close();
		} catch (Exception e) {
			JQueryBackendPlugin.traceUI("Error creating default user.rub file: " + e.getMessage());
		}
	}
	
	private static String makeIncludeLine (String fileName) {
		// TODO: Why are we creating a File then converting it back to a String?
		File tempFile = new File(fileName);
		// this is a disgusting substitution...
		String fname = tempFile.toString().replaceAll("\\x5C", "\\\\\\\\");
		return "#include \"file:///" + fname + "\"";
	}

	private void restoreSavedRulesFiles() {
		String[] files = JQueryTyrubaPreferencePage.getAdditionalRulesFiles();
		rulesList.removeAll();
		for (int i = 0; i < files.length; i++) {
			rulesList.add(files[i]);
		}
	}

	private void saveRulesFiles() {
		String[] item = rulesList.getItems();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < item.length; i++) {
			result.append(item[i]);
			if (i + 1 < item.length) {
				result.append("*");
			}
		}
		getPreferenceStore().setValue(P_RULES_FILES, result.toString());
		rewriteUserRulesFile = true;
	}

	private class CheckboxListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			String p = (String) e.widget.getData();
			getPreferenceStore().setValue(p, ((Button) e.widget).getSelection());
		}
	}

	private class IntegerTextFieldListener implements ModifyListener {
		private PreferencePage p;

		public IntegerTextFieldListener(PreferencePage p) {
			this.p = p;
		}

		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.widget;

			try {
				Integer val = Integer.decode(text.getText());
				getPreferenceStore().setValue((String) text.getData(), val);

				p.setErrorMessage(null);
				p.setValid(true);
			} catch (NumberFormatException ex) {
				p.setErrorMessage(text.getText() + " is not a number");
				p.setValid(false);
			}
		}
	}
}
