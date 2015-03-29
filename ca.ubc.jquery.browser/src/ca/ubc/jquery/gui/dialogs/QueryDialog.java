/*
 * Created on May 12, 2003
 */
package ca.ubc.jquery.gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;

/**
 * @author wannop
 */
public class QueryDialog extends Dialog {

	private static final int ErrorDisplayLength = 40;

	// ---- Non-SWT components
	// ------------------------------------------------------------------------------------/
	/** The Query object that this composite operates on */
	private JQuery query;

	// ---- SWT components
	// ----------------------------------------------------------------------------------------/
	/** The dialog's title */
	private String title;

	private Label dialogInfo;

	private Label dialogImage;

	private Label errorImage;

	private Label errorDisplay;

	private Font displayFont;

	private Image displayImage;

	private static QueryEditDialogController queryDialog;

	// ---- Constructor
	// ----------------------------------------------------------------------------------------------/
	/**
	 * creates a QueryDialog with the given shell and style, and a new, initially empty Query operating on the given target node
	 */
	public QueryDialog(Shell shell, Object target, String dialogTitle) {
		super(shell);

		try {
			query = JQueryAPI.createQuery("");
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error(e);
		}
		this.title = dialogTitle;

		queryDialog = new QueryEditDialogController("", query) {
			protected void onClose() {
				QueryDialog.this.close();
			}

			protected void onError(String error) {
				handleError(error);
			}
		};
	}

	//	/*******************************************************************************************************************************************************************************************************************************************
	//	 * creates a QueryDialog with the given shell and title to operate on the given Query.
	//	 ******************************************************************************************************************************************************************************************************************************************/
	//	public QueryDialog(Shell shell, IQueryResults query, String dialogTitle) {
	//		super(shell);
	//		this.query = query;
	//		// queryTextIsCurrent = false;
	//		this.title = dialogTitle;
	//	}

	//	@Override
	//	protected Point getInitialLocation(Point initialSize) {
	//		return getShell().getDisplay().getCursorLocation();
	//	}

	/**
	 * Sets the dialog's title.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("JQuery -- " + title);
	}

	public void okPressed() {
		try {
			queryDialog.applyChanges(query);
			this.setReturnCode(Window.OK);
			this.close();
			super.okPressed();
		} catch (Throwable e) {
			JQueryTreeBrowserPlugin.error("Exception while parsing query:", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		// TODO Auto-generated method stub
		super.cancelPressed();
	}

	public boolean close() {
		queryDialog.close();

		displayImage.dispose();
		displayFont.dispose();

		return super.close();
	}

	/**
	 * @return true iff the Query's label has been modified during the lifetime of this dialog.
	 */
	public boolean isLabelModified() {
		//		return labelModified;
		return queryDialog.isLabelModified();
	}

	/**
	 * @return true iff the Query's query text or variables have been modified during the lifetime of this dialog. Changes to the Query's label do not change this field, but can be detected using labelModified
	 */
	public boolean isStructureModified() {
		//		return structureModified;
		return queryDialog.isStructureModified();
	}

	public String getQueryLabel() {
		return queryDialog.getQueryLabel();
	}

	public JQuery getQuery() {
		return query;
	}

	protected Control createDialogArea(Composite parent) {
		Composite title = new Composite(parent, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().create();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		title.setLayout(layout);
		GridData data = GridDataFactory.fillDefaults().create();
		title.setLayoutData(data);
		title.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		dialogInfo = new Label(title, SWT.NONE);
		data = GridDataFactory.fillDefaults().create();
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalIndent = 10;
		data.verticalAlignment = SWT.CENTER;
		dialogInfo.setLayoutData(data);
		displayFont = new Font(dialogInfo.getFont().getDevice(), "", 10, SWT.BOLD);
		dialogInfo.setFont(displayFont);
		dialogInfo.setText("New JQuery");
		dialogInfo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		dialogImage = new Label(title, SWT.NONE);
		data = GridDataFactory.fillDefaults().create();
		data.verticalSpan = 2;
		dialogImage.setLayoutData(data);
		displayImage = JQueryTreeBrowserPlugin.getImageDescriptor("querydialog.bmp").createImage();
		dialogImage.setImage(displayImage);
		dialogImage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		errorImage = new Label(title, SWT.NONE);
		data = GridDataFactory.fillDefaults().create();
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = true;
		data.horizontalIndent = 10;
		data.verticalAlignment = SWT.CENTER;
		errorImage.setImage(JQueryAPI.getElementImage("Error"));
		errorImage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		errorImage.setVisible(false);

		errorDisplay = new Label(title, SWT.NONE);
		data = GridDataFactory.fillDefaults().create();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.CENTER;
		errorDisplay.setLayoutData(data);
		errorDisplay.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		errorDisplay.setText("Enter new query information");

		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		separator.setLayoutData(data);

		Composite result = (Composite) super.createDialogArea(parent);
		setBlockOnOpen(true);

		queryDialog.createTitleArea(result, true);
		queryDialog.createQueryArea(result, true);
		// Only show filters area if filters are applied
		if (!query.getFilters().isEmpty()) {
			queryDialog.createFiltersArea(result);
		}
		queryDialog.createEditVariablesArea(result);
		queryDialog.createRecursionArea(result);
		queryDialog.finalizeDialogSetup();

		separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		separator.setLayoutData(data);

		return result;
	}

	private void handleError(String error) {
		if (error == null) {
			errorDisplay.setText("Enter new query information");
			errorImage.setVisible(false);
		} else {
			if (error.length() > ErrorDisplayLength) {
				errorDisplay.setText(error.substring(0, ErrorDisplayLength) + "...");
			} else {
				errorDisplay.setText(error);
			}

			errorDisplay.setToolTipText(error);

			errorImage.setVisible(true);
			//			JQueryTreeBrowserPlugin.error("Error in query: " + error);
		}
	}
}