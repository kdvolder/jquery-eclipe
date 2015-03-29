package ca.ubc.jquery.api.gluelog;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.ubc.jquery.api.JQueryAPI;

public class FactbaseSelectionDialog extends Dialog {

	String selected;

	public FactbaseSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Sets the dialog's title.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select fact base");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		// set window blocking mode
		setBlockOnOpen(true);

		// initialize layout of this composite
		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);
		
		
		// FIXME testing
		Label tempLabel = new Label(composite, SWT.CENTER);
		tempLabel.setText("This is the dialog area");

//		PrologInterfaceRegistry reg = PrologRuntimePlugin.getDefault().getPrologInterfaceRegistry();
//		Set keys = reg.getAllKeys();
//		for (Iterator it = keys.iterator(); it.hasNext();) {
//			String key = (String) it.next();
//			Button temp = new Button(composite, SWT.RADIO);
//
//			temp.setText(key);
//			temp.setData(key);
//
//			// get default selected item
//			if (JQueryAPI.getFactBase().getName() == key) {
//				temp.setSelection(true);
//				selected = key;
//			}
//
//			temp.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent event) {
//					selected = (String) event.widget.getData();
//				}
//			});
//		}

		return composite;
	}

	protected String getSelected() {
		return selected;
	}
}
