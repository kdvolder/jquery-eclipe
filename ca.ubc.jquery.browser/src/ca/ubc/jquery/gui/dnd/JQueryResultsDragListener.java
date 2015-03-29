package ca.ubc.jquery.gui.dnd;

import java.io.IOException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * Supports dragging gadgets from a structured viewer.
 */
public class JQueryResultsDragListener extends DragSourceAdapter {
	private StructuredViewer viewer;

	private IStructuredSelection selection;

	public JQueryResultsDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit)
			return;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragSetData(DragSourceEvent event) {
		if (JQueryResultsTreeTransfer.getInstance().isSupportedType(event.dataType)) {
			ResultsTreeNode[] gadgets = (ResultsTreeNode[]) selection.toList().toArray(new ResultsTreeNode[selection.size()]);
			selection = null;
			event.data = gadgets;
		} else if (JQueryFilterTransfer.getInstance().isSupportedType(event.dataType)) {
			Object[] gadgets = (Object[]) selection.toList().toArray(new Object[selection.size()]);
			event.data = gadgets;
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			ResultsTreeNode[] gadgets = (ResultsTreeNode[]) selection.toList().toArray(new ResultsTreeNode[selection.size()]);
			try {
				byte[] data = JQueryResultsTreeTransfer.getInstance().toByteArray(gadgets);
				event.data = new PluginTransferData("ca.ubc.jquery.browser.dropaction", data);
			} catch (IOException e) {
				JQueryTreeBrowserPlugin.error("Setting drag data: ", e);
			}
		}
	}

	protected void onDragStart(DragSourceEvent event, IStructuredSelection selection) {
		// default does nothing...
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragStart(DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();

		// Work around for bug with selections while dragging in OS X:
		// capture selection now before it gets cleared.
		if (event.doit) {
			selection = (IStructuredSelection) viewer.getSelection();

			// any special handling stuff that may be required
			onDragStart(event, selection);
		}
	}
}