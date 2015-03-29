package ca.ubc.jquery.resource.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactGenerator;

public class JQuerySAXHandler extends DefaultHandler {
	private StringBuffer text;

	private Stack context = new Stack();

	private IFile file;

	private JQueryFactGenerator generator;

	private BufferedReader bufRead;

	private String currentLine;

	private int byteCount;

	private int byteOffset;

	private String uniqueString;

	public JQuerySAXHandler(IFile file, JQueryFactGenerator generator) {
		this.file = file;
		this.generator = generator;

		try {
			byteCount = 0;
			byteOffset = 0;
			currentLine = "";
			bufRead = new BufferedReader(new FileReader(new File(file.getLocationURI())));
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
	}

	public void startDocument() throws SAXException {
		try {
			uniqueString = "file" + file.getFullPath();
			int id = getUniqueID(uniqueString);
			generator.insert("xmlFile", new Object[] { id, file.getName() });
			generator.insertElementLocation(id, file.getFullPath().toString(), 0, 0);
			context.push(new int[] { id, 0 });
		} catch (JQueryException e) {
			throw new SAXException("JQueryError during parse: ", e);
		}
	}

	public void endDocument() throws SAXException {
	}

	//Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		echoText();
		if (!("").equals(localName)) {
			qName = localName;
		}

		try {
			int parent = ((int[]) context.peek())[0];
			int size = ((int[]) context.peek())[1];
			String nodeName = qName;

			uniqueString = uniqueString + "/[" + size + "]";
			String original = uniqueString;
			int p = insertNode(parent, nodeName);

			for (int i = 0; i < attributes.getLength(); i++) {
				String aName = attributes.getLocalName(i);
				if (!("").equals(attributes.getQName(i))) {
					aName = attributes.getQName(i);
				}

				String aVal = attributes.getValue(i);
				uniqueString = original + "/a[" + i + "]";
				insertAttribute(p, aName, aVal);
			}
			uniqueString = original;

			context.push(new int[] { p, size });
		} catch (JQueryException e) {
			throw new SAXException("JQueryError during parse: ", e);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		echoText();
		if (!("").equals(localName)) {
			qName = localName;
		}

		uniqueString = uniqueString.substring(0, uniqueString.lastIndexOf("/"));
		context.pop();
		int[] x = (int[]) context.pop();
		context.push(new int[] { x[0], x[1] + 1 });
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = new String(ch, start, length);
		if (text == null) {
			text = new StringBuffer(s);
		} else {
			text.append(s);
		}
	}

	private void echoText() throws SAXException {
		//					if (text == null)
		//						return;
		//					String s = "" + text;
		text = null;
	}

	/*
	 * 
	 * Fact Generating Portion 
	 * 
	 * 
	 */
	protected int getUniqueID(Object id) throws JQueryException {
		return id.hashCode();
	}

	private void insertXChild(int p, int c) throws JQueryException {
		generator.insert("xchild", new Object[] { p, c });
	}

	private int insertNode(int parent, String name) throws JQueryException {
		int n = getUniqueID(uniqueString);

		insertXChild(parent, n);
		generator.insert("xmlNode", new Object[] { n, name });
		insertSourceLocation(n, name);

		return n;
	}

	private int insertAttribute(int parent, String name, String value) throws JQueryException {
		int n = getUniqueID(uniqueString);
		int v = getUniqueID(uniqueString + "/v");

		insertXChild(parent, n);
		generator.insert("xmlAttribute", new Object[] { n, name, v });
		insertSourceLocation(n, name);
		insertValue(n, v, value);

		return n;
	}

	private void insertValue(int parent, int v, String value) throws JQueryException {
		if (("").equals(value)) {
			return;
		}

		insertXChild(parent, v);
		generator.insert("xmlValue", new Object[] { v, value });
		insertSourceLocation(v, value);
	}

	/** 
	 * Hack at best.
	 */
	private void insertSourceLocation(int id, String match) throws JQueryException {
		// Search the file until we get the match string.  Insert a location at this match.
		//
		//
		String other = "/" + match;
		while (currentLine.indexOf(match, byteOffset) < 0) {// || currentLine.indexOf(other, byteOffset) >= 0) {
			try {
				byteOffset = 0;
				byteCount += currentLine.length() + 2;
				currentLine = bufRead.readLine();
			} catch (IOException e) {
				System.err.println(e);
			}
		}

		int pos = currentLine.indexOf(match, byteOffset) - 2;
		generator.insertElementLocation(id, file.getFullPath().toString(), byteCount + pos, match.length());
		byteOffset = ((pos < 0) ? 0 : pos) + match.length();
	}
}
