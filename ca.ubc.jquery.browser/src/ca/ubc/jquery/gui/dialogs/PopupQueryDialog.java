/*
 * Created on Nov 28, 2007
 */
package ca.ubc.jquery.gui.dialogs;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;

/**
 * @author wannop
 */
public class PopupQueryDialog extends PopupDialog {

	private QueryEditDialogController queryDialog;

	private QueryNode query;

	//
	// SWT Components
	//
	private Control titleText;

	private JQueryTreeViewer viewer;

	private Text errorText;

	// ---- Constructor
	// ----------------------------------------------------------------------------------------------/
	//	/**
	//	 * creates a QueryDialog with the given shell and style, and a new, initially empty Query operating on the given target node
	//	 */
	//	public PopupQueryDialog(Shell shell, JQueryTreeView view, Object target) {
	//		super(shell, INFOPOPUPRESIZE_SHELLSTYLE, true, false, true, false, "edit query", "");
	//		query = new QueryResults(target);
	//		queryNode = null;
	//		this.view = view;
	//
	//		filtersList = null;
	//		errorText = null;
	//		errorMessage = null;
	//		errorDisplayUpdater = null;
	//	}

	/*******************************************************************************************************************************************************************************************************************************************
	 * creates a QueryDialog with the given shell and title to operate on the given Query.
	 ******************************************************************************************************************************************************************************************************************************************/
	public PopupQueryDialog(Shell shell, JQueryTreeViewer view, QueryNode query) {
		super(shell, INFOPOPUPRESIZE_SHELLSTYLE, true, false, true, false, "edit query", "");
		queryDialog = new QueryEditDialogController(query.getLabel(), query.getQuery()) {
			protected void onClose() {
				PopupQueryDialog.this.close();
			}

			protected void onError(String error) {
				updateErrorDisplay(error);
			}
		};

		this.query = query;
		this.viewer = view;
	}

	protected Point getInitialLocation(Point initialSize) {
		return getShell().getDisplay().getCursorLocation();
	}

	protected Control createHorizontalRule(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		separator.setLayoutData(data);

		return separator;
	}

	@Override
	protected Control getFocusControl() {
		return queryDialog.getFocusControl();
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		errorIcon = new Label(parent, SWT.NONE);
		errorIcon.setImage(JQueryAPI.getElementImage("Error"));
		GridData d = GridDataFactory.fillDefaults().create();
		errorIcon.setLayoutData(d);

		Control result = super.createTitleControl(parent);
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridData data;
		GridLayout layout;

		Composite result = new Composite(parent, SWT.NONE);
		layout = GridLayoutFactory.fillDefaults().create();
		result.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		result.setLayoutData(data);

		Composite temp = new Composite(result, SWT.NONE);
		layout = GridLayoutFactory.fillDefaults().create();
		layout.numColumns = 2;
		temp.setLayout(layout);
		temp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		titleText = queryDialog.createTitleArea(temp, false);

		Button b = new Button(temp, SWT.PUSH);
		data = new GridData(SWT.NONE, SWT.NONE, false, false);
		data.horizontalAlignment = SWT.RIGHT;
		b.setLayoutData(data);
		b.setText("&Apply");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					queryDialog.applyChanges(query.getQuery());
					query.setLabel(queryDialog.getQueryLabel());

					if (isStructureModified()) {
						viewer.execute(query);
					} else if (isLabelModified()) {
						viewer.update(query, null);
					}
				} catch (Throwable ex) {
					// ignore error and save query info anyway
				}
			}
		});

		// Horizontal rule
		createHorizontalRule(result);

		queryDialog.createQueryArea(result, false);

		// Horizontal rule
		createHorizontalRule(result);

		//
		// Optional Filters Group
		// Only show this group if we have filters applied
		if (!query.getQuery().getFilters().isEmpty()) {
			queryDialog.createFiltersArea(result);
		}

		queryDialog.createEditVariablesArea(result);
		// Horizontal rule
		createHorizontalRule(result);

		queryDialog.createRecursionArea(result);
		queryDialog.finalizeDialogSetup();
		queryDialog.setAutoHelpDelay(2000);

		result.pack();

		return result;
	}

	@Override
	protected void setTabOrder(Composite composite) {
		titleText.getParent().setTabList(new Control[] { titleText });
		queryDialog.setTabOrder(composite);
	}

	@Override
	public boolean close() {
		queryDialog.close();
		return super.close();
	}

	private Label errorIcon;

	@Override
	protected Control createInfoTextArea(Composite parent) {
		Composite result = parent;
		errorText = new Text(result, SWT.MULTI | SWT.WRAP);
		GridData d = GridDataFactory.fillDefaults().create();
		d.grabExcessHorizontalSpace = true;
		d.horizontalAlignment = SWT.RIGHT;
		d.widthHint = 300;
		d.horizontalSpan = 2;
		errorText.setLayoutData(d);

		updateErrorDisplay(null);
		return result;
	}

	/**
	 * @return true iff the Query's label has been modified during the lifetime of this dialog.
	 */
	public boolean isLabelModified() {
		return queryDialog.isLabelModified();
	}

	/**
	 * @return true iff the Query's query text or variables have been modified during the lifetime of this dialog. Changes to the Query's label do not change this field, but can be detected using labelModified
	 */
	public boolean isStructureModified() {
		return queryDialog.isStructureModified();
	}

	private void updateErrorDisplay(String errorMessage) {
		if (errorText != null) {
			if (errorMessage == null) {
				errorText.setText("(no error)");
				errorText.setToolTipText("");
				errorText.setForeground(errorText.getDisplay().getSystemColor(SWT.COLOR_BLACK));

				errorIcon.setVisible(false);
				((GridData) errorIcon.getLayoutData()).exclude = true;
				errorIcon.getParent().layout(false);
			} else {
				errorText.setText(errorMessage);
				errorText.setToolTipText(errorMessage);
				errorText.setForeground(errorText.getDisplay().getSystemColor(SWT.COLOR_RED));

				errorIcon.setVisible(true);
				((GridData) errorIcon.getLayoutData()).exclude = false;
				errorIcon.getParent().layout(false);
				errorIcon.setToolTipText(errorMessage);
			}
		}
	}
}