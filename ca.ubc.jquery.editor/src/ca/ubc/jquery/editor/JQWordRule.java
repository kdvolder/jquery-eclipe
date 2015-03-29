package ca.ubc.jquery.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class JQWordRule implements IPredicateRule {
	/**
	 * Character buffer, mutable <b>or</b> suitable for use as key in hash maps.
	 * @From org.eclipse.jdt.internal.ui.text.CombinedWordRule
	 */
	public static class CharacterBuffer {

		/** Buffer content */
		private char[] fContent;

		/** Buffer content size */
		private int fLength = 0;

		/**
		 * Initialize with the given capacity.
		 *
		 * @param capacity the initial capacity
		 */
		public CharacterBuffer(int capacity) {
			fContent = new char[capacity];
		}

		/**
		 * Empties this buffer.
		 */
		public void clear() {
			fLength = 0;
		}

		/**
		 * Appends the given character to the buffer.
		 *
		 * @param c the character
		 */
		public void append(char c) {
			if (fLength == fContent.length) {
				char[] old = fContent;
				fContent = new char[old.length << 1];
				System.arraycopy(old, 0, fContent, 0, old.length);
			}
			fContent[fLength++] = c;
		}

		/**
		 * Returns the length of the content.
		 *
		 * @return the length
		 */
		public int length() {
			return fLength;
		}

		/**
		 * Returns the content as string.
		 *
		 * @return the content
		 */
		public String toString() {
			return new String(fContent, 0, fLength);
		}
	}

	private JQWordDetector wordDetector;

	private Map words;

	private TokenManager tokenManager;

	private CharacterBuffer buffer;

	private IToken defaultToken;

	public JQWordRule(JQWordDetector word, TokenManager manager) {
		wordDetector = word;
		words = new HashMap();
		buffer = new CharacterBuffer(15);
		this.tokenManager = manager;
		defaultToken = manager.getToken(JQPartitionScanner.JQ_DEFAULT);
	}

	public void addWord(String word, String token) {
		words.put(word, tokenManager.getToken(token));
	}

	public void removeWord(String word) {
		words.remove(word);
	}

	public void setDefaultToken(String name) {
		defaultToken = tokenManager.getToken(name);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int ch = scanner.read();

		if (wordDetector.isWordStart((char) ch)) {
			buffer.clear();
			do {
				buffer.append((char) ch);
				ch = scanner.read();
			} while (ch != ICharacterScanner.EOF && wordDetector.isWordPart((char) ch));
			scanner.unread();

			IToken token = (IToken) words.get(buffer.toString());
			if (token == null) {
				return defaultToken;
			} else {
				return token;
			}
		} else {
			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		// TODO This actually isn't implemented properly...
		if (resume) {
			return defaultToken;
		} else {
			return evaluate(scanner);
		}
	}

	public IToken getSuccessToken() {
		return defaultToken;
	}
}
