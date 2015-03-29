package ca.ubc.jquery.gui.views;

import org.eclipse.core.runtime.IConfigurationElement;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;

/**
 * Currently Eclipse (3.3) does not support extending an extension point so we cannot 
 * simply create a view with different parameters or parameters we add.  Instead we have
 * to kind of hack them (by editing the XML definition file) and by interpreting them
 * here.  By default most Views won't use these parameters but this allows a more advanced
 * user to create their own views by editing the XML code and opening them through the
 * Show View Eclipse window.
 *  
 * @author lmarkle
 */
public class JQueryTreeViewWithXML extends JQueryTreeView {
	@Override
	protected boolean checkXMLConfiguration() {
		IConfigurationElement conf = getConfigurationElement();

		String name = conf.getAttribute("name");

		try {
			String query = conf.getAttribute("query");
			if (query != null) {
				QueryNode root = new QueryNode(query, name);
				setTreeRoot(root);

				setPartName(name);

				String selectedVars = conf.getAttribute("selectedVars");
				String link = conf.getAttribute("link");
				String inputFilter = conf.getAttribute("linkInputFilter");
				String selectFilter = conf.getAttribute("linkSelectionFilter");
				String autoExpand = conf.getAttribute("autoExpand");
				String recursiveVar = conf.getAttribute("recursiveVar");
				String autoRefresh = conf.getAttribute("autoRefresh");

				if (autoRefresh != null && ("true").equals(autoRefresh)) {
					autoRefreshAction.setChecked(true);
				}

				if (selectedVars != null) {
					String[] vars = selectedVars.split(",");
					try {
						root.getQuery().setChosenVars(vars);
					} catch (Exception e) {
						JQueryTreeBrowserPlugin.error("Initializing query variable selection: ", e);
					}
				}

				if (inputFilter != null) {
					String o = JQueryAPI.getStringProperty(inputFilter, "updateTargetFilter");

					if (("null").equals(o)) {
						getBrowserUpdater().setInputFilter(inputFilter);
					} else {
						getBrowserUpdater().setInputFilter(o);
					}
				}

				if (selectFilter != null) {
					String o = JQueryAPI.getStringProperty(selectFilter, "updateTargetFilter");

					if (("null").equals(o)) {
						getBrowserUpdater().setSelectionFilter(selectFilter);
					} else {
						getBrowserUpdater().setSelectionFilter(o);
					}
				}

				if (autoExpand != null) {
					root.setAutoExpansionDepth(Integer.parseInt(autoExpand));
				}

				if (recursiveVar != null) {
					root.getQuery().setRecursiveVar(recursiveVar);
				}

				if (link != null) {
					Object targ = null;
					String[] tgs = link.split(";");
					JQueryUpdateTarget[] parts = new JQueryUpdateTarget[tgs.length];

					for (int i = 0; i < tgs.length; i++) {
						parts[i] = JQueryAPI.getUpdateTarget(tgs[i]);
						// take one target that's not null
						targ = (targ == null) ? parts[i].getTarget() : targ;
					}

					getLinker().link(parts);

					if (targ == null) {
						setBrowserTarget("(null)");
						root.getQuery().bind(JQueryAPI.getThisVar(), "(null)");
					} else {
						setBrowserTarget(targ);
						root.getQuery().bind(JQueryAPI.getThisVar(), targ);
					}
				}

				((JQueryTreeViewer) getViewer()).execute(root, 250);
				return true;
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Showing view " + name + ": ", e);
		}
		return false;
	}
}
