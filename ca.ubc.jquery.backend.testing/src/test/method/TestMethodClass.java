package test.method;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.SortedMap;

import test.*;
import test.inheritance.*;

public class TestMethodClass {
	public TestMethodClass(int x) {
	}

	public TestMethodClass(float a, float b) {
	}

	public static boolean booleanResult() {
		return false;
	}

	public static int intResult() {
		return 1;
	}

	public static String stringResult() {
		return "Hello World!";
	}

	public void paramsTest(SortedMap s, int x, float[] y, List l, String[] a,
			Object b, double d, Iterator i, Map m, HashMap h) {
	}

	public void usageTest(TestClass x, TestInterface2 y) {
	}

	public void usageInstanceOf(Object o) {
		if (o instanceof TestClass1) {
		} else if (o instanceof TestInterface1) {
		} else if (o instanceof TestMethodClass) {
		}
	}
}
