package ca.ubc.jquery.test.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import ca.ubc.jquery.api.JQueryFileElement;

/**
 * Test whether SourceLocation object works properly. (Right now it doesn't)
 * 
 * Note: This test can be run as a simple JUnit test (doesn't need to be run as Plugin JUnit)
 * it only tests some simple functionality 
 * 
 * @author kdvolder
 */

public class JQueryFileElementTest {

	//	@BeforeClass
	//	public static void setUpBeforeClass() throws Exception {
	//	}
	//
	//	@AfterClass
	//	public static void tearDownAfterClass() throws Exception {
	//	}
	//
	//	@Before
	//	public void setUp() throws Exception {
	//	}
	//
	//	@After
	//	public void tearDown() throws Exception {
	//	}

	//	/**
	//	 * The toString() method is important for TyRuBa persistence. The
	//	 * current implementation of TyRuBa will use it and assumes that it comes out 
	//	 * in a format "..."::ca.ubc.jquery.SourceLocation.
	//	 * The "..." should be a TyRuBa String literal the value of which
	//	 * TyRuBa will pass into the single argument constructor to reconstitute the object
	//	 * upon loading it from disk.
	//	 * <p>
	//	 * This test calls directly into TyRuBa code to test whether this works
	//	 * correctly. At present it doesn't, a mysterious "null" appears in the
	//	 * toString representation.
	//	 * 
	//	 * @throws Exception 
	//	 */
	//	@Test
	//	public void testToString() throws Exception {
	//		JQueryFileElement sloc = new JQueryFileElement("fooFile", 13, 2, 9);
	//		JQueryFileElement sloc2 = new JQueryFileElement("barFile", 13, 2, 9);
	//		assertFalse("Two JQueryFileElements in different files are not equal", sloc.equals(sloc2));
	//		String str = sloc.toString();
	//		TyRuBaConf conf = new TyRuBaConf();
	//		//		FrontEnd frontend = new FrontEnd(false);
	//		// TODO: What does this mean?!?
	//		FrontEnd frontend = new FrontEnd(conf);
	//		try {
	//			RBJavaObjectCompoundTerm term = (RBJavaObjectCompoundTerm) frontend.makeTermFromString(str);
	//			JQueryFileElement reconstituted = (JQueryFileElement) term.getObject();
	//			assertEquals("Reconstituted must be equal to original", sloc, reconstituted);
	//			assertFalse("Reconstituted must be different", sloc2.equals(reconstituted));
	//		} finally {
	//			frontend.shutdown();
	//		}
	//	}
	//
	//	/**
	//	 * Similar test to test above, but throw in some odd characters as well to see if
	//	 * they get escaped properly. 
	//	 * 
	//	 * @throws Exception
	//	 */
	//	@Test
	//	public void testToStringOddCharacters() throws Exception {
	//		//TODO?: JQueryFileElement sloc = new JQueryFileElement("=\"\'\\n#%\\@",13,2,9);
	//		// Note: the above one is commented out and probably doesn't pass, because of the " 
	//		// and other characters that need escaping.
	//		// Itbut doesn't really have to pass if we can assume these characters are never part of a locationID.
	//		// This *seems* to be the case, but it is unclear whether this assumption is *always* true however.
	//		JQueryFileElement sloc = new JQueryFileElement("=foo/bar%itchnee## x", 13, 2, 9);
	//		JQueryFileElement sloc2 = new JQueryFileElement("barFile", 13, 2, 9);
	//		assertFalse(sloc.equals(sloc2));
	//		String str = sloc.toString();
	//		TyRuBaConf conf = new TyRuBaConf();
	//		// TODO: What does this mean?!?
	//		//		FrontEnd frontend = new FrontEnd(false);
	//		FrontEnd frontend = new FrontEnd(conf);
	//		RBJavaObjectCompoundTerm term = (RBJavaObjectCompoundTerm) frontend.makeTermFromString(str);
	//		JQueryFileElement reconstituted = (JQueryFileElement) term.getObject();
	//		assertEquals("Reconstituted must be equal to original", sloc, reconstituted);
	//		assertFalse("Reconstituted must be different", sloc2.equals(reconstituted));
	//	}

	@Test
	public void testEquals() {
		JQueryFileElement sloc = new JQueryFileElement("fooFile", 13, 2);
		assertEquals("Equals is broken", sloc, new JQueryFileElement("fooFile", 13, 2));
		assertEquals("Equals is broken", new JQueryFileElement("fooFile", 13, 2), sloc);

		assertFalse("Equals doesn't properly distinguish ...", sloc.equals(new JQueryFileElement("barFile", 13, 2)));
		assertFalse("Equals doesn't properly distinguish ...", sloc.equals(new JQueryFileElement("fooFile", 12, 2)));
		assertFalse("Equals doesn't properly distinguish ...", sloc.equals(new JQueryFileElement("fooFile", 13, 1)));

		assertFalse("Equals doesn't properly distinguish ...", new JQueryFileElement("barFile", 13, 2).equals(sloc));
		assertFalse("Equals doesn't properly distinguish ...", new JQueryFileElement("fooFile", 12, 2).equals(sloc));
		assertFalse("Equals doesn't properly distinguish ...", new JQueryFileElement("fooFile", 13, 1).equals(sloc));
	}

	//TODO: @Test 
	public void testCompareTo() {
		//Should implement and check whether this works consitently with equals and that
		//for any pair of non equal objects we get proper "assymetry": a<b <=> b<a
		fail("Not yet implemented");
	}

	@Test
	public void testJQueryFileElementFromString() {
		assertEquals("Parse JQueryFileElement from String broken?", new JQueryFileElement("foo", 1, 2), new JQueryFileElement("foo(1,2)"));
	}

	//	@Test
	//	public void testTwoLevelKey() {
	//		JQueryFileElement sloc = new JQueryFileElement("fooFile",13,2,9);
	//		assertNotNull(sloc.getFirst());
	//		assertNotNull(sloc.getSecond());
	//	}

}
