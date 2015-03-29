package ca.ubc.jquery.resources.textfiles;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

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
			getGenerator().insert("rubFile", new Object[] { res.getName() });

			String folder = res.getProjectRelativePath().toString();
			folder = folder.substring(0, folder.indexOf(res.getName()));
			getGenerator().insert("rubFolder", new Object[] { folder, res.getName() });

			getGenerator().insertElementLocation(res.getName(), res.getFullPath().toString(), 0, 0);
		} catch (JQueryException e) {
			System.err.println(e);
		}
	}
}
