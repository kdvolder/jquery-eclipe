package ca.ubc.jquery.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * The document provide.  This has some special file handling methods wich allows
 * the user to acces the fileName (for querying) and the file Extension to help 
 * provide the appropriate JQuery editor configuration.
 * 
 * @author lmarkle
 *
 */
public class JQDocumentProvider extends FileDocumentProvider {

	private String extension;

	private String fileName;

	private String editorName;

	private String[] partitionTypes;

	private TokenManager tokenManager;

	public JQDocumentProvider(TokenManager manager) {
		tokenManager = manager;
		partitionTypes = new String[] { JQPartitionScanner.JQ_DEFAULT };
		editorName = null;
	}

	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof FileEditorInput) {
			FileEditorInput input = (FileEditorInput) element;
			extension = input.getPath().getFileExtension();
			fileName = "/" + input.getToolTipText();
		} else {
			extension = "null";
			fileName = "null";
		}

		IDocument document = super.createDocument(element);
		if (document != null) {
			JQPartitionScanner ps = new JQPartitionScanner(this, tokenManager);
			partitionTypes = ps.getPartitionTypes();
			IDocumentPartitioner partitioner = new FastPartitioner(ps, partitionTypes);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	public String[] getParitionTypes() {
		return partitionTypes;
	}

	public String getFileName() {
		return fileName;
	}

	public String getJQEditor() {
		if (editorName == null) {
			editorName = JQueryAPI.getStringProperty(extension, "JQE_editor");
		}

		return editorName;
	}
}
