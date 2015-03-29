package test;

import test.innerclass.*;
import test.method.*;
import test.exception.*;

public class TestClass {
	public int pubIntField;

	protected float proFloatField;

	private Object privObjectField;

	private String privStringField;

	private TestInnerClass privTestInnerClassField;

	public static int psIntField;

	public static final int psfIntField = 5;

	{
		pubIntField = 0;
		proFloatField = 0.0f;
		privObjectField = new Object[15];
		privStringField = "";
		privTestInnerClassField = new TestInnerClass();
	}

	public TestClass() {
		pubIntField = 0;
		proFloatField = 0.0f;
		privObjectField = new Object[15];
		privStringField = "";
		privTestInnerClassField = new TestInnerClass();
	}

	public void publicTest() {
		privateTest();
		protectedTest();

		if (pubIntField > 0)
			pubIntField++;
	}

	protected void protectedTest() {
		privateTest();

		if (proFloatField > 0)
			proFloatField += 1;
	}

	private void privateTest() {
		publicTest();

		if (privObjectField != null)
			privObjectField = null;
	}

	public void expressionTest() {
		System.out.println("Hello world!");
		int a = 5;

		if (a == 6) {
			System.out.println("Do nothing...");
			a = 5;
		} else if (a == 4) {
			System.out.println("A==4");
			a = 3;
		} else {
			System.out.println();
			(new TestInnerClass()).new Publicinnerclass();
			a = 0;
		}

		try {
			protectedTest();
			if (5 == 5) {
				for (int i = 0; i < 5; i++) {
					protectedTest();
					privateTest();
				}

				while (a != 5) {
					TestMethodClass.intResult();
				}
				if (1 == 0) {
					if (0 == 1)
						privateTest();
				}

				for (int j = 15; j > 0 && TestMethodClass.booleanResult(); j--)
					TestMethodClass.intResult();
				TestMethodClass.intResult();
			}

			ExceptionThrower.throwException();
		} catch (TestException e) {
			TestMethodClass.stringResult();
		} catch (Exception e) {
			publicTest();
			privateTest();
			publicTest();
		} finally {
			publicTest();
		}

		do
			TestMethodClass.stringResult();
		while (TestMethodClass.booleanResult() && (1 > 0)
				&& TestMethodClass.booleanResult());

		switch (1) {
		case 1:
			privateTest();
			break;
		case 2:
			protectedTest();
			privateTest();
			break;
		case 3: {
			System.out.println();
			break;
		}
		default:
			TestMethodClass.booleanResult();
		}

		boolean b;
		b = (TestMethodClass.booleanResult());
		b = ((TestMethodClass.booleanResult()));
	}
}
