package ca.ubc.jquery.gui.dnd;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
//import org.eclipse.ui.internal.PartStack;

/**
 * Modified from something...
 * 
 * A Huge thanks goes out to Sam Davis for figuring out how simple this code actually is.
 *  
 * @author lmarkle
 * @since 3.0
 */
public class DragOperations {

	/**
	 * Drags the given view OR editor to the given location (i.e. it only cares that we're given
	 * a 'Part' and doesn't care whether it's a 'View' or an 'Editor'.
	 * <p>
	 * This method should eventually replace the original one once the Workbench has been updated
	 * to handle Views and Editors without distincton. 
	 * 
	 * @param editor
	 * @param target
	 * @param wholeFolder
	 */
	public static void drag(IWorkbenchPart origin, IWorkbenchPart part, boolean wholeFolder) {
// TODO: no longer compiles in E4
//		PartSite site = (PartSite) part.getSite();
//		PartPane pane = site.getPane();
//
//		// add part temporarily to the stack that created it
//		PartStack container = ((PartStack) (pane.getContainer()));
//		container.remove(pane);
//		if (container.getChildren().length == 0) {
//			container.getContainer().remove(container);
//		}
//		((PartSite) origin.getSite()).getPane().getContainer().add(pane);
//
//		// start drag
//		PartStack parent = ((PartStack) (pane.getContainer()));
//		parent.paneDragStart(wholeFolder ? null : pane, Display.getDefault().getCursorLocation(), false);
	}
}