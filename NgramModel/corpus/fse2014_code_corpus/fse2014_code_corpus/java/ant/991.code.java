package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class PathConvertTest extends BuildFileTest {
    private static final String BUILD_PATH = "src/etc/testcases/taskdefs/";
    private static final String BUILD_FILENAME = "pathconvert.xml";
    private static final String BUILD_FILE = BUILD_PATH + BUILD_FILENAME;
    public PathConvertTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(BUILD_FILE);
    }
    public void testMap() {
        test("testmap");
    }
    public void testMapper() {
        test("testmapper");
    }
    public void testNoTargetOs() {
        executeTarget("testnotargetos");
    }
    private void test(String target) {
        executeTarget(target);
        assertPropertyEquals("result", "test#" + BUILD_FILENAME);
    }
}
