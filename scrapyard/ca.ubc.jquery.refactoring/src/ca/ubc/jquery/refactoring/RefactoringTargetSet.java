package ca.ubc.jquery.refactoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * A set of targets for refactoring based on the results of a JQuery query.
 * @author awjb
 */
public class RefactoringTargetSet implements Iterable<RefactoringTargetSet.RefactoringTarget> {
	private final String[] chosenVars;

	// Note: we use a set instead of a simple list because we can get duplicate
	// results out of the ResultSet when there are unchosen variables in the query
	// (e.g. if query contains ?A, ?B, ?C and only ?A and ?B are chosen variables,
	// then we may get redundant results with the same ?A and ?B but different ?C.)
	private Set<RefactoringTarget> results = new HashSet<RefactoringTarget>();

	public RefactoringTargetSet(String[] chosenVars) {
		this.chosenVars = chosenVars;
	}

	public RefactoringTargetSet(JQueryResultSet rs, String[] chosenVars) throws JQueryException {
		this(chosenVars);
		addResults(rs);
	}

	public Iterator<RefactoringTarget> iterator() {
		return Collections.unmodifiableSet(results).iterator();
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public void addResults(JQueryResultSet rs) throws JQueryException {
		while (rs.hasNext()) {
			results.add(new RefactoringTarget(rs.next()));
		}
	}

	/**
	 * A target for refactoring based on a result from a JQuery query.
	 * @author awjb
	 */
	public class RefactoringTarget {
		private HashMap<String, Object> map = new HashMap<String, Object>();

		RefactoringTarget(JQueryResult result) throws JQueryException {
			for (String var : chosenVars) {
				map.put(var, result.get(var));
			}
		}

		public Object get(int index) {
			return map.get(chosenVars[index]);
		}
		
		@Override
		public boolean equals (Object obj) {
			return (obj instanceof RefactoringTarget && map.equals(((RefactoringTarget)obj).map));
		}
		
		@Override
		public int hashCode () {
			return map.hashCode() + 79;
		}
	}
}
