package ca.ubc.jquery.api.tyruba;

import java.io.File;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBFact;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RuleBaseBucket;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceParser;

public class TyRuBaFactBaseResource extends RuleBaseBucket implements JQueryFactGenerator {

	private JQueryResourceParser parser;

	private String name;

	public TyRuBaFactBaseResource(FrontEnd frontend, String name) {
		super(frontend, name);
		this.name = name;
		parser = null;
	}

	public String getName() {
		return name;
	}

	public void insert(String fact, Object[] args) throws JQueryException {
		RBTerm argsTerm[] = new RBTerm[args.length];
		for (int i = 0; i < args.length; i++) {
			argsTerm[i] = convertToRBTerm(args[i]);
		}

		try {
			RBPredicateExpression exp = FrontEnd.makePredicateExpression(fact, argsTerm);
			insert(new RBFact(exp));
		} catch (TypeModeError e) {
			throw new JQueryTyRuBaException("Error inserting fact", e);
		}
	}

	public void insertChild(Object parent, Object child) throws JQueryException {
		insert("child", new Object[] { parent, child });
	}

	public void insertName(Object element, String name) throws JQueryException {
		insert("primLabel", new Object[] { element, name });
	}

	public void insertElementLocation(Object element, String fileName, int offset, int length) throws JQueryException {
		JQueryFileElement e = new JQueryFileElement(fileName, offset, length);
		insert("sourceLocation", new Object[] { element, makeJavaObject(e) });
	}

	public void remove(String name, Object[] args) {
		// TODO Can this be implemented properly?
		setOutdated();
	}

	public void removeAll() {
		setOutdated();
	}

	protected void setParser(JQueryResourceParser parser) {
		this.parser = parser;
	}

	protected void update() throws TypeModeError, ParseException {
		if (parser != null) {
			parser.parse();
		}
	}

	public int getUniqueID(String strRep) {
		return ((WorkingSetFactBase) JQueryAPI.getFactBase()).getFactId(strRep);
	}

	protected RBTerm convertToRBTerm(Object obj) {
		if (obj instanceof Object[]) {
			Object[] arr = (Object[]) obj;

			RBTerm[] temp = new RBTerm[arr.length];
			for (int j = 0; j < arr.length; j++) {
				temp[j] = convertToRBTerm(arr[j]);
			}

			return FrontEnd.makeList(temp);
		} else if (obj instanceof RBTerm) {
			return (RBTerm) obj;
		} else {
			return makeJavaObject(obj);
		}
	}

	// TODO remove methods below this point eventually...
	protected File getDumpFile() {
		// TODO: Make a better fix?
		File f = super.getDumpFile();
		String p = f.getPath();

		// on Windows Machines, the first two characters represent the drive (eg. c:)
		// if we replace the : there, things get a little messed up so we first remove it
		// then replace, then add it back in.
		String drive = p.substring(0, 2);
		p = p.substring(2);

		char[] illegals = { '*', ':', '|', '\"', '?', '<', '>' };
		for (int i = 0; i < illegals.length; i++) {
			p = p.replace(illegals[i], '.');
		}

		return new File(drive + p);
	}
}
