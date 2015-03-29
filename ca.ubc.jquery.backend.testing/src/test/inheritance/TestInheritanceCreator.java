package test.inheritance;

public class TestInheritanceCreator {
	{
		new TestClass1();
		new TestClass12();
		new TestAbClass1() {
			public int m2() {
				return 0;
			}

			public void m3(int x, int y) {
			}
		};

		Object x = null;
		if (x instanceof TestClass1) {
			x = null;
		}

		new TestClassChild12();
		new TestInterface2() {
			public int n1() {
				return 1;
			}

			public int n2() {
				return 0;
			}

			public void n3(int x, int y) {
			}
		};
	}
}
