package ca.ubc.jquery.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import ca.ubc.jquery.api.JQueryFileElement;

public class Util {

	/** 
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	public static String getSource(JQueryFileElement loc) throws JavaModelException, IOException {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(loc.locationID);

		if (cu != null) {
			if (!cu.isConsistent()) {
				throw new IOException(cu.getElementName() + " is not consistent with its underlying resource.");
			}

			StringBuffer cuSource = new StringBuffer(cu.getSource());
			return cuSource.substring(loc.start, loc.start + loc.length).toString();
		} else {
			throw new IOException("This method should not be called on non-java source locations");
		}
	}

	public static void replaceSource(JQueryFileElement loc, String newSource) throws IOException, CoreException {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(loc.locationID);

		if (cu != null) {
			if (!cu.isConsistent()) {
				throw new IOException(cu.getElementName() + " is not consistent with its underlying resource.");
			}

			if (cu.isOpen()) {
				IBuffer buffer = cu.getBuffer();
				if (buffer.hasUnsavedChanges()) {
					throw new IOException(cu.getElementName() + " has unsaved changes.");
				}
				buffer.replace(loc.start, loc.length, newSource);
				buffer.save(null, false);
			} else {
				StringBuffer cuSource = new StringBuffer(cu.getSource());

				cuSource.replace(loc.start, loc.start + loc.length, newSource);

				IPath path = cu.getPath();
				IFile f = cu.getResource().getWorkspace().getRoot().getFile(path);
				InputStream is = new ByteArrayInputStream(cuSource.toString().getBytes());
				//				InputStream is = new StringBufferInputStream(cuSource.toString());
				f.setContents(is, true, true, null);
			}
		} else {
			throw new IOException("This method should not be called on non-java source locations");
		}
	}

}
