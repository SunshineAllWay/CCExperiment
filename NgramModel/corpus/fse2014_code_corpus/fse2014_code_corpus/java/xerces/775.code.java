package dom.traversal;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
public class AllTests {
    public static void main(String[] args) {
        TestRunner.run(AllTests.suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for the Element Traversal API.");
        suite.addTestSuite(BasicTest.class);
        suite.addTestSuite(ComplexTest.class);
        return suite;
    }
}
