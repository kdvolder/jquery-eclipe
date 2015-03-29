package ca.ubc.jquery.editor;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * The Main Eclipse JQuery Editor Extension Class
 * @author lmarkle
 *
 */
public class JQueryEditor extends TextEditor {

	private TokenManager tokenManager;

	public JQueryEditor() {
		super();

		tokenManager = new TokenManager();
		JQDocumentProvider provider = new JQDocumentProvider(tokenManager);
		tokenManager.setDocumentProvider(provider);
		JQConfiguration configuration = new JQConfiguration(provider, tokenManager);

		setDocumentProvider(provider);
		setSourceViewerConfiguration(configuration);
	}

	public void dispose() {
		tokenManager.dispose();
		super.dispose();
	}
}
