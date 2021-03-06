package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class GUnzipTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public GUnzipTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/gunzip.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "required argument missing");
    }
    public void test2() {
        expectBuildException("test2", "attribute src invalid");
    }
    public void testRealTest() throws java.io.IOException {
        testRealTest("realTest");
    }
    public void testRealTestWithResource() throws java.io.IOException {
        testRealTest("realTestWithResource");
    }
    private void testRealTest(String target) throws java.io.IOException {
        executeTarget(target);
        assertTrue(FILE_UTILS.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }
    public void testTestGzipTask() throws java.io.IOException {
        testRealTest("testGzipTask");
    }
    public void testDocumentationClaimsOnCopy() throws java.io.IOException {
        testRealTest("testDocumentationClaimsOnCopy");
    }
}
