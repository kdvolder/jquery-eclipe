//package ca.ubc.jquery.test.browser;
//
//import static org.junit.Assert.*;
//
//import java.util.Iterator;
//import java.util.Stack;
//
//import org.eclipse.ui.IPerspectiveDescriptor;
//import org.eclipse.ui.IViewReference;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PlatformUI;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import ca.ubc.jquery.api.JQuery;
//import ca.ubc.jquery.api.JQueryAPI;
//import ca.ubc.jquery.api.JQueryResultSet;
//import ca.ubc.jquery.gui.JQueryTreeView;
//import ca.ubc.jquery.gui.results.QueryNode;
//import ca.ubc.jquery.gui.results.ResultsTreeNode;
//import ca.ubc.jquery.query.QueryResults;
//import ca.ubc.jquery.test.backend.TestProject;
//
//public class TestJQueryTreeViewBrowser {
//
//	private JQueryTreeView view;
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
//	@Before
//	public void setUp() throws Exception {
//		view = getTreeView();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test
//	public void testSetTreeRoot() throws Exception {
//		Object fbClass = queryAndReturnFirst("class(?X)", "?X");
//		ResultsTreeNode n = new QueryNode(new QueryResults(fbClass, "child(!this,?M),method(?M)", "Methods"));
//		view.setTreeRoot(n);
//		assertEquals("Same tree root", n, view.getTreeRoot());
//
//		// TODO Test that menu/toolbars are generated properly according to whether the tree
//		//	is query rooted or not
//	}
//
//	@Test
//	public void testCreateAndExecuteIQueryResultsResultsTreeNode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCreateAndExecuteIQueryResults() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetSelection() {
//		populateTree();
//		//		IStructuredSelection selection = ;
//		//		view.setSelection(selection);
//		//		assertEquals("Selection", selection, view.getSelection());
//		fail("Not yet implemented");
//	}
//
//	//	@Test
//	//	public void testDoEditQuery() {
//	// I'm pretty sure this test is impossible... we need to drive a dialog to 
//	// see if this works... perhaps there should be a test EditQueryDialog instead?
//	//		fail("Not yet implemented");
//	//	}
//
//	@Test
//	public void testDoReExecuteAction() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDoDeleteQueryAction() {
//		populateTree();
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsRootTree() {
//		ResultsTreeNode n = view.getTreeRoot();
//		if (n instanceof QueryNode) {
//			assertFalse("Should be query node", view.isRootTree());
//		} else {
//
//		}
//	}
//
//	@Test
//	public void testCreateNewTreeView() throws Exception {
//		Object fbClass = queryAndReturnFirst("class(?X)", "?X");
//		QueryNode n = new QueryNode(new QueryResults(fbClass, "child(!this,?M),method(?M)", "Methods"));
//
//		JQueryTreeView newView = view.createNewTreeView(n);
//		assertFalse("Tree roots are the same", n == newView.getTreeRoot());
//
//		// finds the JQueryPart freshly created
//		IWorkbenchPage p = enableJavaPerspective();
//		IViewReference[] vr = p.getViewReferences();
//		boolean found = false;
//		for (int i = 0; i < vr.length; i++) {
//			if (newView.equals(vr[i].getPart(false))) {
//				found = true;
//			}
//		}
//		if (!found) {
//			fail("Can't find created part!");
//		}
//	}
//
//	private JQueryTreeView getTreeView() throws Exception {
//		IWorkbenchPage p = enableJavaPerspective();
//		IViewReference[] vr = p.getViewReferences();
//		boolean found = false;
//
//		for (int i = 0; i < vr.length; i++) {
//			if (("ca.ubc.jquery.gui.JQueryTreeView").equals(vr[i].getId())) {
//				return (JQueryTreeView) vr[i].getPart(true);
//			}
//		}
//		throw new Exception("Can't find any JQueryTreeView");
//	}
//
//	/** 
//	 * Ensure the JavaPerspective exists and enables it
//	 */
//	private IWorkbenchPage enableJavaPerspective() throws Exception {
//		IPerspectiveDescriptor[] pd = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
//		for (int i = 0; i < pd.length; i++) {
//			if (("org.eclipse.jdt.ui.JavaPerspective").equals(pd[i].getId())) {
//				return PlatformUI.getWorkbench().showPerspective(pd[i].getId(), PlatformUI.getWorkbench().getWorkbenchWindows()[0]);
//			}
//		}
//		return null;
//	}
//
//	/** 
//	 * @param root Root of the tree
//	 * @param n Node to search for
//	 * @return true if the given node can be found in the tree somewhere
//	 * below the given root.
//	 */
//	private boolean isInTree(ResultsTreeNode root, ResultsTreeNode n) {
//		Stack x = new Stack();
//
//		x.push(root);
//		while (!x.isEmpty()) {
//			ResultsTreeNode current = (ResultsTreeNode) x.pop();
//			for (Iterator it = current.getChildren().iterator(); it.hasNext();) {
//				ResultsTreeNode p = (ResultsTreeNode) it.next();
//
//				if (p.equals(n)) {
//					return true;
//				} else if (p.hasChildren()) {
//					x.push(p);
//				}
//			}
//		}
//
//		return false;
//	}
//
//	private void populateTree() {
//		view.execute(new QueryNode(new QueryResults(JQueryAPI.getFactBase(), "package(?P),child(?P,?CU),child(?CU,?C)", "TestTree")));
//	}
//}
