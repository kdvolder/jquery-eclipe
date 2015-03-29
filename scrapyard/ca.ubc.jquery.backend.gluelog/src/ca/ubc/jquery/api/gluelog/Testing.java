package ca.ubc.jquery.api.gluelog;
//package ca.ubc.jquery.api.jtransformer;
//
//import java.util.Map;
//
//import ca.ubc.jquery.api.JQueryException;
//import ca.ubc.jquery.api.JQueryResult;
//import ca.ubc.jquery.api.JQueryResultSet;
//
//import org.cs3.pl.prolog.*;
//
///**
// * This class is supposed to provide query results on request.  This is instead of the usual, generate all results
// * before looking at them.  It is supposed to provide quicker query results because when the user is done, they can 
// * simply stop asking for results rather than waiting till all results and found before examining the data.  
// * 
// * Generally it's not working yet so that's why it still has this silly name...
// * @author lmarkle
// */
//public class Testing implements JQueryResultSet {
//
//	String queryString;
//
//	JTransformerQueryResult next;
//
//	boolean hasNext;
//
//	String[] vars;
//
//	PrologSession query;
//
//	protected Testing(String q) {
//		queryString = q;
//		next = null;
//		vars = null;
//		query = null;
//
//		getNext();
//	}
//
//	protected Testing(String q, String[] v) {
//		queryString = q;
//		vars = v;
//		next = null;
//
//		getNext();
//	}
//
//	public boolean hasNext() {
//		return hasNext;
//	}
//
//	public JQueryResult next() throws JQueryException {
//		JQueryResult result = next;
//		getNext();
//		return result;
//	}
//
//	private void getNext() {
//		try {
//			if (query == null)
//				query = JQueryJTransformerAPI.getPrologConnection();
//			
//			Map temp;
//			if (next == null)
//				temp = query.query(queryString);
//			else
//				temp = query.next();
//
//			if (vars != null)
//				next = new JTransformerQueryResult(temp, vars);
//			else
//				next = new JTransformerQueryResult(temp);
//			hasNext = true;
//		} catch (Exception e) {
//			hasNext = false;
//		} finally {
//			// if (query != null)
//			// query.dispose();
//		}
//	}
//}
