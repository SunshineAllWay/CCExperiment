package schema.config;
import junit.framework.Test;
import junit.framework.TestSuite;
public class AllTests {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for various schema validation configurations.");
        suite.addTestSuite(BasicTest.class);
        suite.addTestSuite(RootTypeDefinitionTest.class);
        suite.addTestSuite(RootSimpleTypeDefinitionTest.class);
        suite.addTestSuite(IgnoreXSIType_C_A_Test.class);
        suite.addTestSuite(IgnoreXSIType_C_C_Test.class);
        suite.addTestSuite(IgnoreXSIType_A_A_Test.class);
        suite.addTestSuite(IgnoreXSIType_A_C_Test.class);
        suite.addTestSuite(IgnoreXSIType_C_AC_Test.class);
        suite.addTestSuite(IgnoreXSIType_C_CA_Test.class);
        suite.addTestSuite(IdIdrefCheckingTest.class);
        suite.addTestSuite(UnparsedEntityCheckingTest.class);
        suite.addTestSuite(IdentityConstraintCheckingTest.class);
        suite.addTestSuite(UseGrammarPoolOnly_True_Test.class);
        suite.addTestSuite(UseGrammarPoolOnly_False_Test.class);
        suite.addTestSuite(FixedAttrTest.class);
        suite.addTestSuite(FeaturePropagationTest.class);
        return suite;
    }
}
