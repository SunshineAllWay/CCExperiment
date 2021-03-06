package schema.annotations;
import junit.framework.Test;
import junit.framework.TestSuite;
public class AllTests {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for Schema Annotations");
        suite.addTestSuite(XSAttributeGroupAnnotationsTest.class);
        suite.addTestSuite(XSNotationAnnotationsTest.class);
        suite.addTestSuite(XSElementAnnotationsTest.class);
        suite.addTestSuite(XSAttributeUseAnnotationsTest.class);
        suite.addTestSuite(XSAttributeAnnotationsTest.class);
        suite.addTestSuite(XSFacetAnnotationsTest.class);
        suite.addTestSuite(XSModelGroupDefinitionAnnotationsTest.class);
        suite.addTestSuite(XSModelGroupAnnotationsTest.class);
        suite.addTestSuite(XSParticleAnnotationsTest.class);
        suite.addTestSuite(XSWildcardAnnotationsTest.class);
        return suite;
    }
}
