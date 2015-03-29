package ca.ubc.jquery.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

public class JQDocumentScanner extends BufferedRuleBasedScanner {
	private static class SyntaxElement {
		public String start;

		public String end;

		public String label;

		public String type;

		public SyntaxElement(JQueryResult result) throws JQueryException {
			start = (String) result.get("?st");
			end = (String) result.get("?en");
			label = (String) result.get("?label");
			type = (String) result.get("?type");
		}
	}

	public JQDocumentScanner(JQDocumentProvider provider, String defaultToken, TokenManager tokens) {
		setRules(jqinitializeRules(provider, defaultToken, tokens, true));
	}

	public static IPredicateRule[] jqinitializeRules(JQDocumentProvider provider, String defaulToken, TokenManager manager) {
		return jqinitializeRules(provider, defaulToken, manager, false);
	}

	private static IPredicateRule[] jqinitializeRules(JQDocumentProvider provider, String defaultToken, TokenManager manager, boolean bindLabels) {
		JQueryResultSet rs = null;

		String editor = provider.getJQEditor();
		String fileName = provider.getFileName();

		if (editor == null) {
			return new IPredicateRule[0];
		}

		try {
			List rules = new ArrayList();

			if (!bindLabels) {
				createSourceLocationRule(rules, editor, fileName, defaultToken, manager);
			}

			JQuery q = JQueryAPI.createQuery("JQE_syntaxRule(!editor,?label,?type,?st,?en)");
			q.bind("!editor", editor);
			if (bindLabels) {
				q.bind("?label", defaultToken);
			}

			rs = q.execute();
			while (rs.hasNext()) {
				SyntaxElement element = new SyntaxElement(rs.next());
				IPredicateRule rule = createRule(element, defaultToken, manager);

				if (rule != null) {
					rules.add(rule);
				}
			}

			if (bindLabels && defaultToken.equals(JQPartitionScanner.JQ_DEFAULT)) {
				createWordRule(rules, editor, defaultToken, manager);
			} else if (!bindLabels) {
				createWordRule(rules, editor, defaultToken, manager);
			}

			return (IPredicateRule[]) rules.toArray(new IPredicateRule[rules.size()]);
		} catch (JQueryException ex) {
			// FIXME do something
			ex.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return new IPredicateRule[0];
	}

	private static IPredicateRule createRule(SyntaxElement el, String defaultToken, TokenManager manager) {
		if (("pattern-match").equals(el.type)) {
			return createPatternRule(el, manager);
		} else if (("multi-line").equals(el.type)) {
			return createMultiLineRule(el, manager.getToken(el.label));
		} else if (("single-line").equals(el.type)) {
			return createSingleLineRule(el, manager.getToken(el.label));
		} else if (("single-char").equals(el.type)) {
			return createSingleCharRule(el, defaultToken, manager);
		} else {
			System.err.println("Unkown syntax rule type: " + el.type);
			return null;
		}
	}

	private static void createSourceLocationRule(List rules, String editorName, String fileName, String defaultToken, TokenManager manager) throws JQueryException {
		JQueryResultSet rs = null;

		try {
			IToken dtoken = manager.getToken(defaultToken);

			JQuery q = JQueryAPI.createQuery("JQE_syntaxSourceLocationRule(!editor,!file,?syntax,?start,?len)");
			q.bind("!editor", editorName);
			q.bind("!file", fileName);
			rs = q.execute();

			JQSourceLocationRule rule = new JQSourceLocationRule(dtoken);
			if (rs.hasNext()) {
				rules.add(rule);
			}

			while (rs.hasNext()) {
				JQueryResult r = rs.next();
				int start = ((Integer) r.get("?start")).intValue();
				int length = ((Integer) r.get("?len")).intValue();
				IToken token = manager.getToken((String) r.get("?syntax"));

				rule.addLocation(token, start, length);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	private static void createWordRule(List rules, String editorName, String defaultToken, TokenManager manager) throws JQueryException {
		JQueryResultSet rs = null;

		try {
			String wstart = null;
			String wend = null;

			JQuery q = JQueryAPI.createQuery("JQE_syntaxWordRule(!editor,?start,?end)");
			q.bind("!editor", editorName);
			rs = q.execute();

			if (rs.hasNext()) {
				JQueryResult r = rs.next();
				wstart = (String) r.get("?start");
				// append \r\t\n characters because they may be difficult to escape in a query language
				wend = (String) r.get("?end") + "\r\t\n";

				JQWordDetector detector = new JQWordDetector(wstart, wend);
				JQWordRule result = new JQWordRule(detector, manager);

				q = JQueryAPI.createQuery("JQE_syntaxWord(!editor,?type,?Word)");
				q.bind("!editor", editorName);
				rs = q.execute();

				while (rs.hasNext()) {
					r = rs.next();

					String word = (String) r.get("?Word");
					String type = (String) r.get("?type");

					result.addWord(word, type);
				}
				result.setDefaultToken(defaultToken);

				rules.add(result);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	private static IPredicateRule createPatternRule(SyntaxElement el, TokenManager manager) {
		JQWordDetector detector = new JQWordDetector(el.start, el.end + "\r");
		JQWordRule result = new JQWordRule(detector, manager);
		result.setDefaultToken(el.label);
		return result;
	}

	private static IPredicateRule createSingleLineRule(SyntaxElement el, IToken token) {
		return new SingleLineRule(el.start, el.end, token, '\\', true);
	}

	private static IPredicateRule createMultiLineRule(SyntaxElement el, IToken token) {
		return new MultiLineRule(el.start, el.end, token, '\\', true);
	}

	private static IPredicateRule createSingleCharRule(SyntaxElement el, String defaultToken, TokenManager manager) {
		return new JQCharRule(el.start, el.label, defaultToken, manager);
	}
}
