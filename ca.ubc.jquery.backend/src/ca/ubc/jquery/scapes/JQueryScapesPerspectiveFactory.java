package ca.ubc.jquery.scapes;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ca.ubc.jquery.JQueryBackendPlugin;

public class JQueryScapesPerspectiveFactory implements IPerspectiveFactory {
	public JQueryScapesPerspectiveFactory() {
		FileDialog d = new FileDialog(JQueryBackendPlugin.getShell());
	}

	public void createInitialLayout(IPageLayout layout) {
	}
}
