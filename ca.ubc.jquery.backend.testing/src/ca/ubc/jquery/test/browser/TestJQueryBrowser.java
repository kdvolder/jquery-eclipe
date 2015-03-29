package ca.ubc.jquery.test.browser;

import junit.framework.TestCase;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestJQueryBrowser extends TestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/** 
	 * Ensure the JavaPerspective exists and enables it
	 */
	private IWorkbenchPage enableJavaPerspective() throws Exception {
		IPerspectiveDescriptor[] pd = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
		for (int i = 0; i < pd.length; i++) {
			if (("org.eclipse.jdt.ui.JavaPerspective").equals(pd[i].getId())) {
				return PlatformUI.getWorkbench().showPerspective(pd[i].getId(), PlatformUI.getWorkbench().getWorkbenchWindows()[0]);
			}
		}
		return null;
	}

	/**
	 * Ensures that the JQuery view is displayed by default in the JavaPerspective
	 */
	@Test
	public void testJQueryViewInJavaPerspective() throws Exception {
		IWorkbenchPage p = enableJavaPerspective();
		IViewReference[] vr = p.getViewReferences();
		boolean found = false;

		for (int i = 0; i < vr.length; i++) {
			if (("ca.ubc.jquery.gui.JQueryTreeView").equals(vr[i].getId())) {
				found = true;
			}
		}

		assertTrue("Finding view in Java Perspective", found);
	}
}
