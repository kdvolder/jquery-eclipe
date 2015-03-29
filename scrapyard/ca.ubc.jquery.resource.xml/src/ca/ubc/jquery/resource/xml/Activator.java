package ca.ubc.jquery.resource.xml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {

	/** The shared instance. */
	private static AbstractUIPlugin plugin = null;

	public Activator() {
		plugin = this;
	}

	/**
	 * Returns the ImageDescriptor of the file at the given location relative to the plugin's icon directory.
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		try {
			URL installURL = plugin.getBundle().getEntry("/");
			URL url = new URL(installURL, "icons" + File.separatorChar + name);

			File p = new File(FileLocator.resolve(plugin.getBundle().getEntry("/")).getPath());
			p = new File(p.toString() + File.separatorChar + "icons" + File.separatorChar + name);

			if (p.exists()) {
				ImageDescriptor x = ImageDescriptor.createFromURL(url);
				return x;
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			// should not happen
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
