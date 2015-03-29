package ca.ubc.jquery;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
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

import ca.ubc.jquery.api.JQueryObjectInputStream;
import ca.ubc.jquery.browser.BrowserSelectionListener;
import ca.ubc.jquery.browser.menu.LayeredMenuProviderFactory;
import ca.ubc.jquery.preferences.TreeBrowserPreferences;

/**
 * The main class for the JQuery plugin. This class takes care of the following tasks:
 * <ul>
 * <li> Maintains some useful information such as the plugin install directory and the current tracing options.
 * <li> Coordinates the interaction between views. Each view must register itself when it opens and deregister itself when it closes. Communication between views is handled using event listeners. This class updates the appropriate listeners
 * when a view opens or closes.
 * </ul>
 */
public class JQueryTreeBrowserPlugin extends AbstractUIPlugin implements ISaveParticipant {

	/** The id of the plugin. */
	public static final String PLUGIN_ID = "ca.ubc.jquery.gui.JQueryTreeView";

	/** The name of the plugin */
	public static final String PLUGIN_NAME = "JQuery Browser";

	/** The version of the plugin */
	public static String PLUGIN_VERSION = "JQUERY VERSION 4_0_0";
	
	private static final String MENUPROVIDER_EXTENSION = "menuProvider";
	private static final String SELECTIONLISTENER_EXTENSION = "selectionListener";

	public static int PLUGIN_COUNTER = 0;

	/** The path to the plugin install directory */
	public static File installPath = null;

	/** True if the tracing option ca.ubc.jquery/debug/ui is on. */
	private static boolean isDebuggingUI = true;

	/** True if the tracing option ca.ubc.jquery/debug/queries is on. */
	private static boolean isDebuggingQueries;

	/** True if user's actions are to be logged */
	private static boolean isLoggingUser;

	private static boolean parseDependencies = false;

	// /** Factbase cache size */
	// private static int cacheSize;

	/** Maximum number of results to display for a single JQuery query */
	private static int maxResults;

	/** Indicates whether or not to use auto complete in the query edit dialog. */
	private static boolean enableAutoComplete;

	/** The shared instance. */
	private static JQueryTreeBrowserPlugin plugin = null;

	/** Path to icons folder relative to installPath */
	private static final String iconPath = "icons/";
	
	/** List of filters created */
	private Map filtersList;

	/** Manage extension points */
	private IExtensionTracker tracker;
	
	private MenuProviderExtensionHandler menuProviderExtensionHandler;
	private SelectionListenerExtensionHandler selectionListenerExtensionHandler;
	
	private Map menuProviderExtensions;
	private List<BrowserSelectionListener> browserSelectionListeners;
	
	public JQueryTreeBrowserPlugin() {
		super();
		maxResults = 100000;
		plugin = this;
		filtersList = new HashMap();
		menuProviderExtensions = new HashMap();
		browserSelectionListeners = new ArrayList<BrowserSelectionListener>();
	}

	// WARNING: calling this method deletes EVERYTHING in JQuery's metadata directory if
	// the saved data is from a version older than that listed in the version.txt. This
	// version does not necessarily indicate the installed version, but instead should contain
	// the oldest version whose metadata is still compatible with this version.
	// use with caution!!
	private void checkSaveDataVersion() {
		boolean shouldClearMetaData = false;

		File stateLoc = this.getStateLocation().toFile();
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
					file.delete();
				}

				// write current version
				saveDataVersion.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(saveDataVersion));
				writer.write(PLUGIN_VERSION.toCharArray());
				writer.close();
			}
		} catch (FileNotFoundException e) {
			JQueryTreeBrowserPlugin.traceUI("JQueryPlugin.checkSaveDataVersion(): FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			JQueryTreeBrowserPlugin.traceUI("JQueryPlugin.checkSaveDataVersion(): IOException: " + e.getMessage());
		}
	}

	/**
	 * Returns the ImageDescriptor of the file at the given location relative to the plugin's icon directory.
	 */
	public static ImageDescriptor getImageDescriptor(String name) {

		try {
			URL installURL = plugin.getBundle().getEntry("/");
			URL url = new URL(installURL, iconPath + name);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static JQueryTreeBrowserPlugin getDefault() {
		return plugin;
	}

	/**
	 * Displays an error message to the user.
	 */
	public static void error(Object _msg) {
		if (_msg != null) {
			final String msg = _msg.toString();
			//			if ((lastMessage != null) && lastMessage.startsWith(msg.substring(0, Math.min(10, msg.length()))))
			//				return;
			//			lastMessage = msg;

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
		} else
			return message.toString();
	}

	//	private static String lastMessage;

	public static void error(String msg, Throwable e) {
		// ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
		// e.printStackTrace(new PrintWriter(stackTrace));
		e.printStackTrace(System.err);
		error(msg + e.getMessage());
	}

	public void doneSaving(ISaveContext context) {
		JQueryTreeBrowserPlugin.traceUI("JQueryPlugin: Done saving state..............................");

		// delete the old saved state since it is not necessary anymore
		String oldFileName = "save-" + Integer.toString(context.getPreviousSaveNumber());
		File f = plugin.getStateLocation().append(oldFileName).toFile();
		f.delete();
	}

	public void prepareToSave(ISaveContext context) {
		JQueryTreeBrowserPlugin.traceUI("JQueryPlugin: Prepare to save state............................");
	}

	public void rollback(ISaveContext context) {
		JQueryTreeBrowserPlugin.traceUI("JQueryPlugin: Rollback to last saved state...............................");

		// since the save operation has failed, delete the saved state we have just written
		int saveNumber = context.getSaveNumber();
		String saveFileName = "save-" + Integer.toString(saveNumber);
		File f = plugin.getStateLocation().append(saveFileName).toFile();
		f.delete();
	}

	public void saving(ISaveContext context) {
		JQueryTreeBrowserPlugin.traceUI("JQueryPlugin: Saving state...............................");
		// ruleBaseMapper.saveState();
		context.needDelta(); // need this for workingSetManager
		// example code
		String saveFileName = "save-" + Integer.toString(context.getSaveNumber());
		File f = plugin.getStateLocation().append(saveFileName).toFile();
		plugin.writeImportantState(f);

		context.map(new Path("save"), new Path(saveFileName));
		context.needSaveNumber();
	}

	private void writeImportantState(File f) {
		try {
			if (f.exists()) {
				f.delete();
			}
			ObjectOutputStream p = new ObjectOutputStream(new FileOutputStream(f));
			p.writeInt(JQueryTreeBrowserPlugin.PLUGIN_COUNTER);
			p.writeObject(filtersList);
		} catch (IOException e) {
			System.err.println("Error saving JQuery State: " + e);
		}
	}

	private void readImportantState(File f) {
		try {
			if (f.exists()) {
				JQueryObjectInputStream in = new JQueryObjectInputStream(getClass().getClassLoader(), new FileInputStream(f));
				JQueryTreeBrowserPlugin.PLUGIN_COUNTER = in.readInt();
				filtersList = (Map) in.readObject();
			}
		} catch (IOException e) {
			System.err.println("Error restoring JQuery State: " + e);
		} catch (ClassNotFoundException e) {
			System.err.println("Error restoring JQuery state: " + e);
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
		super.start(context);

		JQueryTreeBrowserPlugin.traceUI("JQueryBrowserPlugin: Starting up...............................");

		PLUGIN_VERSION = "" + context.getBundle().getLastModified();
		checkSaveDataVersion();

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
		}

		// initialize log of user's actions
		if (isLoggingUser) {
			ActionsLog.initLog();
		}

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
		try {
			installPath = new File(FileLocator.resolve(getBundle().getEntry("/")).getPath());
		} catch (IOException e) {
			traceUI("Error resolving install path: " + e.getMessage());
		}
		
		// search for extensions to the API
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		tracker = new ExtensionTracker(reg);

		IExtensionPoint ep1 = reg.getExtensionPoint("ca.ubc.jquery.browser", MENUPROVIDER_EXTENSION);
		IFilter filter = ExtensionTracker.createExtensionPointFilter(ep1);
		menuProviderExtensionHandler = new MenuProviderExtensionHandler(ep1, tracker);
		tracker.registerHandler(menuProviderExtensionHandler, filter);
		
		IExtensionPoint ep2 = reg.getExtensionPoint("ca.ubc.jquery.browser", SELECTIONLISTENER_EXTENSION);
		filter = ExtensionTracker.createExtensionPointFilter(ep2);
		selectionListenerExtensionHandler = new SelectionListenerExtensionHandler(ep2, tracker);
		tracker.registerHandler(selectionListenerExtensionHandler, filter);
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		JQueryTreeBrowserPlugin.traceUI("JQueryPlugin: Shutting down...............................");
		savePluginPreferences();
		ActionsLog.closeLog();
	}

	/**
	 * @return the shell for this plugin
	 * @author lmarkle
	 */
	public static Shell getShell() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	public static Map getFiltersList() {
		return getDefault().filtersList;
	}

	public static boolean parseDependenciesEnabled() {
		return parseDependencies;
	}

	public static int getMaxResults() {
		return maxResults;
	}

	public static boolean useAutoComplete() {
		return enableAutoComplete;
	}

	public void updatePreferences(boolean initializing) {
		IPreferenceStore store = getPreferenceStore();
		if (initializing) {
			initDefaultPreferences(store);
		}

		maxResults = store.getInt(TreeBrowserPreferences.P_MAX_RESULTS);
		enableAutoComplete = store.getBoolean(TreeBrowserPreferences.P_EDITOR_AUTOCOMPLETION);
	}

	private void initDefaultPreferences(IPreferenceStore store) {
		store.setDefault(TreeBrowserPreferences.P_MAX_RESULTS, 100000);
		store.setDefault(TreeBrowserPreferences.P_EDITOR_AUTOCOMPLETION, true);
	}
	
	// Menu provider extension handler
	protected static void installMenuProviderExtension (String className, LayeredMenuProviderFactory menuProvider) {
		getDefault().menuProviderExtensions.put(className, menuProvider);
	}
	
	protected static void removeMenuProviderExtension (String className) {
		getDefault().menuProviderExtensions.remove(className);
	}
	
	public static Collection getMenuProviderExtensions () {
		return Collections.unmodifiableCollection(getDefault().menuProviderExtensions.values());
	}

	// Selection listener extension handler
	protected static void addSelectionListener(BrowserSelectionListener listener) {
		getDefault().browserSelectionListeners.add(listener);
	}

	protected static void removeSelectionListener(BrowserSelectionListener listener) {
		getDefault().browserSelectionListeners.remove(listener);
	}
	
	public static Collection<BrowserSelectionListener> getBrowserSelectionListeners() {
		return Collections.unmodifiableCollection(getDefault().browserSelectionListeners);
	}
}
