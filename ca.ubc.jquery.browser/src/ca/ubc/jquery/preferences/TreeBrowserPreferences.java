package ca.ubc.jquery.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;

public class TreeBrowserPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String P_MAX_RESULTS = "maxResults";

	public static final String P_EDITOR_AUTOCOMPLETION = "editorAutoComplete";

	private Text maxResultsText;

	private Button editorAutoCompletion;

	public TreeBrowserPreferences() {
	}

	public TreeBrowserPreferences(String title) {
		super(title);
	}

	public TreeBrowserPreferences(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		Group grp = new Group(entryTable, SWT.SHADOW_NONE);
		grp.setText("Tree Browser Settings");
		layout = new GridLayout();
		layout.numColumns = 2;
		grp.setLayout(layout);
		grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(grp, SWT.HORIZONTAL);
		label.setText("Maximum number of results displayed per query");
		data = new GridData();
		label.setLayoutData(data);

		maxResultsText = new Text(grp, SWT.BORDER | SWT.SINGLE);
		maxResultsText.setData(P_MAX_RESULTS);
		maxResultsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		maxResultsText.addModifyListener(new IntegerTextFieldListener(this));

		editorAutoCompletion = new Button(grp, SWT.CHECK);
		editorAutoCompletion.setData(P_EDITOR_AUTOCOMPLETION);
		editorAutoCompletion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPreferenceStore().setValue(P_EDITOR_AUTOCOMPLETION, editorAutoCompletion.getSelection());
			}
		});
		editorAutoCompletion.setText("Enable auto completion in the query editor dialog");

		updateUI();

		return entryTable;
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(JQueryTreeBrowserPlugin.getDefault().getPreferenceStore());
	}

	public static void forceDefaults(IPreferenceStore store) {
		store.setValue(P_MAX_RESULTS, store.getDefaultInt(P_MAX_RESULTS));
	}

	protected void updateUI() {
		maxResultsText.setText("" + getPreferenceStore().getInt(P_MAX_RESULTS));
		editorAutoCompletion.setSelection(getPreferenceStore().getBoolean(P_EDITOR_AUTOCOMPLETION));
	}

	@Override
	protected void performApply() {
		JQueryTreeBrowserPlugin.getDefault().updatePreferences(false);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		forceDefaults(getPreferenceStore());
		updateUI();

		super.performDefaults();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		boolean res = super.performOk();
		JQueryTreeBrowserPlugin.getDefault().updatePreferences(false);
		return res;
	}

	private class IntegerTextFieldListener implements ModifyListener {
		private PreferencePage prefpage;

		public IntegerTextFieldListener(PreferencePage page) {
			prefpage = page;
		}

		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.widget;

			try {
				Integer val = Integer.decode(text.getText());
				getPreferenceStore().setValue((String) text.getData(), val);

				prefpage.setErrorMessage(null);
				prefpage.setValid(true);
			} catch (NumberFormatException ex) {
				prefpage.setErrorMessage(text.getText() + " is not a number");
				prefpage.setValid(false);
			}
		}
	}
}
