package ca.ubc.jquery.editor;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * The Document configuration.  This class maps the damagerepairers with partition
 * types.
 * 
 * @author lmarkle
 *
 */
public class JQConfiguration extends SourceViewerConfiguration {
	private TokenManager tokenManager;

	private JQDocumentProvider provider;

	public JQConfiguration(JQDocumentProvider provider, TokenManager manager) {
		this.tokenManager = manager;
		this.provider = provider;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		String editorType = provider.getJQEditor();
		if (editorType != null) {
			String[] types = provider.getParitionTypes();

			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(JQPartitionScanner.JQ_DEFAULT)) {
					JQDocumentScanner wordScanner = new JQDocumentScanner(provider, types[i], tokenManager);
					JQDamageRepairer dr = new JQDamageRepairer(wordScanner);

					reconciler.setDamager(dr, types[i]);
					reconciler.setRepairer(dr, types[i]);
				} else {
					JQDamageRepairer dr = new JQDamageRepairer(types[i], tokenManager);

					reconciler.setDamager(dr, types[i]);
					reconciler.setRepairer(dr, types[i]);
				}
			}
		}

		return reconciler;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return provider.getParitionTypes();
	}
}
