package ca.ubc.jquery.api.gluelog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import ca.ubc.gluelog.values.ChoiceValue;
import ca.ubc.gluelog.values.Value;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

public class GlueLogQueryResultSet implements JQueryResultSet {

//	private PrologSession ps;

	private Iterator<Object> rawResults;
	private String[] vars; // 
	
	public GlueLogQueryResultSet(String[] vars, Value result) {
		rawResults = result.values();
		this.vars = vars; 
	}
	
	// FIXME testing  - empty result set
	public GlueLogQueryResultSet() {
		List results = new ArrayList();
		rawResults = results.iterator();
	}
	
	// FIXME testing - this is nonsense
	public GlueLogQueryResultSet(String[] vars) {
		
//		Map result = new HashMap();
//		for (int i = 0; i < vars.length; i++) {
//			result.put(vars[i], vars[i] + "valueA");
//		}
//		GlueLogQueryResult qResult1 = new GlueLogQueryResult(result);
//		
//		Map result2 = new HashMap();
//		for (int i = 0; i < vars.length; i++) {
//			result2.put(vars[i], vars[i] + "valueB");
//		}
//		GlueLogQueryResult qResult2 = new GlueLogQueryResult(result2);
//
//		List results = new ArrayList();
//		results.add(qResult1);
//		results.add(qResult2);
//		rawResults = results.iterator();
		this();
	}
	

//	protected JTransformerQueryResultSet(PrologSession ps) {
//		List results = new ArrayList();
//		resultIterator = results.iterator();
//		this.ps = ps;
//	}
//
//	protected JTransformerQueryResultSet(PrologSession ps, List r) {
//		this(ps);
//		List results = new ArrayList();
//		for (Iterator it = r.iterator(); it.hasNext();) {
//			results.add(new GlueLogQueryResult((Map) it.next()));
//		}
//
//		resultIterator = results.iterator();
//	}
//
//	protected JTransformerQueryResultSet(PrologSession ps, List r, String[] vars) {
//		this(ps);
//		List results = new ArrayList();
//		for (Iterator it = r.iterator(); it.hasNext();) {
//			results.add(new GlueLogQueryResult((Map) it.next(), vars));
//		}
//
//		resultIterator = results.iterator();
//	}

	public boolean hasNext() {
		return rawResults.hasNext();
	}

	public JQueryResult next() throws JQueryException {
		// we need to construct a GlueLogQueryResult from the raw result
		return new GlueLogQueryResult(vars, rawResults.next());
	}

	public void close() {
//		if (ps != null && !ps.isDisposed()) {
//			ps.dispose();
//		}
	}
}
