package test.inheritance;

import test.innerclass.*;

public abstract class TestAbClass1 implements TestInterface1 {
	private TestInnerClass c;

	public int f;

	public int m1() {
		if (c == null)
			return 0;
		else
			return 1;
	}
}
