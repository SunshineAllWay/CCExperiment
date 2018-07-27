package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class Native2AsciiTest extends BuildFileTest {
    private final static String BUILD_XML = 
        "src/etc/testcases/taskdefs/optional/native2ascii/build.xml";
    public Native2AsciiTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(BUILD_XML);
    }
    public void tearDown() {
        executeTarget("tearDown");
    }
    public void testIso8859_1() throws java.io.IOException {
        executeTarget("testIso8859-1");
        File in = getProject().resolveFile("expected/iso8859-1.test");
        File out = getProject().resolveFile("output/iso8859-1.test");
        assertTrue(FileUtils.getFileUtils().contentEquals(in, out, true));
    }
}
