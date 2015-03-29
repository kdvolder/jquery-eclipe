package ca.ubc.jquery.editor;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class JQSourceLocationRule implements IPredicateRule {

	private static class Location implements Comparable {
		public IToken token;

		public int start;

		public int length;

		public Location(IToken token, int start, int length) {
			this.token = token;
			this.start = start;
			this.length = length;
		}

		public int compareTo(Object o) {
			Location x = (Location) o;
			if (x.start > start) {
				return 1;
			} else if (x.start < start) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private IToken defaultToken;

	private Set locations;

	public JQSourceLocationRule(IToken defaultToken) {
		this.defaultToken = defaultToken;
		locations = new TreeSet();
	}

	public void addLocation(IToken token, int start, int length) {
		locations.add(new Location(token, start + 1, length));
	}

	public IToken evaluate(ICharacterScanner scanner) {
		JQPartitionScanner sc = (JQPartitionScanner) scanner;
		scanner.read();

		for (Iterator it = locations.iterator(); it.hasNext();) {
			Location t = (Location) it.next();
			if (t.start == sc.getLocation()) {
				int end = t.start + t.length;
				int ch;
				do {
					ch = scanner.read();
				} while (ch != ICharacterScanner.EOF && sc.getLocation() < end);
				scanner.unread();

				return t.token;
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		// TODO This is not be implement properly...
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
