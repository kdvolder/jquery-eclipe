package ca.ubc.jquery.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import ca.ubc.jquery.JQueryBackendPlugin;

/**
 * A mininal API to allow user to plugin to JQuery and run queries. 
 * NOTE: We're in testing stages and this API may be extended in the future 
 * with additional functionality.
 * 
 * @author lmarkle
 */
public abstract class JQueryAPI {

	private static final String IMAGE_CLASS_NAME = "org.eclipse.jdt.internal.ui.JavaPluginImages";

	protected static Point BIG_SIZE = new Point(22, 16);

	protected Class imageClass;

	protected static Map resources = null;

	protected static List definitionFiles = null;

	private static Map imageMap = null;

	private static Map updateTargets = null;

	private static ISchedulingRule schedulingRule = null;

	private static List eventListener = null;

	// Singleton... for an API... it's complicated :(
	private static JQueryAPI instance = null;

	{
		if (schedulingRule == null) {
			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
			//schedulingRule = ruleFactory.buildRule();
			schedulingRule = new JQuerySchedulingRule();
			resources = new HashMap();
			definitionFiles = new ArrayList();
			eventListener = new ArrayList();
			imageMap = new HashMap();
			updateTargets = new HashMap();
		}
	}

	protected JQueryAPI() {
		imageClass = null;
		try {
			imageClass = Class.forName(IMAGE_CLASS_NAME);
			imageMap.put(null, new JavaElementImageDescriptor(getImageDescriptor("String"), SWT.NONE, getImageSize()).createImage());
		} catch (ClassNotFoundException e) {
			JQueryBackendPlugin.traceUI("ElementLabelProvider: couldn't locate class: " + IMAGE_CLASS_NAME);
		}
	}

	protected static JQueryAPI getInstance() {
		return instance;
	}

	/**
	 * This method enables the selected API.
	 * 
	 * Because APIs are initialized as extension points, here is where we detect one has
	 * been activated.  We must do any work here invovled with setting up persistent state
	 * so that the new API is ready to use.
	 * 
	 * For instance, we need to clean up any state associated with the current API and 
	 * replace it with things for the new API (ex: update targets)
	 * 
	 */
	public final void setInstance() {
		if (instance != this) {
			// clear old state
			saveUpdateTargets();
			JQueryEditorSelectionListener.getInstance().disable();

			// initialize new one
			setInstance(this);
		}
	}

	private void setInstance(JQueryAPI p) {
		instance = p;
	}

	// 
	// Rest of class
	//

	// For Job Scheduling... this is here so that we never freeze the UI
	public final static ISchedulingRule getRule() {
		//		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		//		return ruleFactory.buildRule();
		return schedulingRule;
	}

	public final static Point getImageSize() {
		return BIG_SIZE;
	}

	protected void setImageSize(int x, int y) {
		BIG_SIZE = new Point(x, y);
		try {
			imageClass = Class.forName(IMAGE_CLASS_NAME);

			imageMap.clear();
			imageMap.put(null, new JavaElementImageDescriptor(getImageDescriptor("String"), SWT.NONE, getImageSize()).createImage());
		} catch (ClassNotFoundException e) {
			JQueryBackendPlugin.traceUI("ElementLabelProvider: couldn't locate class: " + IMAGE_CLASS_NAME);
		}
	}

	protected static void clearImageMap() {
		// clear image cache
		for (Iterator it = imageMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry x = (Map.Entry) it.next();
			((Image) x.getValue()).dispose();
		}
		imageMap.clear();
	}

	//
	// resource strategies
	//
	public final static JQueryResourceStrategy[] getInstalledResources() {
		JQueryResourceStrategy[] result = new JQueryResourceStrategy[resources.size()];
		int i = 0;

		for (Iterator it = resources.values().iterator(); it.hasNext(); i++) {
			result[i] = (JQueryResourceStrategy) it.next();
		}

		return result;
	}

	public final static void installResource(String name, JQueryResourceStrategy rs) {
		if (rs != null) {
			resources.put(name, rs);
			getInstance()._updateResources();
		}
	}

	public final static void removeResource(String name) {
		resources.remove(name);
		getInstance()._updateResources();
	}

	public final static void updateResources() {
		getInstance()._updateResources();
	}

	public static final List getDefinitionFiles() {
		return definitionFiles;
	}

	public final static void installDefinitionFile(File f) {
		if (!definitionFiles.contains(f)) {
			definitionFiles.add(f);
		}
	}

	public final static void removeDefinitionFile(File f) {
		definitionFiles.remove(f);
	}

	//
	// Event Listeners
	//
	public final static void addListener(JQueryEventListener listener) {
		if (!eventListener.contains(listener)) {
			eventListener.add(listener);
		}
	}

	public final static void removeListener(JQueryEventListener listener) {
		eventListener.remove(listener);
	}

	protected static void postEvent(JQueryEvent.EventType type, Object source) {
		JQueryEvent ev = new JQueryEvent(type, source);
		postEvent(ev);
	}

	protected static void postEvent(JQueryEvent event) {
		for (Iterator it = eventListener.iterator(); it.hasNext();) {
			JQueryEventListener listener = (JQueryEventListener) it.next();
			listener.handleEvent(event);
		}
	}

	// override these methods to create an API for a different factbase
	protected abstract void _updateResources();

	protected abstract JQueryResultSet _topLevelQuery() throws JQueryException;

	protected abstract JQueryResultSet _menuQuery(Object[] targets) throws JQueryException;

	protected abstract JQuery _filterQuery(Object[] targets) throws JQueryException;

	protected abstract JQuery _queryPredicates() throws JQueryException;

	protected abstract JQuery _createQuery(String q) throws JQueryException;

	protected abstract String _getThisVar();

	protected abstract String _getIdentityQuery();

	protected abstract Object _getFileElement(Object target);

	protected abstract void _getElementFromFile(String fileName, int offset, int length, Set context, Set element) throws JQueryException;

	protected abstract Object _getObjectProperty(Object obj, String propertyName);
	
	protected abstract String _getStringProperty(Object obj, String propertyName);

	protected abstract int _getIntProperty(Object obj, String propertyName);

	protected abstract String _getElementLabel(Object target);

	protected abstract String _getElementType(Object target);

	protected abstract void _addRule(String rule) throws JQueryException;

	protected abstract IJavaElement _getJavaModelElement(Object target) throws JQueryException;

	protected abstract Object _getElementFromJavaModel(IJavaElement element) throws JQueryException;

	protected abstract JQueryFactBase _selectFactBase() throws JQueryException;

	protected abstract JQueryFactBase _getFactBase();

	protected abstract JQueryUpdateTarget _createUpdateTarget(String targetName);

	protected abstract void _shutdown();

	// Simple default implementation of getElementImage...
	protected ImageDescriptor _getElementImageDescriptor(Object target) {
		return imageHelper(target);
	}

	private ImageDescriptor imageHelper(Object element) {
		int adornmentFlags = computeAdornmentFlags(element);
		ImageDescriptor baseImage = getBaseImageDescriptor(element);
		if (baseImage != null) {
			return new JavaElementImageDescriptor(baseImage, adornmentFlags, getImageSize());
		} else {
			return null;
		}
	}

	private ImageDescriptor getBaseImageDescriptor(Object element) {
		ImageDescriptor img = null;
		String baseImageName = JQueryAPI.getStringProperty(element, "baseImage");

		if (baseImageName != null && !baseImageName.equals("null")) {
			img = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(baseImageName);

			if (img == null) {
				try {
					Field imageField = imageClass.getField(baseImageName);
					img = (ImageDescriptor) imageField.get(null);
				} catch (Exception e) {
					img = getImageDescriptor(baseImageName);

					if (img == null) {
						JQueryBackendPlugin.traceUI("ElementLabelProvider.getBaseImageDescriptor: no such image field or illegal field access: " + baseImageName);
					}
				}
			}
		}

		return img;
	}

	private int computeAdornmentFlags(Object element) {
		return JQueryAPI.getIntProperty(element, "adornmentFlags");
	}

	/**
	 * Because of the need for serialization of certain classes and because these classes
	 * could be extended by other plugins, we need to check the current APIs bundle context
	 * and ask its class loader for those classes.  It seems Eclipse (or perhaps more precisely
	 * Equinox) has a separate class loader for every plugin.  This makes it sort of 
	 * convenient for some things but serialization then becomes quite complex because one
	 * plugin could serialize data from another and not be able to restore it.  This method
	 * helps us by searching in the APIs class loader for the class.
	 * 
	 * @param desc The description of the class to be loaded
	 * @return the class 
	 * @throws ClassNotFoundException if the class can't be found
	 */
	protected final static Class resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
		return getInstance().getClass().getClassLoader().loadClass(desc.getName());
	}

	// public query api stuff
	/**
	 * Returns the image registry for JQuery
	 */
	protected final static ImageDescriptor getImageDescriptor(String name) {
		ImageDescriptor result = JQueryBackendPlugin.getDefault().getImageRegistry().getDescriptor(name);

		for (Iterator it = resources.values().iterator(); result == null && it.hasNext();) {
			JQueryResourceStrategy rs = (JQueryResourceStrategy) it.next();
			result = rs.getImageDescriptor(name);
		}

		if (result == null) {
			result = JQueryBackendPlugin.getImageDescriptor(name);
		}

		return result;
	}

	/**
	 * Target element should be one selected from the database.  Because this methods selects
	 * images from the back end plugin image registry, we can also use it for certain other 
	 * special cases:
	 * @param target can be an object from the database or one the following  special case strings:
	 * 	1) "Error" will given the plugin error image
	 * 	2) "String" will give the plugin string image
	 * 	3) "Close" a close icon
	 * 	4) "Query" the JQuery icon
	 *  5) "Warning" a warning icon
	 *  6) "Task" the task icon
	 *  7) "Bookmark" a bookmark icon
	 *  8) "UpArrow" or "DownArrow" for appropriate arrow icons
	 *  
	 * @return the image used for the target element
	 */
	public final static Image getElementImage(Object target) {
		ImageDescriptor x = JQueryBackendPlugin.getDefault().getImageRegistry().getDescriptor(target.toString());

		if (x == null) {
			x = getInstance()._getElementImageDescriptor(target);
		} else {
			x = new JavaElementImageDescriptor(x, SWT.NONE, getImageSize());
		}

		Image result = (Image) imageMap.get(x);
		if (result == null) {
			result = x.createImage();
			imageMap.put(x, result);
		}

		return result;
	}

	/**
	 * @return the string used for the this variable in the database.
	 */
	public final static String getThisVar() {
		return getInstance()._getThisVar();
	}

	/**
	 * @return a query with a ThisVar bound to the only other var.
	 */
	public final static String getIdentityQuery() {
		return getInstance()._getIdentityQuery();
	}

	/**
	 * @return the current fact base
	 */
	public final static JQueryFactBase getFactBase() {
		return getInstance()._getFactBase();
	}

	/**
	 * Runs a selection dialog and returns the selected fact base.
	 * 
	 * @throws JQueryException
	 *             if the selected fact base is invalid
	 * @return the selected fact base
	 */
	public final static JQueryFactBase selectFactBase() throws JQueryException {
		return getInstance()._selectFactBase();
	}

	/**
	 * @return a precompiled query representing the possible top level queries
	 */
	public final static JQueryMenuResults topLevelQuery() throws JQueryException {
		return new JQueryMenuResults(getInstance()._topLevelQuery());
	}

	/**
	 * @param targets
	 *            list of targets to query or null if there is no target
	 * @return a precompiled query representing the possible queries for the given selection of objects. Results here are can vary depending on the type of objects inside the targets array.
	 */
	public final static JQueryMenuResults menuQuery(Object[] targets) throws JQueryException {
		return new JQueryMenuResults(getInstance()._menuQuery(targets));
	}

	/**
	 * @return a precompiled query representing the query predicates.
	 */
	public final static JQuery queryPredicates() throws JQueryException {
		return getInstance()._queryPredicates();
	}

	/**
	 * @param targets list of targets to apply filters to
	 * @return a precompiled query representing the list of possible filters for the given item
	 */
	public final static JQuery filterQuery(Object[] targets) throws JQueryException {
		return getInstance()._filterQuery(targets);
	}

	/**
	 * @return a query for the given query string
	 */
	public final static JQuery createQuery(String q) throws JQueryException {
		return getInstance()._createQuery(q);
	}

	/**
	 * Creates a JQuery for the given memento
	 * @param memento
	 * @return a new JQuery reflecting the contents of the memento
	 * @throws JQueryException
	 */
	public final static JQuery createQuery(IMemento memento) throws JQueryException {
		JQuery result = JQuery.createQuery(memento);
		return result;
	}

	/**
	 * An alias for a specific kind of query.
	 */
	public final static JQueryFileElement getFileElement(Object target) {
		return (JQueryFileElement) getInstance()._getFileElement(target);
	}

	/**
	 * @param fileName name of the file
	 * @param offset position in the file
	 * @param length length of the area to search
	 * @return any JQuery elements that occur in the given file from offset to offset+length
	 */
	public final static void getElementFromFile(String fileName, int offset, int length, Set context, Set element) throws JQueryException {
		//		System.out.println("reverseLocation(" + fileName + "," + offset + "," + length + ")");
		getInstance()._getElementFromFile(fileName, offset, length, context, element);
	}

	/**
	 * Retrieves a property on the given element by executing a predicated name propertyName.
	 */
	public final static Object getObjectProperty(Object target, String propertyName) {
		return getInstance()._getObjectProperty(target, propertyName);
	}

	/**
	 * like getProperty, but convert result to a String
	 */
	public final static String getStringProperty(Object target, String propertyName) {
		return getInstance()._getStringProperty(target, propertyName);
	}
	
	/**
	 * @param target
	 * @return a user friendly string representation for the target
	 */
	public static String getElementLabel(Object target) {
		return getInstance()._getElementLabel(target);
	}

	/**
	 * @param target
	 * @return a user friendly type name for the target
	 */
	public final static String getElementType(Object target) {
		return getInstance()._getElementType(target);
	}

	/**
	 * like getProperty, but convert result to an int (must be RBJavaObject with an 
	 * Integer inside or will throw some nasty execeptions)
	 */
	public final static int getIntProperty(Object target, String propertyName) {
		return getInstance()._getIntProperty(target, propertyName);
	}

	/**
	 * Returns an eclipse IJavaElement from the JavaModel which represents the given 
	 * JQuery object.
	 */
	public final static IJavaElement getJavaModelElement(Object target) throws JQueryException {
		return getInstance()._getJavaModelElement(target);
	}

	/**
	 * @param element
	 * @return returns the JQuery representation of the given JDT JavaModel element
	 * @throws JQueryException
	 */
	public final static Object getElementFromJavaModel(IJavaElement element) throws JQueryException {
		return getInstance()._getElementFromJavaModel(element);
	}

	/**
	 * Experimental - AWJB
	 * @param rule Text of the rule to add
	 * @throws JQueryException if rule cannot be parsed or added
	 */
	public final static void addRule(String rule) throws JQueryException {
		getInstance()._addRule(rule);
	}

	/**
	 * This needs to be called to release/close any resources held by the Backend.
	 *  
	 * E.g. if this is not called the database files may not be properly closed and 
	 * the database may be corrupted.
	 */
	public final static void shutdown() {
		clearImageMap();
		JQueryEditorSelectionListener.getInstance().disable();

		// write update targets information to save file
		// saveUpdateTargets();

		getInstance()._shutdown();
	}

	private static void saveUpdateTargets() {
		if (!updateTargets.isEmpty()) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFile()));
				writeUpdateTargets(oos);
				oos.close();
				updateTargets.clear();
			} catch (IOException e) {
				JQueryBackendPlugin.error("Saving update targets: ", e);
			}
		}
	}

	private static File getSaveFile() {
		IPath saveFile = JQueryBackendPlugin.getDefault().getStateLocation().append(".jquery-api");
		return saveFile.toFile();
	}

	private static void readUpdateTargets(ObjectInputStream ois) throws IOException {
		int max = ois.readInt();
		for (int i = 0; i < max; i++) {
			String t = ois.readUTF();
			getUpdateTarget(t);
		}
	}

	private static void writeUpdateTargets(ObjectOutputStream oos) throws IOException {
		oos.writeInt(updateTargets.size() - 2);
		for (Iterator it = updateTargets.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			JQueryUpdateTarget t = (JQueryUpdateTarget) me.getValue();
			// don't save editor targets
			if (!("Editor").equals(t.getName()) && !("Editor (Elements)").equals(t.getName())) {
				oos.writeUTF(t.getName());
			}
		}
	}

	/**
	 *  ------------------------------------------------------------------------------------
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 */
	public final static Collection getUpdateTargets() {
		return updateTargets.values();
	}

	public final static JQueryUpdateTarget getUpdateTarget(String targetName) {
		for (Iterator it = updateTargets.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			JQueryUpdateTarget t = (JQueryUpdateTarget) me.getValue();
			if (t.getName().equals(targetName)) {
				return t;
			}
		}
		return createUpdateTarget(targetName);
	}

	public final static JQueryUpdateTarget createUpdateTarget(String targetName) {
		// This seems like a bad place to initialize this object.  The problem is that 
		// the editor selection creates some targets but creating targets depends on the 
		// API already being initalized.  So we have to enable it here to ensure the API
		// is fully enabled before we try to get targets that don't exist.
		if (!JQueryEditorSelectionListener.getInstance().isEnabled()) {
			JQueryEditorSelectionListener.getInstance().enable();

			// reads update target information from save file
			//			try {
			//				if (getSaveFile().exists()) {
			//					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSaveFile()));
			//					readUpdateTargets(ois);
			//					ois.close();
			//				}
			//			} catch (IOException e) {
			//				JQueryBackendPlugin.error("Saving update targets: ", e);
			//			}

			JQueryUpdateTarget t = getUpdateTarget(targetName);
			if (t != null) {
				return t;
			}
		}

		JQueryUpdateTarget x = getInstance()._createUpdateTarget(targetName);
		updateTargets.put(x.getId(), x);
		return x;
	}

	/**
	 * Removes the given target from the update targets list.
	 * Does nothing if the given target is null.
	 * @param target
	 */
	public final static void removeUpdateTarget(JQueryUpdateTarget target) {
		// always remove targets (EXCEPT when the workbench is closing...)
		if (!PlatformUI.getWorkbench().isClosing() && target != null) {
			updateTargets.remove(target.getId());
			postEvent(new JQueryEvent(JQueryEvent.EventType.RemoveUpdateTarget, target));
		}
	}
}
