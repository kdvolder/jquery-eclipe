package ca.ubc.jquery.resources.textfiles;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;

public class TextFileParser extends JQueryResourceParser {

	private IFile res;

	public TextFileParser(IAdaptable resource, JQueryResourceManager manager) {
		super(manager);
		res = (IFile) resource.getAdapter(IFile.class);
	}

	public String getName() {
		return res.getProjectRelativePath().toString();
	}

	public void initialize(JQueryFactGenerator generator) {
		setGenerator(generator);
	}

	public void parse() {
		try {
			System.out.println("parse text: "+res.getFullPath());
			IPath fullPath = res.getFullPath();
			IPath folderPath = fullPath.removeLastSegments(1);
			String fullPathStr = fullPath.toString();
			getGenerator().insert("rubFile", new Object[] { fullPathStr });

			getGenerator().insert("rubFolder", new Object[] { folderPath.toString(), fullPathStr });
			getGenerator().insertElementLocation(fullPathStr, fullPathStr, 0, 0);
		} catch (JQueryException e) {
			System.err.println(e);
		}
	}
}
