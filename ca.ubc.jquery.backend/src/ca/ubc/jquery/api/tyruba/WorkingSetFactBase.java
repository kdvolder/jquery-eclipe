package ca.ubc.jquery.api.tyruba;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.AggregateWorkingSet;

import serp.util.IdentityMap;
import tyRuBa.engine.RuleBase;
import tyRuBa.jobs.ProgressMonitor;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactBase;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryResourceStrategy;

public class WorkingSetFactBase implements JQueryFactBase {

	private static final String WORKING_SET_TYPE = "org.eclipse.jdt.ui.JavaWorkingSetPage";

	private IWorkingSet workingSet;

	private IWorkingSetManager workingSetManager;

	/**
	 * listener for changes to working sets. Change notifications will not include those for resource changes. These must be handled separately by the workspaceListener
	 */
	private IPropertyChangeListener workingSetListener;

	/** listener for changes to .java files in the workspace */
	private IResourceChangeListener resourceListener;

	private IResourceChangeListener buildListener;

	private Set resourceStrategies;

	private Map ruleBaseManagers = new IdentityMap();

	// for creating/storing directory paths for tyruba rulebases
	private static Map<IWorkingSet, Integer> wsIDMap = new HashMap<>();

	private static int wsIDCounter = 100;

	// save File
	private static final String SERIAL_FILE = "folderMap";

	private boolean useBDB;

	private Job updateFactBaseJob;

	//
	// JQueryFactBase API
	//
	protected WorkingSetFactBase(Set resourceStrategies, boolean useBDB) {
		this.useBDB = useBDB;

		RuleBase.silent = true;
		RuleBase.useCache = true;
		RuleBase.softCache = true;

		updateFactBaseJob = null;

		workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		workingSet = null;

		workingSetListener = createWorkingSetListener();
		workingSetManager.addPropertyChangeListener(workingSetListener);
		resourceListener = createResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);

		this.resourceStrategies = resourceStrategies;

		// WorkingSet ID stuff
		restoreState();
	}

	protected int getFactId(String strRep) {
		return getRuleBaseManager().getFactId(strRep);
	}

	public String getName() {
		return current().getName();
	}

	public void setFactBaseByName(String name) throws JQueryException {
		IWorkingSet ws = workingSetManager.getWorkingSet(name);
		if (ws == null) {
			throw new JQueryTyRuBaException("Working set doesn't exist: " + name);
		} else {
			setCurrent(ws);
		}
	}

	public JQueryFactBase selectionDialog() {
		IWorkingSetSelectionDialog iws = workingSetManager.createWorkingSetSelectionDialog(JQueryBackendPlugin.getShell(), false);
		IWorkingSet old = current();
		IWorkingSet temp = null;

		iws.setSelection(current() != null //Do NOT put a null pointer in there or ...
		? new IWorkingSet[] { current() }
				: new IWorkingSet[0]);

		if (iws.open() == org.eclipse.jface.window.Window.OK && iws.getSelection() != null) {
			IWorkingSet[] selection = iws.getSelection();
			if (selection.length == 0) {
				JQueryBackendPlugin.message("Can't create a JQuery tab without a selected working set.\n" + "Please try again and select a WorkingSet.");
				temp = null;
			} else {
				IWorkingSet selectedSet = selection[0];
				workingSetManager.addRecentWorkingSet(selectedSet);
				temp = selectedSet;
			}
		} else {
			temp = null;
		}

		// only change the workingset if we've picked a new one
		if (temp != null) {
			setCurrent(temp);
			synchronizeCodeObjects(current(), getRuleBaseManager());
		}

		return this;
	}

	private void addElements(JQueryResourceStrategy strat, IAdaptable element, Set codeObjects) throws CoreException {
		if (element instanceof IContainer) {
			IContainer c = (IContainer) element.getAdapter(IContainer.class);
			IResource[] res = c.members();
			for (int i = 0; i < res.length; i++) {
				addElements(strat, res[i], codeObjects);
			}
		} else {
			strat.buildWorkingSet(element, codeObjects);
		}
	}

	public void addProject(IProject project) {
		Set stuffToPutInWorkingSet = new HashSet();
		Set folders = new HashSet();

		try {
			JQueryResourceStrategy[] strats = JQueryAPI.getInstalledResources();

			IResource[] res = project.members();
			for (int i = 0; i < res.length; i++) {
				for (int j = 0; j < strats.length; j++) {
					addElements(strats[j], res[i], stuffToPutInWorkingSet);
				}
			}
		} catch (CoreException e) {
			JQueryBackendPlugin.error(e);
		}

		// here we actually create the working set and select it 
		if (!stuffToPutInWorkingSet.isEmpty()) {
			IAdaptable[] x = (IAdaptable[]) stuffToPutInWorkingSet.toArray(new IAdaptable[stuffToPutInWorkingSet.size()]);
			IWorkingSet ws = workingSetManager.getWorkingSet(project.getName() + "/default");
			if (ws != null) {
				workingSetManager.removeWorkingSet(ws);
			}

			ws = workingSetManager.createWorkingSet(project.getName() + "/default", x);
			ws.setId(WORKING_SET_TYPE);
			workingSetManager.addWorkingSet(ws);

			IWorkingSet[] newWs = new IWorkingSet[0];
			if (current().isAggregateWorkingSet()) {
				AggregateWorkingSet g = (AggregateWorkingSet) current();
				IWorkingSet[] temp = g.getComponents();
				newWs = new IWorkingSet[temp.length + 1];

				newWs[0] = ws;
				for (int i = 0; i < temp.length; i++) {
					newWs[i + 1] = temp[i];
				}
			} else {
				newWs = new IWorkingSet[] { current(), ws };
			}

			String name = "JQueryWorkingSet";
			String label = "JQueryWorkingSet";
			ws = workingSetManager.getWorkingSet(name);
			if (ws != null) {
				workingSetManager.removeWorkingSet(ws);
			}

			ws = workingSetManager.createAggregateWorkingSet(name, label, newWs);
			workingSetManager.addWorkingSet(ws);

			setCurrent(ws);
			synchronizeCodeObjects(current(), getRuleBaseManager());
		}
	}

	public JQueryFactGenerator createResource(String name, JQueryResourceStrategy strategy) {
		RuleBaseManager rbm = getRuleBaseManager();
		return rbm.getResource(name);
	}

	public JQueryFactGenerator createResource(IAdaptable resource) {
		RuleBaseManager rbm = getRuleBaseManager();
		return rbm.getResource(resource);
	}

	public void reloadRules() {
		Collection managers = ruleBaseManagers.values();

		for (Iterator iter = managers.iterator(); iter.hasNext();) {
			((RuleBaseManager) iter.next()).reloadRulesFiles();
		}

		// TODO Potential bug here: maybe other RBM's won't have their rules updated after
		// a working set switch?
		//
		// This call only updates the current RBM
		updateRuleBaseManager();
	}

	public void reloadFacts() {
		forceRefresh();
	}

	//
	// Rest of Class
	//
	/**
	 * @return a unique id string for the given working set
	 */
	private String getUniqueID(IWorkingSet set) {
		Integer idNumber;
		idNumber = wsIDMap.get(set);
		if (idNumber == null) {
			idNumber = wsIDCounter;
			wsIDCounter = new Integer(idNumber.intValue() + 1);
			wsIDMap.put(set, idNumber);
		}
		return idNumber.toString();
	}

	protected RuleBaseManager getRuleBaseManager() {
		return getRuleBaseManager(current());
	}

	private RuleBaseManager getRuleBaseManager(IWorkingSet ws) {
		RuleBaseManager rbm = (RuleBaseManager) ruleBaseManagers.get(ws);

		try {
			if (rbm == null) {
				Set codeObjs = getCodeObjects(ws);
				String wsPath = getUniqueID(ws); // Id can be null for the empty working set.

				rbm = new RuleBaseManager(wsPath, codeObjs, resourceStrategies, useBDB);
				ruleBaseManagers.put(ws, rbm);
				updateRuleBaseManager();
			}
		} catch (ParseException e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		} catch (TypeModeError e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		} catch (IOException e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		}

		return rbm;
	}

	/**
	 * Forces a complete refresh of facts for this working set
	 */
	private void forceRefresh() {
		// TODO This method should check for new/removed items in the working set
		RuleBaseManager rbm = getRuleBaseManager();
		if (rbm == null) {
			return;
		}
		JQueryBackendPlugin.traceQueries("Forcing refresh for workingset: " + current().getName());
		try {
			rbm.initialize(getUniqueID(current()), getCodeObjects(current()), true, useBDB);
			synchronizeCodeObjects(current(), rbm);
			updateRuleBaseManager();
		} catch (ParseException e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		} catch (TypeModeError e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		} catch (IOException e) {
			JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		}
	}

	protected IWorkingSet current() {
		if (workingSet == null) {
			workingSet = workingSetManager.createWorkingSet("null", new IAdaptable[0]);
		}
		return workingSet;
	}

	protected void setCurrent(IWorkingSet ws) {
		// ignore the rest of this method if we're not changing the current working set
		if (ws.equals(current())) {
			return;
		}

		//		RuleBaseManager oldrbm = (RuleBaseManager) ruleBaseManagers.get(current());
		//		RuleBaseManager newrbm = (RuleBaseManager) ruleBaseManagers.get(ws);
		//
		//		if (oldrbm != null && newrbm != null) {
		//			oldrbm.shutdown();
		//			ruleBaseManagers.remove(current());
		//
		//			Set codeObjs = getCodeObjects(ws);
		//			String wsPath = getUniqueID(ws); // Id can be null for the empty working set.
		//
		//			try {
		//				newrbm.initialize(wsPath, codeObjs, false, useBDB);
		//				synchronizeCodeObjects(ws, newrbm);
		//				updateRuleBaseManager();
		//			} catch (ParseException e) {
		//				JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		//			} catch (TypeModeError e) {
		//				JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		//			} catch (IOException e) {
		//				JQueryBackendPlugin.error("Initializing RuleBaseManager: ", e);
		//			}
		//		}

		workingSet = ws;
	}

	protected void shutdown() {
		stopListening();
		Collection rbms = ruleBaseManagers.values();
		for (Iterator iter = rbms.iterator(); iter.hasNext();) {
			RuleBaseManager rbm = (RuleBaseManager) iter.next();
			rbm.shutdown();
		}

		saveState();
	}

	/**
	 * De-registers active listeners (namely workingSetListener and resourceListener)
	 */
	private void stopListening() {
		if (workingSetListener != null) {
			workingSetManager.removePropertyChangeListener(workingSetListener);
			workingSetListener = null;
		}

		if (resourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
			resourceListener = null;
		}

		if (buildListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(buildListener);
			buildListener = null;
		}

		JQueryBackendPlugin.traceQueries("WorkingSetManager stopped listening");
	}

	/**
	* Sets up the working set listener.
	*/
	private IPropertyChangeListener createWorkingSetListener() {
		return new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				RuleBaseManager rbm = null;
				IWorkingSet changedWSet = null;
				String prop = event.getProperty();

				if (prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD) || //
						prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE) || //
						prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
					changedWSet = (IWorkingSet) event.getNewValue();
					rbm = (RuleBaseManager) ruleBaseManagers.get(changedWSet);

					if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE) && rbm != null) {
						synchronizeCodeObjects(changedWSet, rbm);
					}
				}
			}
		};
	}

	private void updateRuleBaseManager() {
		if (updateFactBaseJob != null) {
			updateFactBaseJob.cancel();
		}

		updateFactBaseJob = new Job("Building JQuery factbase...") {
			private IProgressMonitor monitor;

			@Override
			protected void canceling() {
				monitor.setCanceled(true);
			}

			public IStatus run(IProgressMonitor mon) {
				monitor = mon;

				RuleBaseManager rbm = getRuleBaseManager();
				try {
					rbm.getFrontend().setProgressMonitor(new ProgressMonitor() {
						public void beginTask(String name, int totalWork) {
							monitor.beginTask(name, totalWork);
						}

						public void done() {
							monitor.done();
						}

						public boolean isCanceled() {
							return monitor.isCanceled();
						}

						public void worked(int units) {
							monitor.worked(units);
						}
					});
					rbm.getFrontend().updateBuckets();
				} catch (ParseException e) {
					JQueryBackendPlugin.error(e);
					monitor.setCanceled(true);
					return Status.CANCEL_STATUS;
				} catch (TypeModeError e) {
					JQueryBackendPlugin.error(e);
					monitor.setCanceled(true);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		updateFactBaseJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				JQueryTyRuBaAPI.postRefreshEvent(JQueryAPI.getRule());
			}
		});
		updateFactBaseJob.setRule(JQueryAPI.getRule());
		updateFactBaseJob.schedule();
	}

	/**
	 * Create a workspace resource listener
	 */
	private IResourceChangeListener createResourceListener() {
		return new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				// Get the root delta.
				IResourceDelta rootDelta = event.getDelta();
				Vector resourceChanges = processProjectDelta(rootDelta);

				Set workingSets = ruleBaseManagers.keySet();
				for (Iterator iter = workingSets.iterator(); iter.hasNext();) {
					IWorkingSet ws = (IWorkingSet) iter.next();

					// check each (active) working set for changes
					RuleBaseManager rbm = (RuleBaseManager) ruleBaseManagers.get(ws);

					Iterator changeIter = resourceChanges.iterator();
					while (changeIter.hasNext()) {
						ResourceChange rc = (ResourceChange) changeIter.next();
						// check if changed element is a part of this working set
						if (isEnclosed(rc.element, ws)) {
							// determine type of change that occurred
							switch (rc.changeKind) {
							case IResourceDelta.ADDED:
								rbm.fileAdded(rc.element);
								break;
							case IResourceDelta.CHANGED:
								rbm.fileChanged(rc.element);
								break;
							case IResourceDelta.REMOVED:
								rbm.fileRemoved(rc.element);
								break;
							default:
								JQueryBackendPlugin.traceUI("Invalid change type for ResourceChange");
							}
						}
					}
				}

				updateRuleBaseManager();
			}
		};
	}

	private void synchronizeCodeObjects(IWorkingSet changedWSet, RuleBaseManager rbm) {
		// Get old and new sets of compilation units for this working set
		Set oldCodeObjects = rbm.getCodeObjects();
		Set newCodeObjects = getCodeObjects(changedWSet);
		boolean needUpdate = false;

		// find removed elements, notify RuleBaseManager of removal
		LinkedHashSet removedCodeObjects = new LinkedHashSet(oldCodeObjects);
		removedCodeObjects.removeAll(newCodeObjects);
		for (Iterator removed = removedCodeObjects.iterator(); removed.hasNext();) {
			Object next = removed.next();
			rbm.fileRemoved((IAdaptable) next);
		}

		// find added elements, notify RuleBaseManager of addition
		LinkedHashSet addedCodeObjects = new LinkedHashSet(newCodeObjects);
		addedCodeObjects.removeAll(oldCodeObjects);
		for (Iterator added = addedCodeObjects.iterator(); added.hasNext();) {
			rbm.fileAdded((IAdaptable) added.next());
		}

		needUpdate = !addedCodeObjects.isEmpty() || !removedCodeObjects.isEmpty();
		if (needUpdate) {
			updateRuleBaseManager();
		}
	}

	/**
	 * method which should be called to find all compilation units from the IWorkingSet's elements
	 */
	private Set getCodeObjects(IWorkingSet wSet) {
		IAdaptable[] elements = wSet == null ? new IAdaptable[0] : wSet.getElements();
		Set codeObjects = new LinkedHashSet();

		for (int i = 0; i < elements.length; i++) {
			IAdaptable element = elements[i];
			for (Iterator iter = resourceStrategies.iterator(); iter.hasNext();) {
				JQueryResourceStrategy strat = (JQueryResourceStrategy) iter.next();
				strat.addApplicableElementToCollection(element, codeObjects);
			}
		}

		return codeObjects;
	}

	/**
	 * Tests if the given resource is enclosed by a working set element, and false otherwise.
	 * 
	 * The IContainmentAdapter of each working set element is used for the containment test.
	 * 
	 * @param element
	 *            resource to test for enclosure by a working set element
	 * @return true if element is enclosed by a working set element and false otherwise.
	 */
	// note: adapted from org.eclipse.ui.ResourceWorkingSetFilter.isEclosed method
	private boolean isEnclosed(IAdaptable element, IWorkingSet workingSet) {
		IAdaptable[] workingSetElements = workingSet.getElements();

		for (int i = 0; i < workingSetElements.length; i++) {
			IAdaptable workingSetElement = workingSetElements[i];
			IContainmentAdapter containmentAdapter = (IContainmentAdapter) workingSetElement.getAdapter(IContainmentAdapter.class);

			// if there is no IContainmentAdapter defined for the working set element type fall back to using resource based containment check
			if (containmentAdapter != null) {
				try {
					if (containmentAdapter.contains(workingSetElement, element, IContainmentAdapter.CHECK_CONTEXT | IContainmentAdapter.CHECK_IF_CHILD | IContainmentAdapter.CHECK_IF_DESCENDANT))
						/* | IContainmentAdapter.CHECK_IF_ANCESTOR */
						return true;
				} catch (NullPointerException npe) {
					// There is currently a bug in JaveElementContainmentAdapter class that throws an NPE (Bugzilla bug 38971). We should catch it here to avoid losing all of the updates.
					JQueryBackendPlugin.traceQueries("NPE thrown by eclipse's containmentAdapter.contains method:\n" + "\t working set: " + workingSet.getName() + "\n" + "\t element: " + element);
				}
			}

			// if they're equal, then our element is in the working set
			IResource elementResource = (IResource) element.getAdapter(IResource.class);
			if (elementResource != null) {
				if (workingSetElement.equals(elementResource)) {
					return true;
				}
			}

			// if the ws element is an IContainer, check if the element is a descendant
			if (workingSetElement instanceof IContainer && checkDescendant(elementResource, (IContainer) workingSetElement)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * @param element to check
	 * @param container to check
	 * @return true if the element is a descendant of the folder
	 */
	private boolean checkDescendant(IResource element, IContainer container) {
		boolean result = element.getParent().equals(container);

		try {
			IResource[] res = container.members();
			for (int i = 0; !result && i < res.length; i++) {
				if (res[i] instanceof IContainer) {
					result = checkDescendant(element, (IContainer) res[i]);
				} else {
					result = res[i].equals(element.getParent());
				}
			}
		} catch (CoreException e) {
			return result;
		}

		return result;
	}

	/**
	 * Method processProjectDelta.
	 * 
	 * @param projectDelta
	 * @return Returns a list of class files that have been
	 */
	private Vector processProjectDelta(IResourceDelta projectDelta) {

		// Create the delta visitor.
		ProjectDeltaVisitor visitor = new ProjectDeltaVisitor();

		// Put the visitor to work
		try {
			projectDelta.accept(visitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return (visitor.affectedFiles());
	}

	private class ProjectDeltaVisitor implements IResourceDeltaVisitor {
		private Vector affectedFiles = new Vector();

		public boolean visit(IResourceDelta delta) {

			for (Iterator iter = resourceStrategies.iterator(); iter.hasNext();) {
				JQueryResourceStrategy strat = (JQueryResourceStrategy) iter.next();
				IAdaptable element = strat.resourceDelta(delta);
				if (element != null) {
					affectedFiles.add(new ResourceChange(element, delta.getKind()));
				}
			}
			return true;
		}

		public Vector affectedFiles() {
			return affectedFiles;
		}
	}

	private class ResourceChange {
		int changeKind; // use IResourceDelta.ADDED, CHANGED,or REMOVED ...

		IAdaptable element;

		public ResourceChange(IAdaptable element, int changeKind) {
			this.element = element;
			this.changeKind = changeKind;
		}
	}

	private void saveState() {
		File f = JQueryBackendPlugin.getDefault().getStateLocation().append(SERIAL_FILE).toFile();
		Map<String, Integer> temp = new HashMap<>(wsIDMap.size());
		for (Iterator<IWorkingSet> iter = wsIDMap.keySet().iterator(); iter.hasNext();) {
			IWorkingSet set = (IWorkingSet) iter.next();
			temp.put(set.getName(), wsIDMap.get(set));
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

			oos.writeObject(temp);
			oos.writeObject(wsIDCounter);

			oos.close();
		} catch (IOException e) {
			throw new Error("Error occurred while writing file: " + e);
		}
	}

	@SuppressWarnings("unchecked")
	private void restoreState() {
		File f = JQueryBackendPlugin.getDefault().getStateLocation().append(SERIAL_FILE).toFile();
		if (f.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));

				HashMap<String, Integer> temp = (HashMap<String, Integer>) ois.readObject();
				wsIDMap = new HashMap<>(temp.size());
				for (Iterator<String> iter = temp.keySet().iterator(); iter.hasNext();) {
					String setName = (String) iter.next();
					IWorkingSet set = workingSetManager.getWorkingSet(setName);
					if (set != null) {
						wsIDMap.put(set, temp.get(setName));
					} else {
						JQueryBackendPlugin.traceQueries("WorkingSetRuleBaseMapper.restoreState: working set \"" + setName + "\" no longer exists.  Could not restore");
					}
				}

				wsIDCounter = (Integer) ois.readObject();

				ois.close();
			} catch (Exception e) {
				throw new Error("Error occurred while reading connection file: " + e);
			}
		}
	}
}
