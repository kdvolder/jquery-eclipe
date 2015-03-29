package ca.ubc.jquery.gluelog;

//import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;

/**
 * The main class for the JQuery plugin. This class takes care of the following tasks:
 * <ul>
 * <li> Maintains some useful information such as the plugin install directory and the current tracing options.
 * <li> Coordinates the interaction between views. Each view must register itself when it opens and deregister itself when it closes. Communication between views is handled using event listeners. This class updates the appropriate listeners
 * when a view opens or closes.
 * </ul>
 */
public class JQueryGlueLogBackendPlugin extends AbstractUIPlugin implements ISaveParticipant {

	/** The name of the plugin */
	public static final String PLUGIN_NAME = "JQuery Backend";

	/** The version of the plugin */
	public static String PLUGIN_VERSION = null;

	private static final String SAVE_FILE_NAME = "current.workingset";

	/** Path to icons folder relative to installPath */
	private static final String iconPath = "icons/";

	/** The shared instance. */
	private static JQueryGlueLogBackendPlugin plugin = null;

	/** The plugin's shell. */
	private static Shell shell = null;

	public JQueryGlueLogBackendPlugin() {
		super();

		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static JQueryGlueLogBackendPlugin getDefault() {
		return plugin;
	}

	private static File getInstallPath() {
		try {
			File installPath = new File(FileLocator.resolve(JQueryGlueLogBackendPlugin.getDefault().getBundle().getEntry("/")).getPath());
			return installPath;
		} catch (IOException e) {
			error("Error resolving install path: ", e);
			return null;
		}
	}

	/**
	 * Displays an error message to the user.
	 */
	public static void error(Object _msg) {
		if (_msg != null) {
			final String msg = _msg.toString();
			if ((lastMessage != null) && lastMessage.startsWith(msg.substring(0, Math.min(10, msg.length()))))
				return;
			lastMessage = msg;
			System.err.println(msg);

			// force this to display in the UI thread... prevents invalid thread access
			// on errors that may get thrown outside the UI thread.
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
			// force this to display in the UI thread... prevents invalid thread access
			// on errors that may get thrown outside the UI thread.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(getShell(), PLUGIN_NAME, msg.toString());
				}
			});
		}
	}


	private static String lastMessage;

	public static void error(String msg, Throwable e) {
		e.printStackTrace(System.err);
		error(msg + e.getMessage());
	}

	public void doneSaving(ISaveContext context) {
		// delete the old saved state since it is not necessary anymore
		String oldFileName = "save-" + Integer.toString(context.getPreviousSaveNumber());
		File f = plugin.getStateLocation().append(oldFileName).toFile();
		f.delete();
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

	public void prepareToSave(ISaveContext context) {
	}

	public void rollback(ISaveContext context) {
	}

	public void saving(ISaveContext context) {
		// example code
		String saveFileName = "save-" + Integer.toString(context.getSaveNumber());
		File f = getStateLocation().append(SAVE_FILE_NAME).toFile();
		writeImportantState(f);

		context.map(new Path("save"), new Path(saveFileName));
		context.needSaveNumber();
	}

	private void writeImportantState(File f) {
		try {
			if (f.exists())
				f.delete();
			DataOutputStream p = new DataOutputStream(new FileOutputStream(f));
			p.writeUTF(JQueryAPI.getFactBase().getName());
		} catch (IOException e) {
			System.err.println("Error saving JQuery State: " + e);
		}
	}

	private void readImportantState(File f) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			String t = in.readUTF();
			JQueryAPI.getFactBase().setFactBaseByName(t);
		} catch (JQueryException e) {
			System.err.println("Error restoring JQuery State: " + e);
		} catch (IOException e) {
			System.err.println("Error restoring JQuery State: " + e);
		}
	}

	private void updatePreferences() {
		IPreferenceStore store = getPreferenceStore();

//		store.setDefault(JTransformerPreferencePage.P_SELECTED_FACTBASE, "EMPTY");
//		store.setDefault(JTransformerPreferencePage.P_RULES_LOCATION, getInstallPath() + File.separator + "rules.jt" + File.separator + "jquery.pl");
	}

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		PLUGIN_VERSION = "" + context.getBundle().getLastModified();

		updatePreferences();

		File f = getStateLocation().append(SAVE_FILE_NAME).toFile();
		if (f.exists()) {
			readImportantState(f);
		}
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		savePluginPreferences();
	}

	public static Shell getShell() {
		if (shell == null) {
			shell = getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		return shell;
	}
}
