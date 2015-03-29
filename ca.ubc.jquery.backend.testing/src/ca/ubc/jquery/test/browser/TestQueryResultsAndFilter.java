//package ca.ubc.jquery.test.browser;
//
//import static org.junit.Assert.assertFalse;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import ca.ubc.jquery.api.JQueryAPI;
//import ca.ubc.jquery.api.JQueryResultSet;
//import ca.ubc.jquery.gui.results.QueryResultNode;
//import ca.ubc.jquery.query.IQueryResults;
//import ca.ubc.jquery.query.QueryResults;
//import ca.ubc.jquery.query.QueryResultsAndFilter;
//import ca.ubc.jquery.test.backend.TestProject;
//
//public class TestQueryResultsAndFilter extends TestIQueryResults {
//	private QueryResultsAndFilter simpleQuery;
//
//	private QueryResultsAndFilter complexQuery;
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//		TestProject project = new TestProject();
//		project.addSourceFile("SomeClass.java", "public class SomeClass {public void m1(int p1){} private int f1;}");
//		project.addSourceFile("SomeClass2.java", "public class SomeClass2 {private void m0(){} public void m1(int p1){SomeClass x = new SomeClass();x.m1();} private int f1;}");
//		JQueryAPI.getFactBase().setFactBaseByName(project.getSourceCode());
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//		JQueryAPI.shutdown();
//	}
//
//	@Override
//	public IQueryResults generateTestCandidate(String targetqs, String var, String qs) throws Exception {
//		Object fbClass = queryAndReturnFirst(targetqs, var);
//		return new QueryResultsAndFilter(new QueryResults(fbClass, qs));
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		super.setUp();
//		simpleQuery = (QueryResultsAndFilter) super.simpleQuery;
//		complexQuery = (QueryResultsAndFilter) super.complexQuery;
//
//		// need this class to properly detect a private method
//		Object fbClass = queryAndReturnFirst("equals(?C,\"%.SomeClass2\"::RefType)", "?C");
//		simpleQuery.setTarget(fbClass);
//	}
//
//	@After
//	public void tearDown() {
//		super.tearDown();
//	}
//
//	@Test
//	public void testAddFilter() throws Exception {
//		JQueryResultSet rs = simpleQuery.execute();
//		simpleQuery.addFilter("public", "modifier(!this,public)", new QueryResultNode(null, "?M"));
//		JQueryResultSet ars = simpleQuery.execute();
//		while (rs.hasNext() && ars.hasNext()) {
//			rs.next();
//			ars.next();
//		}
//		assertFalse("Should have different results", rs.hasNext() == ars.hasNext());
//	}
//
//	@Test
//	public void testRemoveFilter() throws Exception {
//		simpleQuery.addFilter("public", "modifier(!this,public)", new QueryResultNode(null, "?M"));
//		JQueryResultSet rs = simpleQuery.execute();
//		simpleQuery.removeFilter("public");
//		JQueryResultSet ars = simpleQuery.execute();
//
//		while (rs.hasNext() && ars.hasNext()) {
//			rs.next();
//			ars.next();
//		}
//		assertFalse("Should have different results", rs.hasNext() == ars.hasNext());
//	}
//}
