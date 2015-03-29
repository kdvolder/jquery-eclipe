package ca.ubc.jquery.scapes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;

import ca.ubc.jquery.JQueryBackendPlugin;

public class SaveJQueryScapeHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
//		String saveFile = null;
//
//		FileDialog d = new FileDialog(JQueryBackendPlugin.getShell(), SWT.SAVE);
//		d.setFilterNames(new String[] { "JQueryScapes Files" });
//		d.setFilterExtensions(new String[] { "*.jqs" });
//		d.setOverwrite(true);
//
//		saveFile = d.open();
//
//		if (saveFile != null) {
//			// save perspective to JQueryScape file
//			PerspectiveDescriptor current = (PerspectiveDescriptor) JQueryBackendPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
//			JQueryBackendPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().savePerspectiveAs(current);
//			WorkbenchPage page = (WorkbenchPage) JQueryBackendPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
//
//			// initialization stuff
//			XMLMemento xmlFile = XMLMemento.createWriteRoot("JQueryScape");
//			xmlFile.putString("label", current.getLabel());
//
//			// save the view factory
//			IMemento xmlViews = xmlFile.createChild(IWorkbenchConstants.TAG_VIEWS);
//			page.getViewFactory().saveState(xmlViews);
//
//			// save the perspective layout
//			Perspective persp = page.getPerspective(page.getCurrentPerspective());
//			IMemento xmlPersp = xmlFile.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
//			persp.saveState(xmlPersp);
//
//			// save the XML document
//			try {
//				// ensure save file has proper extension
//				if (!saveFile.endsWith(".jqs")) {
//					saveFile = saveFile + ".jqs";
//				}
//				Writer writer = new FileWriter(saveFile);
//
//				xmlFile.save(writer);
//				writer.close();
//			} catch (IOException e) {
//				JQueryBackendPlugin.error("Saving perspective ", e);
//			}
//		}

		return null;
	}
}
