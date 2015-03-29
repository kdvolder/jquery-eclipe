package ca.ubc.jquery.browser.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResultSet;

// Don't think this is used anywhere. Probably superseded by ca.ubc.jquery.refactoring.AddAnnotationsRefactoring.
@Deprecated
public class ApplyAnnotationAction extends Action {
	private String annotation;

	private Object[] targets;

	private String feature;

	private String target;

	public ApplyAnnotationAction(String label, Object[] targets, String annotation, String feature, String target) {
		this.targets = targets;
		this.annotation = annotation;
		this.feature = feature;
		this.target = target;
	}

	private JQueryFileElement getFirst(Object target) {
		JQueryFileElement result = null;
		JQueryResultSet rs = null;
		try {
			JQuery q = JQueryAPI.createQuery("elementLocation(!this,?E)");
			q.bind("!this", target);
			rs = q.execute();
			if (rs.hasNext()) {
				result = (JQueryFileElement) rs.next().get("?E");
			}
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Obtaining fileElementLocation: " + target, e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	public void run() {
		// sort the elements based on the starting position
		SortedMap s = new TreeMap();
		for (int i = 0; i < targets.length; i++) {
			JQueryFileElement loc = getFirst(targets[i]);
			s.put(loc.start, loc);
		}

		// reverse the list (so we start at the "bottom" of the file)
		JQueryFileElement t[] = new JQueryFileElement[s.size()];
		int i = t.length - 1;
		for (Iterator it = s.entrySet().iterator(); it.hasNext(); i--) {
			JQueryFileElement loc = (JQueryFileElement) ((Map.Entry) it.next()).getValue();
			t[i] = loc;
		}

		// make changes
		for (i = 0; i < t.length; i++) {
			JQueryFileElement loc = t[i];
			IFile file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(loc.locationID));

			try {
				InputStream is = file.getContents();
				byte[] before = new byte[loc.start];
				StringBuffer newFile = new StringBuffer();
				is.read(before, 0, loc.start);
				newFile.append(new String(before));

				byte[] during = new byte[loc.length];
				is.read(during, 0, loc.length);
				addAnnotation(new String(during), newFile);

				byte[] rest = new byte[1024];
				while (is.available() > 0) {
					int count = is.read(rest);
					newFile.append(new String(rest, 0, count));
				}
				is.close();

				file.setContents(new ByteArrayInputStream(newFile.toString().getBytes()), true, false, null);
			} catch (CoreException e) {
				JQueryTreeBrowserPlugin.error("Applying annotation " + annotation, e);
			} catch (IOException e) {
				JQueryTreeBrowserPlugin.error("Applying annotation " + annotation, e);
			}
		}
	}

	private void addAnnotation(String body, StringBuffer newFile) {
		// 3 cases here...
		//
		//	1) no annotation
		//		- simply add @Feature stuff before the body
		//	2) single annotated
		//		- we have to convert the existing thing into an array, and append our new one
		//	3) multi annotated
		//		- simply append our new name into the already existing array
		// 
		// Simple?

		int f = body.indexOf(feature);
		if (f < 0) {
			// case 1
			newFile.append("\n" + feature + "(" + target + "={\"" + annotation + "\"})\n");
			newFile.append(body);
		} else {
			int brace = body.indexOf("{", f);
			newFile.append(body.substring(0, f));
			if (brace > body.indexOf(")", f)) {
				// case 2
				int start = body.indexOf("\"", f);
				int finish = body.indexOf("\"", start + 1);
				String existing = body.substring(start, finish + 1);
				body = body.substring(body.indexOf(")", finish));
				newFile.append(feature + "(" + target + "={" + existing + ",\"" + annotation + "\"}");
				newFile.append(body);
			} else {
				// case 3
				newFile.append(body.substring(f, body.indexOf("}", brace)));
				newFile.append(",\"" + annotation + "\"}");
				body = body.substring(body.indexOf(")", brace));
				newFile.append(body);
			}
		}
	}
}
