package ca.ubc.jquery.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class JQWordDetector implements IWordDetector {
	private char[] start;

	private char[] end;

	public JQWordDetector(String wordStart, String wordEnd) {
		start = new char[wordStart.length()];
		for (int i = 0; i < wordStart.length(); i++) {
			start[i] = wordStart.charAt(i);
		}

		end = new char[wordEnd.length()];
		for (int i = 0; i < wordEnd.length(); i++) {
			end[i] = wordEnd.charAt(i);
		}
	}

	public boolean isWordStart(char c) {
		boolean result = false;

		for (int i = 0; i < start.length && !result; i++) {
			result = start[i] == c;
		}

		return result;
	}

	public boolean isWordPart(char c) {
		boolean result = false;

		for (int i = 0; i < end.length && !result; i++) {
			result = end[i] == c;
		}

		return !result;
	}
}
