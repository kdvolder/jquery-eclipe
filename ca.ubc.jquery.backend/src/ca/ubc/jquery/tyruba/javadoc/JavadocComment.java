/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ca.ubc.jquery.tyruba.javadoc;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import ca.ubc.jquery.api.JQueryFileElement;

/**
 * @author dsjanzen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JavadocComment {

	private static final int PARSING_COMMENT = 1;

	private static final int PARSING_TAG = 2;

	private static Pattern tagRe = null;

	private static Pattern closingCommentRe = null;

	private static String lineSep;

	private static int lineSepLength;

	static {
		lineSep = System.getProperty("line.separator");
		lineSepLength = lineSep.length();

		try {
			tagRe = Pattern.compile("^(\\s*)\\*\\s*@(\\S+)\\s*(.*)");
			closingCommentRe = Pattern.compile("^(.*)\\*\\/.*");
		} catch (PatternSyntaxException ex) {
			// FIXME Should handle this exception
		}
	}

	/** The tags. */
	private List tags = new Vector();

	/** The original comment string. */
	private String comment = lineSep;

	/** Start char in the original source file. */
	private int start;

	/** Length of comment in the original source file. */
	private int length;

	/** The source location for the comment. */
	private JQueryFileElement sourceLocation = null;

	public JavadocComment(JQueryFileElement javadocSourceLocation) throws IOException, JavaModelException {
		setSourceLocation(javadocSourceLocation);
	}

	/**
	 * Paremeter javadocComment must include the starting and ending delimeters 
	 * of the comment. i.e. the string must begin with "/**" and end with "\*\/"
	 */
	public JavadocComment(String javadocComment, int start, int length) throws IOException {

		this.start = start;
		this.length = length;
		parseComment(javadocComment);
	}

	private void setSourceLocation(JQueryFileElement loc) throws JavaModelException, IOException {

		this.sourceLocation = loc;
		this.start = sourceLocation.start;
		this.length = sourceLocation.length;

		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(sourceLocation.locationID);
		String cuSource = cu.getSource();
		String commentSource = cuSource.substring(sourceLocation.start, sourceLocation.start + sourceLocation.length);
		parseComment(commentSource);

	}

	private void parseComment(String javadocComment) throws IOException {

		LineNumberReader lnr = new LineNumberReader(new StringReader(javadocComment));

		int state = PARSING_COMMENT;

		String tag = null;
		StringBuffer value = new StringBuffer();
		StringBuffer commentBuffer = new StringBuffer();

		String line = lnr.readLine();

		// Get rid of the "/*" at the beginning of the comment
		// leaving a "*" to be consistent with the way the rest
		// of the line begin.
		line = line.substring(line.indexOf("/*") + 2);

		//	Iterate through all the lines
		while (line != null) {
			Matcher m = tagRe.matcher(line);

			// For lines that define tags
			if (m.matches()) {

				// Take care of the preceding tag
				if (state == PARSING_TAG) {
					tags.add(new JavadocTag(tag, value.toString()));
					tag = null;
					value = new StringBuffer();
				}

				state = PARSING_TAG;

				//				setLeadingWhitespace(tagRe.getParen(1));
				// FIXME I'm not sure if this regexp pattern is matching properly...
				// old code here...
				//				tag = tagRe.getParen(2);
				//				value.append(stripClosingComment(tagRe.getParen(3)));
				// new code here...
				tag = m.group(2);
				value.append(stripClosingComment(m.group(3)));
			}
			// All other lines just append text to
			// either the comment or the current tag
			else if (state == PARSING_COMMENT) {
				commentBuffer.append(strip(line) + lineSep);
			} else {
				value.append(lineSep + strip(line));
			}
			line = lnr.readLine();
		}

		comment = commentBuffer.toString();

		// Take care of the last tag
		if (state == PARSING_TAG) {
			// remove last linefeed
			String v = value.toString();
			if (v.endsWith(lineSep)) {
				v = v.substring(0, v.length() - lineSepLength);
			}
			tags.add(new JavadocTag(tag, v));
		}
	}

	private String strip(String line) {

		line = stripClosingComment(line);

		int idx = line.indexOf("*");
		return line.substring(idx + 1);
	}

	private String stripClosingComment(String line) {
		Matcher m = closingCommentRe.matcher(line);
		if (m.matches()) {
			// FIXME Not sure if this regexp patter is used properly
			// old code...
			//			line = closingCommentRe.getParen(1);
			// new code ...
			line = m.group(1);

			if (line.trim().length() == 0) {
				return "";
			}
		}
		return line;
	}

	private String getComment() {
		return comment;
	}

	/**
	 * Adds a new javadoc tag to the javadoc comment.  If a tag
	 * with the same name and value already exists this method does nothing.
	 * If a tag with the same name and different value already exists the
	 * value of <code>replace</code> determines what action to take.
	 *
	 * @param replace When true this method will check for an existing tag of the
	 *                same name and replace its value with the new one.
	 *                If <code>replace</code> is false then this method always creates
	 *                a new tag.
	 */
	private boolean addTag(String name, String value, boolean replace) {

		for (Iterator iter = tags.iterator(); iter.hasNext();) {
			JavadocTag tag = (JavadocTag) iter.next();
			if (tag.getName().equals(name)) {
				if (tag.getValue().trim().equals(value)) {
					return false;
				}
				if (replace) {
					tag.setValue(value);
					return true;
				}
			}
		}

		JavadocTag newTag = new JavadocTag(name, value);
		tags.add(newTag);
		return true;
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}

}
