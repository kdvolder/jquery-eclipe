package ca.ubc.jquery.editor;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;

public class JQToken implements IToken {

	private TextAttribute ta;

	private Object data;

	public JQToken(Object data, TextAttribute attrib) {
		this.data = data;
		ta = attrib;
	}

	public Object getData() {
		return data;
	}

	public TextAttribute getTextAttribute() {
		return ta;
	}

	public boolean isEOF() {
		return false;
	}

	public boolean isOther() {
		return true;
	}

	public boolean isUndefined() {
		return false;
	}

	public boolean isWhitespace() {
		return false;
	}

	public String toString() {
		return "JQToken(" + data + ")";
	}
}
