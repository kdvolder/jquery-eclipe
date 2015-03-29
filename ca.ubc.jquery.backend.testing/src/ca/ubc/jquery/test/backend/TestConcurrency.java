package ca.ubc.jquery.test.backend;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryResultSet;

public class TestConcurrency {

	final private static int NUM_THREADS = 100;

	final private static int NUM_FILES = 5;

	final private static double MODIFICATION_RATE = 0.85;

	private static TestProject project;

	private static void addClass(String name) throws Exception {
		project.addSourceFile(name + ".java", "public class " + name + " {private void m0(){} public void m1(int p1){} private int f1;}");
	}

	private static void addClass2(String name) throws Exception {
		project.addSourceFile(name + ".java", "public class " + name + " {private void m0(){} public void m1(int p1){int y;} private int f1;}");
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		project = new TestProject();

		// add some classes to the database
		for (int i = 0; i < NUM_FILES; i++) {
			addClass("SomeClass" + i);
		}
		JQueryAPI.getFactBase().setFactBaseByName(project.getSourceCode());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JQueryAPI.shutdown();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConcurrentQueries() throws Throwable {
		TesterThread[] p = new TesterThread[NUM_THREADS];
		{
			JQuery q = JQueryAPI.createQuery("class(?X)");
			JQueryResultSet rs = q.execute();
			while (rs.hasNext()) {
				rs.next();
			}
		}

		for (int i = 0; i < p.length; i++) {
			p[i] = new FileUpdateThread("UpdateThread" + i);
			//			if (Math.random() > 0.75) {
			//				p[i] = new FileUpdateThread("UpdateThread"+i);
			//			} else {
			//				p[i] = new QueryThread("QueryThread"+i);
			//			}
		}

		for (int i = 0; i < p.length; i++) {
			p[i].start();
		}

		//		{
		//			for (int i = 0; i < NUM_FILES; i++) {
		//				if (Math.random() > MODIFICATION_RATE) {
		//					// perform a random modification
		//					if (Math.random() < 0.5) {
		//						addClass2("SomeClass" + i);
		//					} else {
		//						addClass("SomeClass" + i);
		//					}
		//					JQuery q = JQueryAPI.createQuery("class(?X)");
		//					JQueryResultSet rs = q.execute();
		//					while (rs.hasNext()) {
		//						rs.next();
		//					}
		//				}
		//			}
		//		}

		for (int i = 0; i < p.length; i++) {
			p[i].join();
		}

		for (int i = 0; i < p.length; i++) {
			if (p[i].crash != null) {
				throw p[i].crash;
			}
		}

		// success...
	}

	private class TesterThread extends Thread {

		Throwable crash = null;

		private TesterThread(String string) {
			super(string);
		}

	}

	private class FileUpdateThread extends TesterThread {
		public FileUpdateThread(String s) {
			super(s);
		}

		public void run() {
			for (int i = 0; i < NUM_FILES; i++) {
				if (Math.random() > MODIFICATION_RATE) {
					try {
						// perform a random modification
						if (Math.random() < 0.5) {
							addClass2("SomeClass" + i);
						} else {
							addClass("SomeClass" + i);
						}
						JQuery q = JQueryAPI.createQuery("class(?X)");
						JQueryResultSet rs = q.execute();
						while (rs.hasNext()) {
							rs.next();
						}
					} catch (Exception e) {
						crash = e;
						System.err.println(e);
					}
				}
			}
		}
	}

	//	static Lock lock = new ReentrantLock();

	private class QueryThread extends TesterThread {
		public QueryThread(String s) {
			super(s);
		}

		public void run() {
			try {
				//				lock.lock();
				JQuery q = JQueryAPI.createQuery("class(?X)");
				JQueryResultSet rs = q.execute();
				while (rs.hasNext()) {
					rs.next();
				}
			} catch (Exception e) {
				crash = e;
				System.err.println(e.getMessage());
			} finally {
				//				lock.unlock();
			}
		}
	}
}
