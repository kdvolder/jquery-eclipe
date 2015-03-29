package ca.ubc.jquery.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;


public class JQueryTreeViewKeyListener implements KeyListener {

	private JQueryTreeView view;

	public JQueryTreeViewKeyListener(JQueryTreeView view) {
		this.view = view;
	}

	public void keyPressed(KeyEvent e) {
		if (!ignoreKey(e.keyCode)) {
			view.activateFilter();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	protected boolean ignoreKey(int keyCode) {
		if (keyCode == SWT.ARROW_UP) {
			return true;
		}

		if (keyCode == SWT.ARROW_DOWN) {
			return true;
		}

		if (keyCode == SWT.ARROW_LEFT) {
			return true;
		}

		if (keyCode == SWT.ARROW_RIGHT) {
			return true;
		}

		if (keyCode == SWT.PAGE_DOWN || keyCode == SWT.PAGE_UP || keyCode == SWT.HOME || keyCode == SWT.END) {
			return true;
		}

		if (keyCode == SWT.TAB || keyCode == SWT.CR || keyCode == SWT.LF || keyCode == SWT.KEYPAD_CR) {
			return true;
		}

		return false;
	}
}
