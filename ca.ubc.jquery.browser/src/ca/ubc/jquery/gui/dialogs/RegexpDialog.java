package ca.ubc.jquery.gui.dialogs;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;

/**
 * TODO Remove this class
 */
public class RegexpDialog extends PopupDialog {
	private Text regExp;

	private QueryNode node;

	private QueryResultNode queryType;

	private JQueryTreeViewer view;

	public RegexpDialog(Shell parent, JQueryTreeViewer view, QueryResultNode node) {
		super(parent, SWT.FOCUSED | SWT.ON_TOP, true, false, false, false, "Regular Expression Filter", "enter regular expression");
		this.view = view;
		this.node = node.getQueryNode();
		this.queryType = node;
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		return getShell().getDisplay().getCursorLocation();
	}

	@Override
	protected Point getInitialSize() {
		return getShell().computeSize(200, SWT.DEFAULT, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		regExp = new Text(parent, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.widthHint = 100;
		regExp.setLayoutData(data);
		regExp.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.LF || e.character == SWT.CR) {
					String regExp = RegexpDialog.this.regExp.getText();
					String qs = "re_name(" + JQueryAPI.getThisVar() + ",/" + regExp + "/)";

					try {
						JQuery q = JQueryAPI.createQuery(qs);
						node.getQuery().addFilter("re_name(" + regExp + ")", q, queryType.getElementSource(), queryType.getListPosition());
						RegexpDialog.this.view.execute(node);
					} catch (JQueryException ex) {
						JQueryTreeBrowserPlugin.error("Applying filter: ", ex);
					}

					RegexpDialog.this.close();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		return regExp;
	}
}
