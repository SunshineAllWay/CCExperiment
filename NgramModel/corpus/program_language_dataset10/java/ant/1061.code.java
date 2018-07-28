package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class XsltTest extends BuildFileTest {
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";
    public XsltTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "xslt.xml");
    }
    public void tearDown() {
        executeTarget("teardown");
    }
    public void testCatchNoDtd() throws Exception {
        expectBuildExceptionContaining("testCatchNoDtd",
                                       "expected failure",
                                       null);
    }
    public void testCatalog() throws Exception {
         executeTarget("testCatalog");
    }
    public void testOutputProperty() throws Exception {
      executeTarget("testOutputProperty");
    }
    public void testXMLWithEntitiesInNonAsciiPath() throws Exception {
        executeTarget("testXMLWithEntitiesInNonAsciiPath");
    }
    public void testStyleSheetWithInclude() throws Exception {
        executeTarget("testStyleSheetWithInclude");
        if (getLog().indexOf("java.io.FileNotFoundException") != -1) {
            fail("xsl:include was not found");
        }
    }
}
