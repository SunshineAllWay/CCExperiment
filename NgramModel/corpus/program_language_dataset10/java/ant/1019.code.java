package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
public class WarTest extends BuildFileTest {
    public static final String TEST_BUILD_FILE
        = "src/etc/testcases/taskdefs/war.xml";
    public WarTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TEST_BUILD_FILE);
    }
    public void tearDown() {
        executeTarget("clean");
    }
    public void testLibRefs() {
        executeTarget("testlibrefs");
        File f = getProject().resolveFile("working/WEB-INF/lib/war.xml");
        assertTrue("File has been put into lib", f.exists());
    }
}
