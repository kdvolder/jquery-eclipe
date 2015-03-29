package ca.ubc.jquery.resource.xml;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.xml.sax.SAXException;

import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;

public class JQXMLParser extends JQueryResourceParser {
	// Most of this code taken from here: http://labe.felk.cvut.cz/~xfaigl/mep/xml/java-xml.htm

	private IFile xmlFile;

	public JQXMLParser(IAdaptable file, JQueryResourceManager manager) {
		super(manager);
		xmlFile = (IFile) file;
	}

	@Override
	public String getName() {
		try {
			return xmlFile.getLocationURI().toURL().toString();
		} catch (MalformedURLException e) {
			return xmlFile.getName();
		}
	}

	@Override
	public void initialize(JQueryFactGenerator generator) {
		setGenerator(generator);
	}

	@Override
	public void parse() {
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			//parse the file and also register this class for call backs
			sp.parse(getName(), new JQuerySAXHandler(xmlFile, getGenerator()));
			System.out.println("[DEBUG] parsed: " + xmlFile.getFullPath());
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
}
