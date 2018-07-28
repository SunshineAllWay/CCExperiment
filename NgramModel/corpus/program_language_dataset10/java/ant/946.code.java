package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class BUnzip2Test extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public BUnzip2Test(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/bunzip2.xml");
        executeTarget("prepare");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testRealTest() throws java.io.IOException {
        testRealTest("realTest");
    }
    public void testRealTestWithResource() throws java.io.IOException {
        testRealTest("realTestWithResource");
    }
    private void testRealTest(String target) throws java.io.IOException {
        executeTarget(target);
        assertTrue("File content mismatch after bunzip2",
            FILE_UTILS.contentEquals(project.resolveFile("expected/asf-logo-huge.tar"),
                                    project.resolveFile("asf-logo-huge.tar")));
    }
    public void testDocumentationClaimsOnCopy() throws java.io.IOException {
        testRealTest("testDocumentationClaimsOnCopy");
    }
}
