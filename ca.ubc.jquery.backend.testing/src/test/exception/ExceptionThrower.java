package test.exception;

public class ExceptionThrower {
	public ExceptionThrower() {
	}

	public void throwMe() throws TestException {
		throw new TestException("Testing...");
	}
	
	public static void throwException() throws TestException {
		throw new TestException("Testing...");
	}
	
	public void throwNormal() throws Exception {
		throw new Exception();
	}
}
