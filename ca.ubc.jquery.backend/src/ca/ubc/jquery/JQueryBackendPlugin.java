package ca.ubc.jquery;

//import java.io.BufferedWriter;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryPredicateInstaller;
import ca.ubc.jquery.api.JQueryResourceStrategy;
import ca.ubc.jquery.preferences.JQueryBackendPreferences;
import ca.ubc.jquery.preferences.JQueryTyrubaPreferencePage;
import ca.ubc.jquery.util.Util;

/**
 * The main class for the JQuery plugin. This class takes care of the following tasks:
 * <ul>
 * <li> Maintains some useful information such as the plugin install directory and the current
 *      tracing options.
 * <li> Coordinates the interaction between views. Each view must register itself when it 
 *     opens and deregister itself when it closes. Communication between views is handled 
 *     using event listeners. This class updates the appropriate listeners
 *     when a view opens or closes.
 * </ul>
 */
public class JQueryBackendPlugin extends AbstractUIPlugin implements ISaveParticipant {

	private static final String FACTBASE_EXTENSION = "factbase";

	private static final String RESOURCE_EXTENSION = "resource";

	private static final String JAVAVISITOR_EXTENSION = "javaVisitor";

	private static final String INCLUDERULES_EXTENSION = "includeRules";

	private static final String PREDICATES_EXTENSION = "predicates";
	
	/** The name of the plugin */
	public static final String PLUGIN_NAME = "JQuery Backend";

	/** The version of the plugin */
	public static String PLUGIN_VERSION = "JQUERY VERSION 4_0_0";

	/** Path to icons folder relative to installPath */
	private static final String iconPath = "icons/";

	/** True if the tracing option ca.ubc.jquery/debug/ui is on. */
	private static boolean isDebuggingUI = true;

	/** True if the tracing option ca.ubc.jquery/debug/queries is on. */
	private static boolean isDebuggingQueries;

	/** True if query execution needs to be logged */
	private static boolean isLoggingQueries;

	/** Manage extension points */
	private IExtensionTracker tracker;

	private FactbaseExtensionHandler factbaseExtensionHandler;

	private ResourceExtensionHandler resourceExtensionHandler;

	private JavaVisitorExtensionHandler javaVisitorHandler;

	private IncludeRulesExtensionHandler includeRulesHandler;
	
	private PredicatesExtensionHandler predicatesHandler;

	private Map apiExtensions;

	private Map resourceExtensions;

	private Set includeOptionalRuleExtensions;

	private Set includeRequiredRuleExtensions;
	
	private Set<JQueryPredicateInstaller> predicateInstallers;
	
	/**
	 * True if user wants to have detailed information from .class files. False if skeletal signature 
	 * information is enough. TODO: This option is currently not supported. Should be turned of. (Actually 
	 * an implementation exists but has not been tested in a very long time, probably doesn't quite work).
	 */
	private static final boolean detailedClassFiles = false;

	private static boolean parseDependencies = false;

	/** Factbase cache size */
	private static int cacheSize;

	/** Maximum number of results to display for a single JQuery query */
	private static int maxResults;

	/** The shared instance. */
	private static JQueryBackendPlugin plugin = null;

	/** The plugin's shell. */
	private static Shell shell = null;

	public JQueryBackendPlugin() {
		super();

		plugin = this;

		apiExtensions = new HashMap();
		resourceExtensions = new HashMap();
		includeOptionalRuleExtensions = new HashSet();
		includeRequiredRuleExtensions = new HashSet();
		predicateInstallers = new HashSet<JQueryPredicateInstaller>();
	}

	//
	// Extension Point stuff
	//

	//
	// Java Visitor Extension
	//
	public static JavaVisitorExtensionHandler getJavaVisitorHandler() {
		return getDefault().javaVisitorHandler;
	}

	//
	// Resource Extensions
	//
	/**
	 * Returns a List<String> with the names of all the resources installed.
	 */
	public static List getListOfResourceExtensions() {
		List result = new ArrayList();
		Iterator it = getDefault().resourceExtensions.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			result.add(o);
		}

		return result;
	}

	protected static void installResource(String name, IExtension p) {
		getDefault().resourceExtensions.put(name, p);
	}

	public static void activateResource(String name) {
		Object temp = getDefault().resourceExtensions.get(name);
		if (temp != null) {
			if (temp instanceof Object[]) {
				Object[] o = (Object[]) temp;
				JQueryAPI.installResource(name, (JQueryResourceStrategy) o[0]);
				for (Iterator it = ((List) o[1]).iterator(); it.hasNext();) {
					JQueryAPI.installDefinitionFile((File) it.next());
				}
			} else {
				List files = new ArrayList();
				JQueryResourceStrategy rs = getDefault().resourceExtensionHandler.loadExtension((IExtension) temp, files);

				// install resource
				JQueryAPI.installResource(name, rs);
				for (Iterator it = files.iterator(); it.hasNext();) {
					JQueryAPI.installDefinitionFile((File) it.next());
				}

				// overwrite map entry with JQueryResourceStrategy instance
				getDefault().resourceExtensions.put(name, new Object[] { rs, files });
			}

			JQueryAPI.updateResources();
		}
	}

	public static void disableResource(String name) {
		JQueryAPI.removeResource(name);

		Object resource = getDefault().resourceExtensions.get(name);
		if (resource instanceof Object[]) {
			List files = (List) ((Object[]) resource)[1];
			for (Iterator it = files.iterator(); it.hasNext();) {
				JQueryAPI.removeDefinitionFile((File) it.next());
			}
		}
	}

	protected static void removeResource(String name) {
		disableResource(name);
		getDefault().resourceExtensions.remove((String) name);
	}

	//
	// Factbase Extensions
	//
	public static List getListOfAPIs() {
		List result = new ArrayList();
		Iterator it = getDefault().apiExtensions.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			result.add(o);
		}

		return result;
	}

	protected static void installAPI(String name, IExtension p) {
		getDefault().apiExtensions.put(name, p);
	}

	public static void activateAPI(String name) {
		Object temp = getDefault().apiExtensions.get(name);
		if (temp != null) {
			JQueryAPI api = null;

			if (temp instanceof JQueryAPI) {
				api = (JQueryAPI) temp;
			} else {
				api = getDefault().factbaseExtensionHandler.loadExtension((IExtension) temp);
				// overwrite map entry with an API instance
				getDefault().apiExtensions.put(name, api);
			}

			if (api == null) {
				error("Cannot initialize backend: " + name);
			} else {
				api.setInstance();
				JQueryAPI.updateResources();
			}
		}
	}

	protected static void removeAPI(Object name) {
		getDefault().apiExtensions.remove((String) name);
	}
	
	//
	// Include Rules Extensions
	//
	
	protected static void installIncludeRequiredRuleExtension (String name) {
		getDefault().includeRequiredRuleExtensions.add(name);
	}
	
	protected static void removeIncludeRequiredRuleExtension (String name) {
		getDefault().includeRequiredRuleExtensions.remove(name);
	}
	
	protected static void installIncludeOptionalRuleExtension (String name) {
		getDefault().includeOptionalRuleExtensions.add(name);
	}
	
	protected static void removeIncludeOptionalRuleExtension (String name) {
		getDefault().includeOptionalRuleExtensions.remove(name);
	}
	
	public static Collection getRequiredRuleExtensions () {
		return Collections.unmodifiableCollection(getDefault().includeRequiredRuleExtensions);
	}
	
	public static Collection getOptionalRuleExtensions () {
		return Collections.unmodifiableCollection(getDefault().includeOptionalRuleExtensions);
	}
	
	protected static void addPredicateInstaller(JQueryPredicateInstaller installer) {
		getDefault().predicateInstallers.add(installer);
	}

	protected static void removePredicateInstaller(JQueryPredicateInstaller installer) {
		getDefault().predicateInstallers.remove(installer);
	}
	
	public static Collection<JQueryPredicateInstaller> getPredicateInstallers()
	{
		return Collections.unmodifiableCollection(getDefault().predicateInstallers);
	}
	
	//
	// Rest of class...
	//
	public void updatePreferences(boolean initializing) {
		IPreferenceStore store = getPreferenceStore();
		if (initializing) {
			initDefaultPreferences(store);
		}

		activateAPI(store.getString(JQueryBackendPreferences.P_JQUERY_BACKEND_FACTBASE));
		trace(true, store.getString(JQueryBackendPreferences.P_JQUERY_BACKEND_FACTBASE) + " activated");
		JQueryBackendPreferences.activateJQueryResources();

		// update all the variables based on the new values in the preference
		// store
		isDebuggingUI = store.getBoolean(JQueryBackendPreferences.P_DEBUG_UI);
		isDebuggingQueries = store.getBoolean(JQueryBackendPreferences.P_DEBUG_QUERY);
		isLoggingQueries = store.getBoolean(JQueryBackendPreferences.P_LOG_QUERIES);

		cacheSize = store.getInt(JQueryTyrubaPreferencePage.P_CACHE_SIZE);
		parseDependencies = store.getBoolean(JQueryTyrubaPreferencePage.P_PARSE_DEPENDENCIES);

		// isLoggingUser = store.getBoolean(JQueryPreferencePage.P_LOG_USER);
	}

	private void initDefaultPreferences(IPreferenceStore store) {
		store.setDefault(JQueryBackendPreferences.P_DEBUG_QUERY, false);
		store.setDefault(JQueryBackendPreferences.P_DEBUG_UI, false);
		store.setDefault(JQueryBackendPreferences.P_LOG_QUERIES, false);
		// store.setDefault(JQueryPreferencePage.P_LOG_USER, false);
		store.setDefault(JQueryBackendPreferences.P_JQUERY_BACKEND_FACTBASE, "Tyruba backend");

		// by default, all resources are activated
		store.setDefault(JQueryBackendPreferences.P_INSTALLED_RESOURCES, "");

		store.setDefault(JQueryTyrubaPreferencePage.P_CACHE_SIZE, 15000);
		store.setDefault(JQueryTyrubaPreferencePage.P_CLASSFILES_DETAILED, false);
		store.setDefault(JQueryTyrubaPreferencePage.P_PARSE_DEPENDENCIES, false);

		File rulesDir = new File(JQueryBackendPlugin.getInstallPath(), "rules/");
		StringBuffer fileList = new StringBuffer();
		fileList.append(rulesDir + File.separator + "menu.rub*");
		fileList.append(rulesDir + File.separator + "filter.rub*");
		fileList.append(rulesDir + File.separator + "LabelProvider.rub*");
		fileList.append(rulesDir + File.separator + "category.rub*");
		fileList.append(rulesDir + File.separator + "topQuery.rub*");
		fileList.append(rulesDir + File.separator + "features" + File.separator + "initfile.rub*");
		fileList.append(rulesDir + File.separator + "fact_check.rub");
		// fileList.append(rulesDir + File.separator + "features" + File.separator + "applyannotations.rub");
		for (Object ruleFile : getOptionalRuleExtensions()) {
			fileList.append("*" + (String)ruleFile);
		}
		store.setDefault(JQueryTyrubaPreferencePage.P_RULES_FILES, fileList.toString());
	}

	public static File getInstallPath() {
		try {
			return new File(FileLocator.resolve(getDefault().getBundle().getEntry("/")).getPath());
		} catch (IOException e) {
			traceUI("Error resolving install path: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static JQueryBackendPlugin getDefault() {
		return plugin;
	}

	/**
	 * @return Filename of the global {@code user.rub} include file 
	 */
	public static String getGlobalUserIncludeFile () {
		return getDefault().getStateLocation().toString().concat("/user.rub");
	}
	
	//	private static String lastMessage;

	public static void error(String msg, Throwable e) {
		e.printStackTrace(System.err);
		error(msg + "\n" + e.getMessage());
	}

	private static long lastErrorTime = 0;
	private static long errorSuppressTime = 1000*60*1; 
	   // After each error dialog, suppress error messages for 1 minute.
	
	/**
	 * Displays an error message to the user.
	 */
	public static void error(Object _msg) {
		if (_msg != null) {
			final String msg = _msg.toString();
			//			if ((lastMessage != null) && lastMessage.startsWith(msg.substring(0, Math.min(10, msg.length()))))
			//				return;
			//			lastMessage = msg;
			System.err.println(msg);
			
			long errorTime = System.currentTimeMillis();
			if (errorTime-lastErrorTime<errorSuppressTime) 
				return;
			lastErrorTime = errorTime;

			// force this message to be displayed in the UI thread
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(getShell(), PLUGIN_NAME, msg);
				}
			});
		}
	}

	/**
	 * Displays an information message to the user.
	 * 
	 * @author wannop
	 */
	public static void message(final Object msg) {
		if (msg != null) {
			// force this message to be displayed in the UI thread
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(getShell(), PLUGIN_NAME, msg.toString());
				}
			});
		}
	}

	/**
	 * Outputs message to System.out if isDebuggingUI is true.
	 */
	public static void traceUI(Object message) {
		trace(isDebuggingUI, message);
	}

	/**
	 * Outputs message to System.out if isDebuggingQueries is true.
	 */
	public static void traceQueries(Object message) {
		trace(isDebuggingQueries, message);
	}

	private static void trace(boolean traceFlag, Object message) {
		if (traceFlag || message instanceof Throwable) {
			if (message == null) {
				System.out.println("[" + PLUGIN_NAME + "] null");
			} else {
				System.out.println("[" + PLUGIN_NAME + "] " + getMessage(message));
			}
		}
	}

	private static String getMessage(Object message) {
		if (message instanceof Throwable) {
			ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(byte_out);
			((Throwable) message).printStackTrace(out);
			out.close();
			return byte_out.toString();
		} else {
			return message.toString();
		}
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public static Shell getShell() {
		if (shell == null) {
			shell = getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		return shell;
	}

	public void doneSaving(ISaveContext context) {
		JQueryBackendPlugin.traceUI("JQueryPlugin: Done saving state..............................");
		// delete the old saved state since it is not necessary anymore
		String oldFileName = "save-" + Integer.toString(context.getPreviousSaveNumber());
		File f = plugin.getStateLocation().append(oldFileName).toFile();
		f.delete();
	}

	public void prepareToSave(ISaveContext context) {
		JQueryBackendPlugin.traceUI("JQueryPlugin: Prepare to save state............................");
	}

	public void rollback(ISaveContext context) {
		JQueryBackendPlugin.traceUI("JQueryPlugin: Rollback to last saved state...............................");

		// since the save operation has failed, delete the saved state we have just written
		int saveNumber = context.getSaveNumber();
		String saveFileName = "save-" + Integer.toString(saveNumber);
		File f = plugin.getStateLocation().append(saveFileName).toFile();
		f.delete();
	}

	public void saving(ISaveContext context) {
		JQueryBackendPlugin.traceUI("JQueryPlugin: Saving state...............................");

		// example code
		String saveFileName = "save-" + Integer.toString(context.getSaveNumber());
		File f = getStateLocation().append(saveFileName).toFile();
		writeImportantState(f);

		context.map(new Path("save"), new Path(saveFileName));
		context.needSaveNumber();
	}

	private void writeImportantState(File f) {
		try {
			if (f.exists()) {
				f.delete();
			}
			DataOutputStream p = new DataOutputStream(new FileOutputStream(f));
			if (JQueryAPI.getFactBase().getName() != null) {
				p.writeUTF(JQueryAPI.getFactBase().getName());
			}
		} catch (IOException e) {
			System.err.println("Error saving JQuery State: " + e);
		}
	}

	private void readImportantState(File f) {
		if (!f.exists()) {
			return;
		}

		try {
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			if (in.available() > 0) {
				String t = in.readUTF();
				JQueryAPI.getFactBase().setFactBaseByName(t);
			}
		} catch (JQueryException e) {
			System.err.println("Error restoring JQuery State: " + e);
		} catch (IOException e) {
			System.err.println("Error restoring JQuery State: " + e);
		}
	}

	/**
	 * Returns the ImageDescriptor of the file at the given location relative to the plugin's icon directory.
	 */
	public static ImageDescriptor getImageDescriptor(String name) {

		try {
			URL installURL = plugin.getBundle().getEntry("/");
			URL url = new URL(installURL, iconPath + name);
			ImageDescriptor x = ImageDescriptor.createFromURL(url);
			return x;
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	public void initializeImageRegistry(ImageRegistry imgReg) {
		// Load images
		imgReg.put("String", getImageDescriptor("String.gif"));
		imgReg.put("UpArrow", getImageDescriptor("HideTopSash_UpArrow.gif"));
		imgReg.put("DownArrow", getImageDescriptor("HideTopSash_DownArrow.gif"));
		imgReg.put("Close", getImageDescriptor("Close.gif"));
		imgReg.put("Execute", getImageDescriptor("Execute.gif"));

		imgReg.put("Query", getImageDescriptor("QueryView.gif"));

		imgReg.put("Error", getImageDescriptor("Error.gif"));
		imgReg.put("Warning", getImageDescriptor("Warning.gif"));
		imgReg.put("Task", getImageDescriptor("Task.gif"));
		imgReg.put("Bookmark", getImageDescriptor("Bookmark.gif"));
		imgReg.put("Info", getImageDescriptor("Info.gif"));
		imgReg.put("Vsf", getImageDescriptor("Vsf.gif"));
		imgReg.put("TVar", getImageDescriptor("TVar.gif"));
	}

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		try {
			super.start(context);
			JQueryBackendPlugin.traceUI("JQueryBackendPlugin: Starting up...............................");

			PLUGIN_VERSION = "" + context.getBundle().getVersion();

			checkSaveDataVersion();

			// Set tracing flags
			if (isDebugging()) {
				String option;

				option = Platform.getDebugOption("ca.ubc.jquery/debug/ui");
				if (option != null) {
					isDebuggingUI = option.equals("true");
					traceUI("UI tracing started.");
				}

				option = Platform.getDebugOption("ca.ubc.jquery/debug/queries");
				if (option != null) {
					isDebuggingQueries = option.equals("true");
					traceQueries("Query tracing started.");
				}
			}

			// Get install directory
			File installPath = null;
			try {
				installPath = new File(FileLocator.resolve(getBundle().getEntry("/")).getPath());
				PropertyConfigurator.configure(installPath + "/lib/log4j.properties");
			} catch (IOException e) {
				traceUI("Error resolving install path: " + e.getMessage());
			}

			// search for extensions to the API
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			tracker = new ExtensionTracker(reg);

			IExtensionPoint ep1 = reg.getExtensionPoint("ca.ubc.jquery.backend", FACTBASE_EXTENSION);
			IFilter filter = ExtensionTracker.createExtensionPointFilter(ep1);
			factbaseExtensionHandler = new FactbaseExtensionHandler(ep1, tracker);
			tracker.registerHandler(factbaseExtensionHandler, filter);

			IExtensionPoint ep2 = reg.getExtensionPoint("ca.ubc.jquery.backend", RESOURCE_EXTENSION);
			filter = ExtensionTracker.createExtensionPointFilter(ep2);
			resourceExtensionHandler = new ResourceExtensionHandler(ep2, tracker);
			tracker.registerHandler(resourceExtensionHandler, filter);

			IExtensionPoint ep3 = reg.getExtensionPoint("ca.ubc.jquery.backend", INCLUDERULES_EXTENSION);
			filter = ExtensionTracker.createExtensionPointFilter(ep3);
			includeRulesHandler = new IncludeRulesExtensionHandler(ep3, tracker);
			tracker.registerHandler(includeRulesHandler, filter);

			IExtensionPoint ep4 = reg.getExtensionPoint("ca.ubc.jquery.backend", JAVAVISITOR_EXTENSION);
			filter = ExtensionTracker.createExtensionPointFilter(ep4);
			javaVisitorHandler = new JavaVisitorExtensionHandler(ep4, tracker);
			tracker.registerHandler(javaVisitorHandler, filter);

			IExtensionPoint ep5 = reg.getExtensionPoint("ca.ubc.jquery.backend", PREDICATES_EXTENSION);
			filter = ExtensionTracker.createExtensionPointFilter(ep5);
			predicatesHandler = new PredicatesExtensionHandler(ep5, tracker);
			tracker.registerHandler(predicatesHandler, filter);
			
			// get the preferences from the store
			updatePreferences(true);

			ISavedState state = ResourcesPlugin.getWorkspace().addSaveParticipant(this, this);
			if (state != null) {
				//TODO: This state is null when the plugin is activated for the 
				//very first time. You should handle this case, AND make sure that there is a test 
				//that explicitly test the "first launch" as well as "restore state" scenarios.
				IPath p = state.lookup(new Path("save"));
				if (p != null) {
					File f = getStateLocation().append(p.toString()).toFile();
					readImportantState(f);
				}
			} else {
				// Load TyRuBa backend as default API
				activateAPI("Tyruba backend");
			}
		} catch (Exception e) {
			// TODO: Right now, if for whatever reason the backend doesn't
			//properly start everything goes to hell without any sort of clue
			//what's going on. Should do something (here ?) to deal with that more
			//gracefully.
			e.printStackTrace();
			JQueryBackendPlugin.error("Problems starting the backend", e);
		}
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (tracker != null) {
			tracker.close();
			tracker = null;
		}

		JQueryBackendPlugin.traceUI("JQueryPlugin: Shutting down...............................");
		JQueryAPI.shutdown();
		savePluginPreferences();
	}

	// WARNING: calling this method deletes EVERYTHING in JQuery's metadata directory if
	// the saved data is from a version older than that listed in the version.txt. This
	// version does not necessarily indicate the installed version, but instead should contain
	// the oldest version whose metadata is still compatible with this version. use with caution!!
	private void checkSaveDataVersion() {
		boolean shouldClearMetaData = false;

		File stateLoc = JQueryBackendPlugin.getDefault().getStateLocation().toFile();
		File saveDataVersion = new File(stateLoc, "version.txt");

		try {
			if (!saveDataVersion.exists()) {
				shouldClearMetaData = true;
			} else {

				LineNumberReader versionReader = new LineNumberReader(new FileReader(saveDataVersion));

				String version = versionReader.readLine().trim();

				// this could be changed to support a range of versions
				if (!version.equals(PLUGIN_VERSION)) {
					shouldClearMetaData = true;
				}
				versionReader.close();
			}

			if (shouldClearMetaData) {
				// clear any existing contents from stateLocation
				File[] stateLocContents = stateLoc.listFiles();
				for (int i = 0; i < stateLocContents.length; i++) {
					File file = stateLocContents[i];
					Util.deleteDir(file);
				}

				// clear preferences back to defaults
				IPreferenceStore store = getPreferenceStore();
				initDefaultPreferences(store);
				JQueryTyrubaPreferencePage.forceDefaults(store);
				JQueryBackendPreferences.forceDefaults(store);

				// write current version
				saveDataVersion.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(saveDataVersion));
				writer.write(PLUGIN_VERSION.toCharArray());
				writer.close();
			}
		} catch (FileNotFoundException e) {
			JQueryBackendPlugin.traceUI("JQueryPlugin.checkSaveDataVersion(): FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			JQueryBackendPlugin.traceUI("JQueryPlugin.checkSaveDataVersion(): IOException: " + e.getMessage());
		}
	}

	public static boolean isLoggingQueries() {
		return isLoggingQueries;
	}

	public static boolean detailedClassFiles() {
		return detailedClassFiles;
	}

	public static boolean parseDependenciesEnabled() {
		return parseDependencies;
	}

	public static int getMaxResults() {
		return maxResults;
	}
}
