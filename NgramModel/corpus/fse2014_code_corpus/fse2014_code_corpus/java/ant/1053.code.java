package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
public class ReplaceRegExpTest extends BuildFileTest {
    private static final String PROJECT_PATH = "src/etc/testcases/taskdefs/optional";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public ReplaceRegExpTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(PROJECT_PATH + "/replaceregexp.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testReplace() throws IOException {
        Properties original = new Properties();
        FileInputStream propsFile = null;
        try {
            propsFile = new FileInputStream(new File(System.getProperty("root"), PROJECT_PATH + "/replaceregexp.properties"));
            original.load(propsFile);
        } finally {
            if (propsFile != null) {
                propsFile.close();
                propsFile = null;
            }
        }
        assertEquals("Def", original.get("OldAbc"));
        executeTarget("testReplace");
        Properties after = new Properties();
        try {
            propsFile = new FileInputStream(new File(System.getProperty("root"), PROJECT_PATH + "/test.properties"));
            after.load(propsFile);
        } finally {
            if (propsFile != null) {
                propsFile.close();
                propsFile = null;
            }
        }
        assertNull(after.get("OldAbc"));
        assertEquals("AbcDef", after.get("NewProp"));
    }
    public void testDirectoryDateDoesNotChange() {
        executeTarget("touchDirectory");
        File myFile = new File(System.getProperty("root"), PROJECT_PATH + "/" + getProject().getProperty("tmpregexp"));
        long timeStampBefore = myFile.lastModified();
        executeTarget("testDirectoryDateDoesNotChange");
        long timeStampAfter = myFile.lastModified();
        assertEquals("directory date should not change",
            timeStampBefore, timeStampAfter);
    }
    public void testDontAddNewline1() throws IOException {
        executeTarget("testDontAddNewline1");
        assertTrue("Files match",
                   FILE_UTILS
                   .contentEquals(new File(System.getProperty("root"), PROJECT_PATH + "/test.properties"),
                                  new File(System.getProperty("root"), PROJECT_PATH + "/replaceregexp2.result.properties")));
    }
    public void testDontAddNewline2() throws IOException {
        executeTarget("testDontAddNewline2");
        assertTrue("Files match",
                   FILE_UTILS
                   .contentEquals(new File(System.getProperty("root"), PROJECT_PATH + "/test.properties"),
                                  new File(System.getProperty("root"), PROJECT_PATH + "/replaceregexp2.result.properties")));
    }
    public void testNoPreserveLastModified() throws Exception {
        executeTarget("lastModifiedSetup");
        String tmpdir = project.getProperty("tmpregexp");
        long ts1 = new File(tmpdir, "test.txt").lastModified();
        Thread.sleep(3000);
        executeTarget("testNoPreserve");
        assertTrue(ts1 < new File(tmpdir, "test.txt").lastModified());
    }
    public void testPreserveLastModified() throws Exception {
        executeTarget("lastModifiedSetup");
        String tmpdir = project.getProperty("tmpregexp");
        long ts1 = new File(tmpdir, "test.txt").lastModified();
        Thread.sleep(3000);
        executeTarget("testPreserve");
        assertTrue(ts1 == new File(tmpdir, "test.txt").lastModified());
    }
}
