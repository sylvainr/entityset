package test.utils;

import static org.junit.Assert.fail;

public class ExceptionAsserter 
{
	public interface ExceptionAssert {
		public void doAction() throws Exception;
	}
	
	public static void assertException(Class<?> throwableClass, ExceptionAssert code) throws Exception
	{
		boolean ok = false;
		
		try {
			code.doAction();
		} 
		catch(Exception t)
		{
			ok = true;
			if (t.getClass().equals(throwableClass) == false) {
				throw t;
			}
		}
		
		if (ok == false)
			fail("No exception thrown at all when expecting " + throwableClass.getName());
	}
}