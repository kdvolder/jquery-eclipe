package ca.ubc.jquery.gui.views;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;

public class JQueryTreeViewMouseListener implements MouseListener {

	private JQueryTreeViewer view;

	public JQueryTreeViewMouseListener(JQueryTreeViewer view) {
		this.view = view;
	}

	public void mouseDoubleClick(MouseEvent e) {
		if (!view.isRootTree()) {
			view.doEditQuery((QueryNode) view.getTreeRoot());
		}
	}

	public void mouseDown(MouseEvent e) {
	}

	public void mouseUp(MouseEvent e) {
	}
}
