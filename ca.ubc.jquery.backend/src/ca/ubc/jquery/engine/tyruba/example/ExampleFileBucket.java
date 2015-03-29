/*
 * Created on Aug 25, 2004
 */
package ca.ubc.jquery.engine.tyruba.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RuleBaseBucket;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.TyrubaException;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryFileElement;

/**
 * Bucket for the Example extension.  See ExampleFileStrategy.java for more information.
 * @author riecken
 */
public class ExampleFileBucket extends RuleBaseBucket {

	private IFile file;

	//statements that we can pre-prepare
	private PreparedInsert jDocument1;

	private PreparedInsert jLine1;

	private PreparedInsert jLine2;

	private PreparedInsert jLineNumber;

	private PreparedInsert jWord1;

	private PreparedInsert jWord2;

	private PreparedInsert jWordNumber;

	//fields that help in the updating
	private int lineNumber;

	private int wordCounter;

	private int characterCounter;

	private String documentRep;

	private int position;

	public ExampleFileBucket(FrontEnd frontend, IFile file) {
		super(frontend, file.getFullPath().toString());
		this.file = file;
		this.documentRep = file.getFullPath().toString();

		try {
			jDocument1 = prepareForInsertion("jDocument(!x::JDocument)");
			jLine1 = prepareForInsertion("jLine(!x::JLine)");
			jLine2 = prepareForInsertion("jLine(!x::JDocument,!y::JLine)");
			jLineNumber = prepareForInsertion("jLineNumber(!x::JLine,!y)");
			jWord1 = prepareForInsertion("jWord(!x::JWord)");
			jWord2 = prepareForInsertion("jWord(!x::JLine,!y::JWord)");
			jWordNumber = prepareForInsertion("jWordNumber(!x::JWord,!y)");
		} catch (ParseException e) {
			JQueryBackendPlugin.error(e);
		} catch (TypeModeError e) {
			JQueryBackendPlugin.error(e);
		}

	}

	protected void update() throws ParseException, TypeModeError {
		lineNumber = 0;
		wordCounter = 0;
		characterCounter = 0;

		try {
			assertDocument();

			InputStream is = file.getContents();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
			while (reader.ready()) {
				String line = reader.readLine();
				updateDoLine(line);
			}

		} catch (CoreException e) {
			JQueryBackendPlugin.error(e);
		} catch (IOException e) {
			JQueryBackendPlugin.error(e);
		} catch (TyrubaException e) {
			JQueryBackendPlugin.error(e);
		}

	}

	private void updateDoLine(String line) throws TyrubaException, ParseException, TypeModeError {
		lineNumber++;
		wordCounter = 0;

		assertLine(lineNumber, characterCounter, line);
		assertLine(lineNumber);

		String[] words = line.split(" ");
		int innerCharacterCounter = 0;
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word.trim().length() != 0) {
				updateDoWord(lineNumber, word, innerCharacterCounter);
			}
			innerCharacterCounter += (word.length() + 1);
		}

		characterCounter += line.length() + 2;
	}

	private void updateDoWord(int lineNumber, String word, int charCtr) throws TyrubaException, ParseException, TypeModeError {
		wordCounter++;
		assertWord(word, documentRep + lineNumber, wordCounter, charCtr);
		assertWord(lineNumber, word, wordCounter);
	}

	private void assertDocument() throws TyrubaException, ParseException, TypeModeError {
        jDocument1.put("!x", documentRep);
        jDocument1.executeInsert();
        
        PreparedInsert pi = prepareForInsertion("sourceLocation(!x::JDocument,!y)");
        pi.put("!x", documentRep);
        pi.put("!y", new JQueryFileElement(documentRep, 0, 0));
        pi.executeInsert();
        
        pi = prepareForInsertion("jName(!x::JDocument,!y)");
        pi.put("!x", documentRep);
        pi.put("!y", file.getName());
        pi.executeInsert();        
    }

	private void assertLine(int lineNumber, int startPos, String line) throws TyrubaException, ParseException, TypeModeError {
		String lineRep = documentRep + lineNumber;
		jLine1.put("!x", lineRep);
		jLine1.executeInsert();

		PreparedInsert pi = prepareForInsertion("sourceLocation(!x::JLine,!y)");
		pi.put("!x", lineRep);
		pi.put("!y", new JQueryFileElement(documentRep, startPos, line.length()));
		pi.executeInsert();

		jLineNumber.put("!x", lineRep);
		jLineNumber.put("!y", lineNumber);
		jLineNumber.executeInsert();

		pi = prepareForInsertion("jName(!x::JLine,!y)");
		pi.put("!x", lineRep);
		pi.put("!y", line);
		pi.executeInsert();
	}

	private void assertLine(int lineNumber) throws TyrubaException {
		String lineRep = documentRep + lineNumber;
		jLine2.put("!x", documentRep);
		jLine2.put("!y", lineRep);
		jLine2.executeInsert();
	}

	private void assertWord(String word, String lineRep, int wordNumber, int charCtr) throws TyrubaException, ParseException, TypeModeError {
		String wordRep = lineRep + word + wordNumber;
		jWord1.put("!x", wordRep);
		jWord1.executeInsert();

		PreparedInsert pi = prepareForInsertion("sourceLocation(!x::JWord,!y)");
		pi.put("!x", wordRep);
		pi.put("!y", new JQueryFileElement(documentRep, characterCounter + charCtr, word.length()));
		pi.executeInsert();

		pi = prepareForInsertion("jName(!x::JWord,!y)");
		pi.put("!x", wordRep);
		pi.put("!y", word);
		pi.executeInsert();

		jWordNumber.put("!x", wordRep);
		jWordNumber.put("!y", wordNumber);
		jWordNumber.executeInsert();

	}

	private void assertWord(int lineNumber, String word, int wordNumber) throws TyrubaException {
		String lineRep = documentRep + lineNumber;
		String wordRep = lineRep + word + wordNumber;
		jWord2.put("!x", lineRep);
		jWord2.put("!y", wordRep);
		jWord2.executeInsert();
	}

}
