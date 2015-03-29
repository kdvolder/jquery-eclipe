package ca.ubc.jquery.test.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryMenuResults;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

/**
 * This class provides some infrastructure for testing the JQuery backend.
 * Right now it is a bit of a mess. This should be cleaned up and separated into
 * several classes of tests.
 * 
 * This class itself should probably only contain a few simple test just to see
 * whether this infrastructure itself works right. Subclasses should be created
 * for different collections of tests.
 * 
 * @author kdvolder
 */
public class JQueryAPITest {

	private TestProject project;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath loc = workspace.getRoot().getLocation();
		System.out.println("Workspace location = " + loc);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		project = new TestProject();
		JQueryAPI.getFactBase().setFactBaseByName(project.getSourceCode());
		JQueryAPI.getFactBase().reloadFacts();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Note: It is actually convenient NOT to dispose the test fixture project
		// because that will allow you to run an Eclipse instance of the JUnit-workspace
		// and examine the testFixture.
		//		if (project != null)
		//			project.dispose();
	}

	private void query_must_succeed(String string) throws JQueryException {
		JQuery q = JQueryAPI.createQuery(string);
		JQueryResultSet results = q.execute();
		assertTrue(string, results.hasNext());
		results.close();
	}

	private void method_must_exist(String className, String methodName) throws JQueryException {
		query_must_succeed("class(?C),name(?C," + className + ")," + "child(?C,?m),method(?m),name(?m," + methodName + ")");
	}

	private void addTestProject() throws Exception {
		project.addSourceFile("SomeClass.java", "public class SomeClass {public void m1(int p1){} private int f1;}");
		JQueryAPI.getFactBase().reloadFacts();
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getElementImage(java.lang.Object)}.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testProjectCreated() throws CoreException {
		assertTrue("Project must be created", project.exists());
		assertTrue("Project must have java nature", project.hasJavaNature());
		helloWorldSetup();
		assertTrue("Source file not found", project.fileExists("src/Hello.java"));
		project.build();
		assertTrue("Compiled class file not found", project.fileExists("bin/Hello.class"));
	}

	private void helloWorldSetup() throws CoreException {
		project.addSourceFile("Hello.java", "public class Hello { \n" + "  public static void main(String[] args) { \n" + "     System.out.println(\"Hello world!\"); " + "  }\n" + "}");
		JQueryAPI.getFactBase().reloadFacts();
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getElementImage(java.lang.Object)}.
	 */
	//	TODO: @Test
	public void testGetElementImage() {
		// This requires getting an element from the fact base
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link ca.ubc.jquery.api.JQueryAPI#getThisVar()}.
	 */
	@Test
	public void testGetThisVar() {
		String tv = JQueryAPI.getThisVar();
		assertNotNull(tv);
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#setFaceBase(java.lang.String)}.
	 * 
	 * @throws JQueryException
	 */
	@Test
	public void testGetSetFactBase() throws JQueryException {
		String sourceSet = project.getSourceCode();
		JQueryAPI.getFactBase().setFactBaseByName(sourceSet);
		assertEquals(sourceSet, JQueryAPI.getFactBase().getName());
	}

	/**
	 * This test exposes a bug that causes the JQuery AST walker to
	 * throw NPE when it parses generic methods.
	 * 
	 * The bug has been fixed (but without adding true support for 
	 * generics).
	 * 
	 * @throws JQueryException
	 * @throws CoreException 
	 */
	@Test
	public void testGenericMethod() throws JQueryException, CoreException {
		project.addClassWithMethod("import java.util.List;", "Generic", "public <E> void addElement(List<E> foo, E el) { foo.add(el); }");
		project.build();
		JQueryAPI.getFactBase().reloadFacts();
		method_must_exist("Generic", "addElement");
	}

	/**
	 * Test method for {@link ca.ubc.jquery.api.JQueryAPI#topLevelQuery()}.
	 * 
	 * @throws JQueryException
	 */
	@Test
	public void testTopLevelQuery() throws JQueryException {
		Set expected = new HashSet();
		expected.add("Package Browser (.java files)");
		expected.add("Method Browser");
		expected.add("Bookmarks");
		expected.add("Compiler Errors");
		expected.add("Compiler Warnings");
		expected.add("Feature/Redundant Export Annot");
		expected.add("Feature/Redundant Feature Annot");
		expected.add("Features");
		expected.add("Java 1.5 Annotations");
		expected.add("Tags");
		expected.add("Tasks");

		// remove these?
		expected.add("~ Unused/Package Browser (.java|.class files)");
		expected.add("~ Unused/Abstract Classes");
		expected.add("~ Unused/Interface Implementation");

		JQueryMenuResults rs = JQueryAPI.topLevelQuery();
		for (; rs.hasNext(); rs.next()) {
			String label = rs.getLabel();
			System.out.println(label);
			expected.remove(label);
		}
		assertTrue("No more expected queries", expected.isEmpty());
	}

	private Object queryAndReturnFirst(String query, String var) throws Exception {
		JQuery q = JQueryAPI.createQuery(query);
		JQueryResultSet rs = q.execute();
		return rs.next().get(var);
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#menuQuery(java.lang.Object[])}.
	 */
	@Test
	public void testMenuQuery() throws Exception {
		addTestProject();
		Object fbClass = queryAndReturnFirst("class(?X)", "?X");
		Object fbMethod = queryAndReturnFirst("method(?X)", "?X");
		Object fbField = queryAndReturnFirst("field(?X)", "?X");
		Object fbPackage = queryAndReturnFirst("package(?X)", "?X");

		Set expected = new HashSet();
		Set actual = new HashSet();

		try {
			// Menu Query over a class
			JQueryMenuResults m = JQueryAPI.menuQuery(new Object[] { fbClass });
			expected.add("Creators");
			expected.add("Members");
			expected.add("Usage");
			expected.add("Inheritance");
			expected.add("Markers");
			expected.add("Java Structure");

			for (; m.hasNext(); m.next()) {
				actual.add(m.getPath()[0]);
			}
			expected.removeAll(actual);
			if (!expected.isEmpty()) {
				fail("Menu query failed");
			}

			// Menu Query over a method
			m = JQueryAPI.menuQuery(new Object[] { fbMethod });
			expected.add("Calls");
			expected.add("Field Accesses");
			expected.add("Signature");
			expected.add("Markers");
			expected.add("Java Structure");
			expected.add("Inheritance");

			for (; m.hasNext(); m.next()) {
				actual.add(m.getPath()[0]);
			}
			expected.removeAll(actual);
			if (!expected.isEmpty()) {
				fail("Menu query failed");
			}

			// Menu Query over a field
			m = JQueryAPI.menuQuery(new Object[] { fbField });
			expected.add("Markers");
			expected.add("Java Structure");
			expected.add("Written by");
			expected.add("Read by");
			expected.add("Type of Field");

			for (; m.hasNext(); m.next()) {
				actual.add(m.getPath()[0]);
			}
			expected.removeAll(actual);
			if (!expected.isEmpty()) {
				fail("Menu query failed");
			}

			// Menu Query over a package
			m = JQueryAPI.menuQuery(new Object[] { fbPackage });
			expected.add("Markers");
			expected.add("Java Structure");
			expected.add("Top-level Types");
			expected.add("Top-level Classes");

			for (; m.hasNext(); m.next()) {
				actual.add(m.getPath()[0]);
			}
			expected.removeAll(actual);
			if (!expected.isEmpty()) {
				fail("Menu query failed");
			}
		} catch (JQueryException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link ca.ubc.jquery.api.JQueryAPI#queryPredicates()}.
	 */
	@Test
	public void testQueryPredicates() throws JQueryException {
		JQueryResultSet results = null;

		try {
			JQuery q = JQueryAPI.queryPredicates();
			results = q.execute();

			Set expected = new HashSet();
			expected.add("class(?X)");
			expected.add("package(?P,?C)");

			while (results.hasNext()) {
				JQueryResult r = results.next();
				String predicate = (String) r.get(0);
				String tooltip = (String) r.get(1);
				expected.remove(predicate);
			}
			results.close();
		} catch (RuntimeException e) {
			results.close();
		}
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#createQuery(java.lang.String)}.
	 * 
	 * @throws CoreException
	 * @throws JQueryException
	 */
	@Test
	public void testCreateQuery0() throws CoreException, JQueryException {
		helloWorldSetup();
		query_must_succeed("class(?H)");
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#createQuery(java.lang.String)}.
	 * 
	 * @throws CoreException
	 * @throws JQueryException
	 */
	@Test
	public void testCreateQuery1() throws CoreException, JQueryException {
		helloWorldSetup();
		query_must_succeed("class(?H),name(?H,Hello)");
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#createQuery(java.lang.String)}.
	 * 
	 * @throws CoreException
	 * @throws JQueryException
	 */
	@Test
	public void testCreateQuery2() throws CoreException, JQueryException {
		helloWorldSetup();
		query_must_succeed("class(?H),name(?H,Hello),child(?H,?m),name(?m,main)");
	}

	//TODO: @Test
	public void testCreateQueryN() throws CoreException {
		fail("Need to implement more tests for queries?");
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#bindQueryVariable(ca.ubc.jquery.api.JQuery, java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testBindQueryVariable() throws Exception {
		addTestProject();
		Object b = queryAndReturnFirst("class(?X),name(?X,SomeClass)", "?X");
		JQuery q = JQueryAPI.createQuery("child(!this,?C)");
		q.bind("!this", b);
		// if no exception thrown, this test succeeded

		// FIXME Re-enable this when type checking is working again...
		//		q = JQueryAPI.createQuery("calls(!this,?,?)");
		//		try {
		//			q.bind("!this", b);
		//			fail("No type error thrown");
		//		} catch (JQueryException e) {
		//			// We wanted that to throw a type error
		//		}
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getFileElement(java.lang.Object)}.
	 */
	//TODO: @Test
	public void testGetSourceLocation() {
		// This requires getting an element from the fact base
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getStringProperty(java.lang.Object, java.lang.String)}.
	 */
	//TODO: @Test
	public void testGetStringProperty() {
		// This requires getting an element from the fact base
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getElementLabel(java.lang.Object)}.
	 */
	@Test
	public void testGetElementLabel() throws Exception {
		addTestProject();
		Object fbClass = queryAndReturnFirst("class(?X),name(?X,SomeClass)", "?X");
		Object fbMethod = queryAndReturnFirst("class(?Y),name(?Y,SomeClass),method(?X)", "?X");

		assertEquals("Class label", "SomeClass", JQueryAPI.getElementLabel(fbClass));
		assertEquals("Method label", "m1(int)", JQueryAPI.getElementLabel(fbMethod));
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getElementType(java.lang.Object)}.
	 */
	@Test
	public void testGetElementType() throws Exception {
		addTestProject();
		Object fbField = queryAndReturnFirst("class(?Y),name(?Y,SomeClass),field(?X)", "?X");
		Object fbMethod = queryAndReturnFirst("class(?Y),name(?Y,SomeClass),method(?X)", "?X");

		assertEquals("Field type", "field", JQueryAPI.getElementType(fbField));
		assertEquals("Method type", "method", JQueryAPI.getElementType(fbMethod));
	}

	/**
	 * Test method for
	 * {@link ca.ubc.jquery.api.JQueryAPI#getIntProperty(java.lang.Object, java.lang.String)}.
	 */
	//TODO: @Test
	public void testGetIntProperty() {
		// This requires getting an element from the fact base
		fail("Not yet implemented");
	}

	@Test
	public void testQueryErrorException() {
		try {
			JQueryAPI.createQuery("query(with error!");
		} catch (Exception e) {
			assertTrue("Possible impropery query exception thrown", e.getMessage().startsWith("Problem preparing query: query(with error!: tyRuBa.parser.ParseException: Encountered \"error\" at line 1, column 12."));
		}
	}

	@Test
	public void testAnnotationQuery() throws CoreException, JQueryException {
		project.addSourceFile("SomeAnnotations.java", 
			"public class SomeAnnotations {\n" +
			"public @interface Foo {\n" +
				"int x() default 5;" +
				"String y() default \"hello\";" +
				"String value() default \"there\";" +
				"Bar bar() default @Bar;\n }" +
			
			"public @interface Bar {\n" +
				"int value() default 10;\n }" +
			
			"@Foo(\"alpha\") void alpha () { }\n" +
			"@SomeAnnotations.Foo void beta () { }\n" +
			"@Foo(x=10, y=\"whatever\", bar=@Bar(2)) void gamma () { }\n }");
		JQueryAPI.getFactBase().reloadFacts();

		// Test annotation declaration recognition
		query_must_succeed("annotationDeclaration(?T),name(?T,Foo),annotType(?A,?T),hasAnnotation(?X,?A),name(?X,alpha)");
	
		// Test annotation declaration attribute recognition/default values
		query_must_succeed("annotationDeclaration(?T),name(?T,Foo),method(?T,?M),name(?M,y),hasDefault(?M)");		
		query_must_succeed("annotationDeclaration(?T),name(?T,Foo),method(?T,?M),name(?M,y),defaultValue(?M,hello)");		
		
		// Test annotation recognition and types
		query_must_succeed("hasAnnotation(?X,?Annot),name(?X,alpha),annotType(?Annot,?C),interface(?C),name(?C,Foo)");
		query_must_succeed("hasAnnotation(?X,?Annot),name(?X,beta),annotType(?Annot,?C),interface(?C),name(?C,Foo)");
		query_must_succeed("hasAnnotation(?X,?Annot),name(?X,gamma),annotType(?Annot,?C),interface(?C),name(?C,Foo)");
		
		// Test annotation attribute recognition and default values
		query_must_succeed("hasAnnotation(?X,?Annot),name(?X,gamma),annotType(?Annot,?C),interface(?C),name(?C,Foo),attribute(?Annot,y,whatever),attribute(?Annot,value,there)");
		
		// Test annotations inside annotations
		query_must_succeed("hasAnnotation(?X,?Annot),name(?X,gamma),annotType(?Annot,?C),interface(?C),name(?C,Foo),attribute(?Annot,bar,?IA),annotType(?IA,?B),name(?B,Bar),attribute(?IA,value,2)");
	}
	
//	@Test public void testTypeId() throws CoreException, JQueryException {
//		String asmStringRep = org.ob
//		String stringID = JQueryAPI.getFactBase().getTypeRep(
//	}


	//TODO: A test that checks whether plugin state is properly restored after
	//plugin is stopped and restarted.
}
