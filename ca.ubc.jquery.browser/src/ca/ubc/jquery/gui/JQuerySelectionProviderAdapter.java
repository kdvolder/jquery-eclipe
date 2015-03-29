package ca.ubc.jquery.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * This class adapts the view selection so that other plugins can understand it.  Most
 * plugins won't understand a JQuery factbase selection so instead we broadcast the 
 * selected IJavaElements.  This adapter does the conversion.
 * 
 * @author lmarkle
 */
public class JQuerySelectionProviderAdapter implements ISelectionProvider, ISelectionChangedListener {

	private JQueryTreeViewer viewer;

	private List selectionListeners;

	public JQuerySelectionProviderAdapter(JQueryTreeViewer viewer) {
		this.viewer = viewer;
		selectionListeners = new ArrayList();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// We can't simply add the selection listeners to the underlying viewer because
		// it will only broadcast JQuery item selections.  Instead we add this listener to
		// do the conversions
		//
		// We again don't add duplicate listeners
		//
		// TODO Is this thread safe?  It seems eclipse does this with an array...
		if (!selectionListeners.contains(listener)) {
			selectionListeners.add(listener);
		}

		viewer.addSelectionChangedListener(this);
	}

	public ISelection getSelection() {
		return viewer.getJMSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Is this thread safe?
		selectionListeners.remove(listener);

		// remove this listener if there are no children listeners
		if (selectionListeners.isEmpty()) {
			viewer.removeSelectionChangedListener(this);
		}
	}

	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection s = viewer.getJMSelection();
		SelectionChangedEvent ev = new SelectionChangedEvent(this, s);

		// TODO Is this thread safe?
		for (Iterator it = selectionListeners.iterator(); it.hasNext();) {
			ISelectionChangedListener t = (ISelectionChangedListener) it.next();
			t.selectionChanged(ev);
		}
	}
}
