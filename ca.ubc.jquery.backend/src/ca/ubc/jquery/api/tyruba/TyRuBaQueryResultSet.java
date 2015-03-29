package ca.ubc.jquery.api.tyruba;

import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * !this var is handled in a special way:
 * 		Because TyRuBaQuery binds a !this to multiple targets using ?this, we substitute
 * 		!this for ?this and return results.  If a user requests the value of !this, we actually
 * 		make a substitution there as well.
 * 
 * 		A better way to do this would be to change the way we bind !this to multiple targets
 * 		but for now it should work.
 * 
 * NOTE: This class contains hacks to allow queries over list of results.
 * NOTE 2: The above note is deprecated
 */
public class TyRuBaQueryResultSet implements JQueryResultSet {

	//	// Hack here...
	//	private Collection appendedResults;
	//
	//	// Hack here...
	//	private Iterator appendedResultsIterator;

	private ResultSet results;

	private String[] vars;

	private boolean hasNext;

	private JQueryResult next;

	protected TyRuBaQueryResultSet(ResultSet r, String[] vars) throws JQueryException {
		results = r;
		this.vars = vars;

		// replace !this with ?this
		for (int i = 0; i < vars.length; i++) {
			if (JQueryAPI.getThisVar().equals(vars[i])) {
				this.vars[i] = "?this";
			}
		}

		//		// Hack here...
		//		appendedResults = new LinkedList();
		//		// Hack here...
		//		appendedResultsIterator = null;

		if (r == null) {
			next = null;
			hasNext = false;
		} else {
			try {
				getNext();
			} catch (TyrubaException e) {
				throw new JQueryTyRuBaException(e.getMessage());
			}
		}
	}

	//	// Hack here...
	//	protected void appendResults(TyRuBaQueryResultSet r) throws JQueryException {
	//		// only add if the ResultSet is non-empty
	//		if (r.hasNext()) {
	//			appendedResults.add(r);
	//
	//			if (!hasNext()) {
	//				next();
	//
	//				// because we append the results in the thread which generates the results,
	//				// it's not necessarily the thread that will read/use the results (often this 
	//				// will be the case in GUI stuff).  SO, we remove the element we just chose and
	//				// reset the iterator so that the results will again be used properly.
	//				appendedResultsIterator.remove();
	//				appendedResultsIterator = null;
	//			}
	//		}
	//	}

	public boolean hasNext() {
		return hasNext;
	}

	public JQueryResult next() throws JQueryException {
		JQueryResult result = next;

		try {
			getNext();
		} catch (TyrubaException e) {
			throw new JQueryTyRuBaException(e.getMessage());
		}

		return result;
	}

	private void getNext() throws TyrubaException, JQueryException {
		hasNext = results.next();

		if (hasNext)
			next = new TyRuBaQueryResult(results, vars);
		else {
			//			// Hack here...
			//			if (appendedResultsIterator == null && !appendedResults.isEmpty()) {
			//				appendedResultsIterator = appendedResults.iterator();
			//			}
			//
			//			// Hack here...
			//			if (appendedResultsIterator != null && appendedResultsIterator.hasNext()) {
			//				TyRuBaQueryResultSet temp = (TyRuBaQueryResultSet) appendedResultsIterator.next();
			//				results = temp.results;
			//				next = temp.next;
			//				hasNext = temp.hasNext;
			//			} else {
			//				// not here...
			next = null;
			//			}
		}
	}

	public void close() {
		results.close();
	}
}
