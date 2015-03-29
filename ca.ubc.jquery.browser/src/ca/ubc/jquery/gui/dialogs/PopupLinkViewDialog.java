package ca.ubc.jquery.gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.ubc.jquery.gui.QueryNodeUpdateJob;
import ca.ubc.jquery.gui.views.JQueryTreeView;

public class PopupLinkViewDialog extends PopupDialog {

	private LinkBrowserDialogController controller;

	// ---- Constructor
	// ----------------------------------------------------------------------------------------------/
	/**
	 * @param selected Is the selected parts in the dialog
	 */
	public PopupLinkViewDialog(Shell shell, JQueryTreeView view, List selected, QueryNodeUpdateJob browserUpdater) {
		super(shell, INFOPOPUP_SHELLSTYLE, true, true, true, true, "Link browser", "JQuery Select Browser Link");

		controller = new LinkBrowserDialogController(view, selected, browserUpdater);
	}

	@Override
	public boolean close() {
		// apply changes
		controller.applyChanges();

		return super.close();
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		return getShell().getDisplay().getCursorLocation();
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		Control p = super.createTitleControl(parent);
		parent = p.getParent();
		return p;
	}

	@Override
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
}
