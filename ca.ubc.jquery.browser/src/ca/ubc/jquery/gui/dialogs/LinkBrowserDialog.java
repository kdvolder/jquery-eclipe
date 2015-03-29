/*
 * Created on May 12, 2003
 */
package ca.ubc.jquery.gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.gui.QueryNodeUpdateJob;
import ca.ubc.jquery.gui.views.JQueryTreeView;

/**
 * @author wannop
 */
public class LinkBrowserDialog extends Dialog {

	private LinkBrowserDialogController controller;

	// ---- Constructor
	// ----------------------------------------------------------------------------------------------/
	/**
	 * @param selected Is the selected parts in the dialog
	 */
	public LinkBrowserDialog(Shell shell, JQueryTreeView view, List selected, QueryNodeUpdateJob browserUpdater) {
		super(shell);

		controller = new LinkBrowserDialogController(view, selected, browserUpdater);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("JQuery Select Browser Link");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = parent;
		GridData data;

		// set window blocking mode
		setBlockOnOpen(true);

		// initialize layout of this composite
		GridLayout layout = GridLayoutFactory.fillDefaults().create();
		composite.setLayout(layout);

		composite.setSize(400, 300);

		controller.createBrowserListTable(composite);

		// horizontal rule
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		controller.createLinkFilter(composite);
		controller.createSelectionFilter(composite);
		controller.finalizeSetup();

		return composite;
	}

	public void okPressed() {
		try {
			this.setReturnCode(Window.OK);
			this.close();
			controller.applyChanges();
			super.okPressed();
		} catch (Exception e) {
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
}