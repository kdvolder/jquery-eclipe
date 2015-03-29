package ca.ubc.jquery.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

public class TokenManager {

	protected Map rgbTable;

	protected Map nameTable;

	private JQDocumentProvider provider;

	public TokenManager() {
		rgbTable = new HashMap();
		nameTable = new HashMap();
	}

	public void setDocumentProvider(JQDocumentProvider provider) {
		this.provider = provider;
	}

	public void dispose() {
		for (Iterator it = rgbTable.values().iterator(); it.hasNext();) {
			((Color) it.next()).dispose();
		}
	}

	public IToken getToken(String name) {
		TextAttribute ta = getTextAttribute(name);
		return new JQToken(name, ta);
	}

	public Color getColor(RGB rgb) {
		Color result = (Color) rgbTable.get(rgb);
		if (result == null) {
			result = new Color(Display.getCurrent(), rgb);
			rgbTable.put(rgb, result);
		}
		return result;
	}

	public Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}

	private TextAttribute getTextAttribute(String name) {
		TextAttribute result = (TextAttribute) nameTable.get(name);
		if (result == null && provider.getJQEditor() != null) {
			// default to black text on white background with no special style
			Color foreground = getColor("JQE_textcolor", name, null);
			Color background = getColor("JQE_textbackcolor", name, null);
			int style = getStyle(name);
			result = new TextAttribute(foreground, background, style);

			nameTable.put(name, result);
		}

		return result;
	}

	private Color getColor(String predicate, String name, Color defaultColor) {
		Color result = defaultColor;
		JQueryResultSet rs = null;

		try {
			JQuery q = JQueryAPI.createQuery(predicate + "(!type,!name,?r,?g,?b)");
			q.bind("!type", provider.getJQEditor());
			q.bind("!name", name);
			rs = q.execute();

			if (rs.hasNext()) {
				JQueryResult re = rs.next();
				int r = ((Integer) re.get("?r")).intValue();
				int g = ((Integer) re.get("?g")).intValue();
				int b = ((Integer) re.get("?b")).intValue();

				result = getColor(r, g, b);
			}
		} catch (JQueryException ex) {
			// FIXME - ignoring exception
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	private int getStyle(String name) {
		int result = SWT.NORMAL;
		JQueryResultSet rs = null;

		try {
			JQuery q = JQueryAPI.createQuery("JQE_textstyle(!type,!name,?style)");
			q.bind("!type", provider.getJQEditor());
			q.bind("!name", name);
			rs = q.execute();

			if (rs.hasNext()) {
				Object[] style = (Object[]) rs.next().get("?style");

				for (int i = 0; i < style.length; i++) {
					if (("italic").equals(style[i])) {
						result |= SWT.ITALIC;
					} else if (("bold").equals(style[i])) {
						result |= SWT.BOLD;
					}
				}
			}
		} catch (JQueryException ex) {
			// FIXME - ignoring exception
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}
}
