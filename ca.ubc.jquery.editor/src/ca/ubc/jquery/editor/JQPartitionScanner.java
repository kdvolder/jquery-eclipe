package ca.ubc.jquery.editor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * Privdes the partitions for the given JQuery Document.
 * 
 * @author lmarkle
 *
 */
public class JQPartitionScanner extends RuleBasedPartitionScanner {
	public final static String JQ_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;

	private Set types;

	private int location;

	public JQPartitionScanner(JQDocumentProvider provider, TokenManager manager) {

		types = new HashSet();
		// always handle default content type
		types.add(JQ_DEFAULT);

		location = 0;

		setPredicateRules(JQDocumentScanner.jqinitializeRules(provider, JQ_DEFAULT, manager));
		setupTypes(provider.getJQEditor());
	}

	public String[] getPartitionTypes() {
		return (String[]) types.toArray(new String[types.size()]);
	}

	private void setupTypes(String editor) {
		JQueryResultSet rs = null;

		if (editor == null) {
			return;
		}

		try {
			JQuery q = JQueryAPI.createQuery("JQE_syntaxRule(!editor,?label,?,?,?)");
			q.bind("!editor", editor);
			rs = q.execute();
			for (int i = 0; rs.hasNext(); i++) {
				String type = (String) rs.next().get("?label");
				types.add(type);
			}

			q = JQueryAPI.createQuery("JQE_syntaxSourceLocationRule(!editor,?,?label,?,?)");
			q.bind("!editor", editor);
			rs = q.execute();
			for (int i = 0; rs.hasNext(); i++) {
				String type = (String) rs.next().get("?label");
				types.add(type);
			}
		} catch (JQueryException ex) {
			// FIXME do something
			ex.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	public int getLocation() {
		return location;
	}

	@Override
	public int read() {
		++location;
		return super.read();
	}

	@Override
	public void unread() {
		--location;
		super.unread();
	}
}
