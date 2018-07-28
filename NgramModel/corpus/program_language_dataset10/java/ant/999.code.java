package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.rmic.RmicAdapterFactory;
import org.apache.tools.ant.taskdefs.rmic.DefaultRmicAdapter;
public class RmicAdvancedTest extends BuildFileTest {
    public RmicAdvancedTest(String name) {
        super(name);
    }
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/rmic/";
    public void setUp() throws Exception {
        super.setUp();
        configureProject(TASKDEFS_DIR + "rmic.xml");
    }
    public void tearDown() {
        executeTarget("teardown");
    }
    public void testDefault() throws Exception {
        executeTarget("testDefault");
    }
    public void testDefaultDest() throws Exception {
        executeTarget("testDefaultDest");
    }
    public void testEmpty() throws Exception {
        executeTarget("testEmpty");
    }
    public void testEmptyDest() throws Exception {
        executeTarget("testEmptyDest");
    }
    public void testRmic() throws Exception {
        executeTarget("testRmic");
    }
    public void testRmicDest() throws Exception {
        executeTarget("testRmicDest");
    }
    public void testRmicJArg() throws Exception {
        executeTarget("testRmicJArg");
    }
    public void testRmicJArgDest() throws Exception {
        executeTarget("testRmicJArgDest");
    }
    public void testKaffe() throws Exception {
        executeTarget("testKaffe");
    }
    public void testKaffeDest() throws Exception {
        executeTarget("testKaffeDest");
    }
    public void XtestWlrmic() throws Exception {
        executeTarget("testWlrmic");
    }
    public void XtestWlrmicJArg() throws Exception {
        executeTarget("testWlrmicJArg");
    }
    public void NotestForking() throws Exception {
        executeTarget("testForking");
    }
    public void testForkingAntClasspath() throws Exception {
        executeTarget("testForkingAntClasspath");
    }
    public void testForkingAntClasspathDest() throws Exception {
        executeTarget("testForkingAntClasspathDest");
    }
    public void testAntClasspath() throws Exception {
        executeTarget("testAntClasspath");
    }
    public void testAntClasspathDest() throws Exception {
        executeTarget("testAntClasspathDest");
    }
    public void testBadName() throws Exception {
        expectBuildExceptionContaining("testBadName",
                "compiler not known",
                RmicAdapterFactory.ERROR_UNKNOWN_COMPILER);
    }
    public void testExplicitClass() throws Exception {
        executeTarget("testExplicitClass");
    }
    public void testWrongClass() throws Exception {
        expectBuildExceptionContaining("testWrongClass",
                "class not an RMIC adapter",
                RmicAdapterFactory.ERROR_NOT_RMIC_ADAPTER);
    }
    public void testDefaultBadClass() throws Exception {
        expectBuildExceptionContaining("testDefaultBadClass",
                "expected the class to fail",
                Rmic.ERROR_RMIC_FAILED);
        assertLogContaining("unimplemented.class");
    }
    public void testMagicProperty() throws Exception {
        expectBuildExceptionContaining("testMagicProperty",
                "magic property not working",
                RmicAdapterFactory.ERROR_UNKNOWN_COMPILER);
    }
    public void testMagicPropertyOverridesEmptyString() throws Exception {
        expectBuildExceptionContaining("testMagicPropertyOverridesEmptyString",
                "magic property not working",
                RmicAdapterFactory.ERROR_UNKNOWN_COMPILER);
    }
    public void testMagicPropertyIsEmptyString() throws Exception {
        executeTarget("testMagicPropertyIsEmptyString");
    }
    public void NotestFailingAdapter() throws Exception {
        expectBuildExceptionContaining("testFailingAdapter",
                "expected failures to propagate",
                Rmic.ERROR_RMIC_FAILED);
    }
    public void testVersion11() throws Exception {
        executeTarget("testVersion11");
    }
    public void testVersion11Dest() throws Exception {
        executeTarget("testVersion11Dest");
    }
    public void testVersion12() throws Exception {
        executeTarget("testVersion12");
    }
    public void testVersion12Dest() throws Exception {
        executeTarget("testVersion12Dest");
    }
    public void testVersionCompat() throws Exception {
        executeTarget("testVersionCompat");
    }
    public void testVersionCompatDest() throws Exception {
        executeTarget("testVersionCompatDest");
    }
    public void testXnew() throws Exception {
        executeTarget("testXnew");
    }
    public void testXnewDest() throws Exception {
        executeTarget("testXnewDest");
    }
    public void testXnewForked() throws Exception {
        executeTarget("testXnewForked");
    }
    public void testXnewForkedDest() throws Exception {
        executeTarget("testXnewForkedDest");
    }
    public void testXnewCompiler() throws Exception {
        executeTarget("testXnewCompiler");
    }
    public void testXnewCompilerDest() throws Exception {
        executeTarget("testXnewCompilerDest");
    }
    public void testIDL() throws Exception {
        executeTarget("testIDL");
    }
    public void testIDLDest() throws Exception {
        executeTarget("testIDLDest");
    }
    public void testIIOP() throws Exception {
        executeTarget("testIIOP");
    }
    public void testIIOPDest() throws Exception {
        executeTarget("testIIOPDest");
    }
    public static class FailingRmicAdapter extends DefaultRmicAdapter {
        public static final String LOG_MESSAGE = "hello from FailingRmicAdapter";
        public boolean execute() throws BuildException {
            getRmic().log(LOG_MESSAGE);
            return false;
        }
    }
}
