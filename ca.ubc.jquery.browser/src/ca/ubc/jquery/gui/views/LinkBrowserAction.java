package ca.ubc.jquery.gui.views;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.PopupDialog;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryEvent;
import ca.ubc.jquery.api.JQueryEventListener;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.QueryNodeUpdateJob;
import ca.ubc.jquery.gui.dialogs.PopupLinkViewDialog;
import ca.ubc.jquery.gui.results.QueryNode;

/**
 * This class links a browser to a JQueryUpdateTarget (or multiple ones).  It makes a 
 * dialog visible an manages it's own checked state.  It also sets the input and 
 * selection filters for the updateJob and schedules that same job.
 * 
 * This class also serializes itself in order for JQuery to save/restore linked states.
 * 
 * @author lmarkle
 */
public class LinkBrowserAction extends Action implements JQueryEventListener {
	private static int UPDATE_DELAY = 500;

	private JQueryTreeView view;

	private JQuery query;

	private List linkParts;

	private QueryNodeUpdateJob updateBrowser;

	public LinkBrowserAction(String name, JQueryTreeView view, QueryNodeUpdateJob updater) {
		super(name, IAction.AS_CHECK_BOX);
		updateBrowser = updater;

		this.view = view;
		linkParts = new ArrayList();
	}

	public void link() {
		updateBrowser.setQuery((QueryNode) view.getTreeRoot());
		JQueryAPI.addListener(this);
	}

	protected void link(JQueryUpdateTarget[] parts) {
		setLinkedParts(parts);
		if (parts.length > 0) {
			setChecked(true);
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (checked) {
			link();
		} else {
			unlink();
		}
	}

	public void unlink() {
		super.setChecked(false);
		updateBrowser.cancel();
		JQueryAPI.removeListener(this);

		// a little cleanup... (ONLY FOR QRA)
		//			qra.clear();
	}

	public void run() {
		// figure out which browser to link too...
		super.setChecked(!linkParts.isEmpty());

		PopupDialog d = new PopupLinkViewDialog(JQueryTreeBrowserPlugin.getShell(), view, linkParts, updateBrowser);
		//		Dialog d = new LinkBrowserDialog(JQueryTreeBrowserPlugin.getShell(), view, linkParts, updateBrowser);
		d.open();
	}

	private void setLinkedParts(JQueryUpdateTarget[] vr) {
		linkParts.clear();
		for (int i = 0; i < vr.length; i++) {
			linkParts.add(vr[i]);
		}
	}

	protected List getLinkParts() {
		return linkParts;
	}

	protected void saveState(ObjectOutputStream oos) throws IOException {
		oos.writeBoolean(isChecked());
		oos.writeUTF(updateBrowser.getInputFilter());
		oos.writeUTF(updateBrowser.getSelectionFilter());
		if (isChecked()) {
			oos.writeObject(query);

			oos.writeInt(linkParts.size());
			for (Iterator it = linkParts.iterator(); it.hasNext();) {
				String t = ((JQueryUpdateTarget) it.next()).getName();
				oos.writeUTF(t);
			}
		}
	}

	protected void restoreState(ObjectInputStream ois) throws IOException, Exception {
		setChecked(ois.readBoolean());
		updateBrowser.setInputFilter(ois.readUTF());
		updateBrowser.setSelectionFilter(ois.readUTF());
		if (isChecked()) {
			query = (JQuery) ois.readObject();

			int count = ois.readInt();
			linkParts.clear();
			for (int i = 0; i < count; i++) {
				String t = ois.readUTF();
				linkParts.add(JQueryAPI.getUpdateTarget(t));
			}

			// reactivate the listener
			link();
		}
	}

	public void handleEvent(JQueryEvent e) {
		if (e.getType().equals(JQueryEvent.EventType.TargetUpdate)) {
			// only receive inputs from targets we've been told to link to
			if (!linkParts.contains(e.getSource())) {
				return;
			}

			// ignore empty selections
			if (e.getData() == null) {
				return;
			}

			view.setBrowserTarget(e.getData(), UPDATE_DELAY);
		}

		if (e.getType().equals(JQueryEvent.EventType.RemoveUpdateTarget)) {
			linkParts.remove(e.getSource());

			if (linkParts.isEmpty()) {
				unlink();
				setChecked(false);
			}
		}
	}
}
