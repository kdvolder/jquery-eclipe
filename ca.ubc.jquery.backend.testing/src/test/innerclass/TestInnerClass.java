package test.innerclass;

public class TestInnerClass {
	private interface Privateinterface {
		public void m();
	}

	private class Privateinnerclass implements Privateinterface {
		public void m() {
		}
	}

	protected class Protectedinnerclass implements Privateinterface {
		public void m() {
		}
	}

	public class Publicinnerclass implements Privateinterface {
		public void m() {
		}
	}

	public TestInnerClass() {
		Privateinnerclass a = new Privateinnerclass();
		Protectedinnerclass b = new Protectedinnerclass();
		Publicinnerclass c = new Publicinnerclass();

		a.m();
		b.m();
		c.m();
		
		Privateinterface d = a;
		d.m();
	}
}
