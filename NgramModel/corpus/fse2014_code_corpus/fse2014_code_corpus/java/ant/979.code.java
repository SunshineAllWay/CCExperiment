package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
public class JavadocTest extends BuildFileTest {
    public JavadocTest(String name) {
        super(name);
    }
    private static final String BUILD_PATH = "src/etc/testcases/taskdefs/javadoc/";
    private static final String BUILD_FILENAME = "javadoc.xml";
    private static final String BUILD_FILE = BUILD_PATH + BUILD_FILENAME;
    protected void setUp() throws Exception {
        super.setUp();
        configureProject(BUILD_FILE);
    }
    public void testDirsetPath() throws Exception {
        executeTarget("dirsetPath");
    }
    public void testDirsetPathWithoutPackagenames() throws Exception {
        try {
            executeTarget("dirsetPathWithoutPackagenames");
        } catch (BuildException e) {
            fail("Contents of path should be picked up without specifying package names: " + e);
        }
    }
    public void testNestedDirsetPath() throws Exception {
        executeTarget("nestedDirsetPath");
    }
    public void testFilesetPath() throws Exception {
        try {
            executeTarget("filesetPath");
        } catch (BuildException e) {
            fail("A path can contain filesets: " + e);
        }
    }
    public void testNestedFilesetPath() throws Exception {
        try {
            executeTarget("nestedFilesetPath");
        } catch (BuildException e) {
            fail("A path can contain nested filesets: " + e);
        }
    }
    public void testFilelistPath() throws Exception {
        try {
            executeTarget("filelistPath");
        } catch (BuildException e) {
            fail("A path can contain filelists: " + e);
        }
    }
    public void testNestedFilelistPath() throws Exception {
        try {
            executeTarget("nestedFilelistPath");
        } catch (BuildException e) {
            fail("A path can contain nested filelists: " + e);
        }
    }
    public void testPathelementPath() throws Exception {
        executeTarget("pathelementPath");
    }
    public void testPathelementLocationPath() throws Exception {
        try {
            executeTarget("pathelementLocationPath");
        } catch (BuildException e) {
            fail("A path can contain pathelements pointing to a file: " + e);
        }
    }
    public void testNestedSource() throws Exception {
        executeTarget("nestedSource");
    }
    public void testNestedFilesetRef() throws Exception {
        executeTarget("nestedFilesetRef");
    }
    public void testNestedFilesetRefInPath() throws Exception {
        executeTarget("nestedFilesetRefInPath");
    }
    public void testNestedFilesetNoPatterns() throws Exception {
        executeTarget("nestedFilesetNoPatterns");
    }
    public void testDoublyNestedFileset() throws Exception {
        executeTarget("doublyNestedFileset");
    }
    public void testDoublyNestedFilesetNoPatterns() throws Exception {
        executeTarget("doublyNestedFilesetNoPatterns");
    }
    public void testNonJavaIncludes() throws Exception { 
        executeTarget("nonJavaIncludes");
    }
}
