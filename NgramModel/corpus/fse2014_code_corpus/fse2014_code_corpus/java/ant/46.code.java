 import junit.framework.TestCase;
public class AssertionTest extends TestCase {
	public AssertionTest(String name) {
		super(name);
	}
	public void testAssertRaised() {
		try {
			assert true == false;
			fail("expected an assertion");
		} catch(AssertionError asserto) {
		}
	}
	public void testAssertNotRaised() {
		assert(2+2==4);
	}
}
