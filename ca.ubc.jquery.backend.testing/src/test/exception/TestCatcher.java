package test.exception;

public class TestCatcher {
	public void catcher() {
		try {
			ExceptionThrower e = new ExceptionThrower();
			e.throwMe();
		} catch (TestException e) {
			m();
			n();
		} catch (Exception e) {
			m();
		}
	}

	private void m() {
	}

	private void n() {
	}
}
