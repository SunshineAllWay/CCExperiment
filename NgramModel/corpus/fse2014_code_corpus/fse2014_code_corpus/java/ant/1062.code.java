package org.apache.tools.ant.taskdefs.optional.depend;
import java.util.Hashtable;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
public class DependTest extends BuildFileTest {
    public static final String RESULT_FILESET = "result";
    public static final String TEST_BUILD_FILE
        = "src/etc/testcases/taskdefs/optional/depend/depend.xml";
    public DependTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TEST_BUILD_FILE);
    }
    public void tearDown() {
        executeTarget("clean");
    }
    public void testDirect() {
        executeTarget("testdirect");
        Hashtable files = getResultFiles();
        assertEquals("Depend did not leave correct number of files", 3,
            files.size());
        assertTrue("Result did not contain A.class",
            files.containsKey("A.class"));
        assertTrue("Result did not contain D.class",
            files.containsKey("D.class"));
    }
    public void testClosure() {
        executeTarget("testclosure");
        Hashtable files = getResultFiles();
        assertTrue("Depend did not leave correct number of files", 
            files.size() <= 2);
        assertTrue("Result did not contain D.class",
            files.containsKey("D.class"));
    }
    public void testInner() {
        executeTarget("testinner");
        assertEquals("Depend did not leave correct number of files", 0,
            getResultFiles().size());
    }
    public void testInnerInner() {
        executeTarget("testinnerinner");
        assertEquals("Depend did not leave correct number of files", 0,
            getResultFiles().size());
    }
    public void testNoSource() {
        expectBuildExceptionContaining("testnosource",
            "No source specified", "srcdir attribute must be set");
    }
    public void testEmptySource() {
        expectBuildExceptionContaining("testemptysource",
            "No source specified", "srcdir attribute must be non-empty");
    }
    private Hashtable getResultFiles() {
        FileSet resultFileSet = (FileSet) project.getReference(RESULT_FILESET);
        DirectoryScanner scanner = resultFileSet.getDirectoryScanner(project);
        String[] scannedFiles = scanner.getIncludedFiles();
        Hashtable files = new Hashtable();
        for (int i = 0; i < scannedFiles.length; ++i) {
            files.put(scannedFiles[i], scannedFiles[i]);
        }
        return files;
    }
    public void testInnerClosure() {
        executeTarget("testinnerclosure");
        assertEquals("Depend did not leave correct number of files", 4,
            getResultFiles().size());
    }
    public void testCache() {
        executeTarget("testcache");
    }
    public void testNonPublic() {
        executeTarget("testnonpublic");
        String log = getLog();
        assertTrue("Expected warning about APrivate",
            log.indexOf("The class APrivate in file") != -1);
        assertTrue("but has not been deleted because its source file "
            + "could not be determined",
            log.indexOf("The class APrivate in file") != -1);
    }
}
