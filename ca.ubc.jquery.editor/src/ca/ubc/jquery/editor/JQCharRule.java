package ca.ubc.jquery.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class JQCharRule implements IPredicateRule {

	private char[] match;

	private Map tokens;

	private TokenManager tokenManager;

	private String tokenName;

	private IToken defaultToken;

	public JQCharRule(String chars, String name, String defaultToken, TokenManager manager) {
		match = new char[chars.length()];
		for (int i = 0; i < chars.length(); i++) {
			match[i] = chars.charAt(i);
		}

		this.defaultToken = manager.getToken(defaultToken);
		tokenName = name;
		tokens = new HashMap();
		this.tokenManager = manager;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int ch = scanner.read();

		if (contains((char) ch)) {
			IToken result = (IToken) tokens.get(ch);
			if (result == null) {
				result = tokenManager.getToken(tokenName);
				tokens.put(ch, result);
			}

			return result;
		} else {
			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	private boolean contains(char c) {
		boolean result = false;
		for (int i = 0; i < match.length && !result; i++) {
			result = c == match[i];
		}

		return result;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}

	public IToken getSuccessToken() {
		return defaultToken;
	}
}
