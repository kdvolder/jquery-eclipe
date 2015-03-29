//package ca.ubc.jquery.test.browser;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import ca.ubc.jquery.api.JQuery;
//import ca.ubc.jquery.api.JQueryAPI;
//import ca.ubc.jquery.api.JQueryResult;
//import ca.ubc.jquery.api.JQueryResultSet;
//import ca.ubc.jquery.query.IQueryResults;
//import ca.ubc.jquery.query.QueryResults;
//import ca.ubc.jquery.test.backend.TestProject;
//
//public class TestIQueryResults {
//
//	protected IQueryResults simpleQuery;
//
//	protected IQueryResults complexQuery;
//
//	public IQueryResults generateTestCandidate(String targetqs, String var, String qs) throws Exception {
//		Object fbClass = queryAndReturnFirst(targetqs, var);
//		return new QueryResults(fbClass, qs);
//	}
//
//	public IQueryResults generateTestCandidate(String qs) throws Exception {
//		return generateTestCandidate("class(?X)", "?X", qs);
//	}
//
//	protected Object queryAndReturnFirst(String query, String var) throws Exception {
//		JQuery q = JQueryAPI.createQuery(query);
//		JQueryResultSet rs = q.execute();
//		return rs.next().get(var);
//	}
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//		TestProject project = new TestProject();
//		project.addSourceFile("SomeClass.java", "public class SomeClass {private void m0(){} public void m1(int p1){} private int f1;}");
//		project.addSourceFile("SomeClass2.java", "public class SomeClass2 {private void m0(){} public void m1(int p1){SomeClass x = new SomeClass();x.m1();} private int f1;}");
//		JQueryAPI.getFactBase().setFactBaseByName(project.getSourceCode());
//		JQueryAPI.getFactBase().reloadFacts();
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//		JQueryAPI.shutdown();
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		simpleQuery = generateTestCandidate("class(?X)", "?X", "child(!this,?M),method(?M)");
//		complexQuery = generateTestCandidate("package(?X),child(!this,?M),method(?M),calls(?O,?M,?Ref),package(?M,?P)");
//	}
//
//	@After
//	public void tearDown() {
//	}
//
//	@Test
//	public void testAllowQueryToBindToThisNode() throws Exception {
//		IQueryResults falseCandidate = generateTestCandidate("child(?X,?Y)");
//		IQueryResults anotherFalse = generateTestCandidate("child(!this,?X)");
//		anotherFalse.setTarget(JQueryAPI.getFactBase());
//
//		assertTrue("Valid allow bind", simpleQuery.allowQueryToBindToThisNode());
//		assertFalse("Invalid query bind", falseCandidate.allowQueryToBindToThisNode());
//		assertFalse("Bind to fact base", anotherFalse.allowQueryToBindToThisNode());
//	}
//
//	@Test
//	public void testSetTarget() throws Exception {
//		Object target = simpleQuery.getTarget();
//		Object fbMethod = queryAndReturnFirst("method(?M)", "?M");
//
//		simpleQuery.setTarget(fbMethod);
//		assertEquals("New target set", fbMethod, simpleQuery.getTarget());
//		simpleQuery.setTarget(target);
//		assertEquals("Reset target", target, simpleQuery.getTarget());
//	}
//
//	@Test
//	public void testBindQueryTarget() throws Exception {
//		try {
//			simpleQuery.bindQueryTarget();
//		} catch (Exception e) {
//			fail("Bind target should not fail");
//		}
//
//		// Test is meaningless in a typeless world
//		//		IQueryResults failBind = generateTestCandidate("calls(!this,?M,?Ref)");
//		//		try {
//		//			failBind.bindQueryTarget();
//		//			fail("This bind should have failed!");
//		//		} catch (Exception e) {
//		//
//		//		}
//	}
//
//	@Test
//	public void testSetQueryString() {
//		String qs = simpleQuery.getQueryString();
//		String newQs = "child(!this,?M)";
//
//		simpleQuery.setQuery(newQs);
//		assertEquals("New query string", newQs, simpleQuery.getQueryString());
//		simpleQuery.setQuery(qs);
//		assertEquals("New query string", qs, simpleQuery.getQueryString());
//	}
//
//	@Test
//	public void testSetQueryJQuery() throws Exception {
//		String qs = "package(!this,?P)";
//		JQuery q = simpleQuery.getQuery();
//		JQuery q2 = JQueryAPI.createQuery(qs);
//		simpleQuery.setQuery(q2);
//		assertEquals("Query string should be reset", qs, simpleQuery.getQueryString());
//		assertFalse("Query should not be equal", (q == q2));
//	}
//
//	@Test
//	public void testInvalidate() throws Exception {
//		JQuery q = simpleQuery.getQuery();
//		simpleQuery.invalidate();
//		JQuery q2 = simpleQuery.getQuery();
//		assertFalse("Should not be equal", (q == q2));
//	}
//
//	@Test
//	public void testSetChosenVars() throws Exception {
//		List vars = new ArrayList();
//		vars.add("?M");
//		simpleQuery.setChosenVars(vars);
//		List cVars = simpleQuery.getChosenVars();
//		assertEquals("Simple chosen vars", vars, cVars);
//
//		vars.clear();
//		vars.add("?P");
//		vars.add("?O");
//		vars.add("?Ref");
//		vars.add("?M");
//		complexQuery.setChosenVars(vars);
//		cVars = complexQuery.getChosenVars();
//		assertEquals("Complicated chosen vars", vars, cVars);
//
//		try {
//			vars.clear();
//			vars.add("?M");
//			vars.add("?ABC");
//			vars.add("?P");
//			complexQuery.setChosenVars(vars);
//			fail("Should throw exception on invalid variable selection");
//		} catch (Exception e) {
//
//		}
//	}
//
//	@Test
//	public void testGetVarsFromQuery() throws Exception {
//		Set s = simpleQuery.getVarsFromQuery();
//		s.remove("?M");
//		s.remove("!this");
//		assertTrue("Variable set is not empty", s.isEmpty());
//
//		s = complexQuery.getVarsFromQuery();
//		s.remove("?X");
//		s.remove("?M");
//		s.remove("?O");
//		s.remove("?P");
//		s.remove("?Ref");
//		s.remove("!this");
//
//		IQueryResults rs = new QueryResults(simpleQuery.getTarget(), "class(!this)");
//		s = rs.getVarsFromQuery();
//		assertTrue("Set is null", s != null);
//		assertTrue("Set is not empty", s.isEmpty());
//	}
//
//	@Test
//	public void testIsSubQuery() throws Exception {
//		assertTrue("Should be sub query", simpleQuery.isSubQuery());
//
//		IQueryResults notSub = generateTestCandidate("child(?X,?Y)");
//		notSub.setTarget(JQueryAPI.getFactBase());
//
//		assertFalse("Should not be sub query", notSub.isSubQuery());
//	}
//
//	@Test
//	public void testExecute() throws Exception {
//		JQueryResultSet rs = simpleQuery.execute();
//		assertTrue("Query returned something", (rs != null));
//
//		rs = complexQuery.execute();
//		assertTrue("Query returned something", (rs != null));
//		List vars = new ArrayList();
//		vars.add("?P");
//		vars.add("?M");
//		vars.add("?X");
//		complexQuery.setChosenVars(vars);
//		rs = complexQuery.execute();
//		assertTrue("Query returned something", (rs != null));
//		if (rs.hasNext()) {
//			JQueryResult res = rs.next();
//			assertTrue("Result 1 should be equal", (res.get("?P") == res.get(0)));
//			assertTrue("Result 2 should be equal", (res.get("?M") == res.get(1)));
//			assertTrue("Result 3 should be equal", (res.get("?X") == res.get(2)));
//		} else {
//			fail("Please edit this query (or the fact base) so that we have at least one result!");
//		}
//	}
//
//	@Test
//	public void testClone() {
//		IQueryResults t = (IQueryResults) simpleQuery.clone();
//		assertTrue("Same type", t.getClass().equals(simpleQuery.getClass()));
//		assertTrue("Same target", t.getTarget().equals(simpleQuery.getTarget()));
//		assertTrue("Same variables", t.getChosenVars().equals(simpleQuery.getChosenVars()));
//		assertTrue("Same query", t.getQueryString().equals(simpleQuery.getQueryString()));
//	}
//}
