package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import java.io.IOException;
public class RecorderTest extends BuildFileTest {
    private static final String REC_IN = "recorder/";
    private static final String REC_DIR = "recorder-out/";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public RecorderTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/recorder.xml");
        executeTarget("prepare");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testNoAppend() throws IOException {
        executeTarget("noappend");
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest1.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest1.log"), true));
    }
    public void testAppend() throws IOException {
        executeTarget("append");
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest2.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest2.log"), true));
    }
    public void testRestart() throws IOException {
        executeTarget("restart");
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest3.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest3.log"), true));
    }
    public void testDeleteRestart() throws IOException {
        executeTarget("deleterestart");
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest4.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest4.log"), true));
    }
    public void testSubBuild() throws IOException {
        executeTarget("subbuild");
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest5.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest5.log"), true));
        assertTrue(FILE_UTILS
                   .contentEquals(project.resolveFile(REC_IN
                                                      + "rectest6.result"),
                                  project.resolveFile(REC_DIR
                                                      + "rectest6.log"), true));
    }
}
