package ca.ubc.jquery.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * Much of this code is lifted from the Eclipse DefaultDamageRepairer class.
 * 
 * Much of the purpose of this class is to associate the text formation with the
 * tokens that are discovered.  Tokens come from the scanner but they can also be entire
 * partitions.
 * 
 * It is important to note that TOKEN MAY NOT OVERLAP!
 * 
 * @author lmarkle
 *
 */
public class JQDamageRepairer implements IPresentationDamager, IPresentationRepairer {
	private IDocument document;

	private TextAttribute defaultText;

	private String token;

	private TokenManager manager;

	private JQDocumentScanner scanner;

	public JQDamageRepairer(String label, TokenManager manager) {
		this.token = label;
		this.manager = manager;
	}

	public JQDamageRepairer(JQDocumentScanner scanner) {
		this.scanner = scanner;
	}

	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		if (documentPartitioningChanged) {
			return partition;
		} else {
			try {
				IRegion info = document.getLineInformationOfOffset(event.getOffset());
				int start = Math.max(partition.getOffset(), info.getOffset());

				int end = event.getOffset() + (event.getText() == null ? event.getLength() : event.getText().length());

				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					// optimize the case of the same line
					end = info.getOffset() + info.getLength();
				} else
					end = endOfLineOf(end);

				end = Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);
			} catch (BadLocationException ex) {
				return partition;
			}
		}
	}

	protected int endOfLineOf(int offset) throws BadLocationException {
		IRegion info = document.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength()) {
			return info.getOffset() + info.getLength();
		}

		int line = document.getLineOfOffset(offset);
		try {
			info = document.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException x) {
			return document.getLength();
		}
	}

	public void setDocument(IDocument document) {
		this.document = document;
	}

	public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
		if (scanner == null) {
			IToken t = manager.getToken(token);
			addRange(presentation, damage.getOffset(), damage.getLength(), getTokenTextAttribute(t));
		} else {
			defaultCreatePresentation(presentation, damage);
		}
	}

	public void defaultCreatePresentation(TextPresentation presentation, ITypedRegion region) {
		int lastStart = region.getOffset();
		int length = 0;
		boolean firstToken = true;
		IToken lastToken = Token.UNDEFINED;
		TextAttribute lastAttribute = getTokenTextAttribute(lastToken);

		scanner.setRange(document, lastStart, region.getLength());

		while (true) {
			IToken token = scanner.nextToken();
			if (token.isEOF())
				break;

			TextAttribute attribute = getTokenTextAttribute(token);
			if (lastAttribute != null && lastAttribute.equals(attribute)) {
				length += scanner.getTokenLength();
				firstToken = false;
			} else {
				if (!firstToken)
					addRange(presentation, lastStart, length, lastAttribute);
				firstToken = false;
				lastToken = token;
				lastAttribute = attribute;
				lastStart = scanner.getTokenOffset();
				length = scanner.getTokenLength();
			}
		}

		addRange(presentation, lastStart, length, lastAttribute);
	}

	/**
	 * Returns a text attribute encoded in the given token. If the token's
	 * data is not <code>null</code> and a text attribute it is assumed that
	 * it is the encoded text attribute. It returns the default text attribute
	 * if there is no encoded text attribute found.
	 *
	 * @param token the token whose text attribute is to be determined
	 * @return the token's text attribute
	 */
	protected TextAttribute getTokenTextAttribute(IToken token) {
		if (token instanceof JQToken) {
			return ((JQToken) token).getTextAttribute();
		} else {
			Object data = token.getData();
			if (data instanceof TextAttribute) {
				return (TextAttribute) data;
			} else {
				return defaultText;
			}
		}
	}

	/**
	 * Adds style information to the given text presentation.
	 *
	 * @param presentation the text presentation to be extended
	 * @param offset the offset of the range to be styled
	 * @param length the length of the range to be styled
	 * @param attr the attribute describing the style of the range to be styled
	 */
	protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null) {
			int style = attr.getStyle();
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			StyleRange styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
			styleRange.font = attr.getFont();
			presentation.addStyleRange(styleRange);
		}
	}
}
