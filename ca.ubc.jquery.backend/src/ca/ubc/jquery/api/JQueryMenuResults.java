package ca.ubc.jquery.api;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class JQueryMenuResults {

	private SortedMap results;

	private Iterator it;

	private String current;

	protected JQueryMenuResults(JQueryResultSet rs) throws JQueryException {
		results = new TreeMap();
		try {
			while (rs.hasNext()) {
				JQueryResult r = rs.next();

				String[] path = stringArrayFromObjectArray((Object[]) r.get(0));
				String query = (String) r.get(1);
				String[] varList = stringArrayFromObjectArray((Object[]) r.get(2));

				String label = createLabel(path);
				results.put(label, new Object[] { path, query, varList });
			}
		} finally {
			it = results.keySet().iterator();
			next();
			rs.close();
		}
	}

	public boolean hasNext() {
		return (current != null);
	}

	public void next() {
		if (!it.hasNext()) {
			current = null;
		} else {
			current = (String) it.next();
		}
	}

	public String getLabel() {
		return current;
	}

	public String[] getPath() {
		return getPath(current);
	}

	public String[] getPath(String label) {
		Object[] t = (Object[]) results.get(label);
		return (String[]) t[0];
	}

	public String getQuery() {
		return getQuery(current);
	}

	public String getQuery(String label) {
		Object[] t = (Object[]) results.get(label);
		return (String) t[1];
	}

	public String[] getChosenVariables() {
		return getChosenVariables(current);
	}

	public String[] getChosenVariables(String label) {
		Object[] t = (Object[]) results.get(label);
		return (String[]) t[2];
	}

	private String[] stringArrayFromObjectArray(Object[] o) {
		String[] r = new String[o.length];
		for (int i = 0; i < o.length; i++) {
			r[i] = o[i].toString();
		}
		return r;
	}

	private String createLabel(Object[] elements) {
		String label = elements[0].toString();
		for (int i = 1; i < elements.length; i++) {
			label = label + "/" + elements[i];
		}
		return label;
	}
}