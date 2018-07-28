package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
public class LoadFileTest extends BuildFileTest {
    public LoadFileTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/loadfile.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testNoSourcefileDefined() {
        expectBuildException("testNoSourcefileDefined",
                "source file not defined");
    }
    public void testNoPropertyDefined() {
        expectBuildException("testNoPropertyDefined",
                "output property not defined");
    }
    public void testNoSourcefilefound() {
        expectBuildExceptionContaining("testNoSourcefilefound",
                "File not found", " doesn't exist");
    }
    public void testFailOnError()
            throws BuildException {
        expectPropertyUnset("testFailOnError","testFailOnError");
    }
    public void testLoadAFile()
            throws BuildException {
        executeTarget("testLoadAFile");
        if(project.getProperty("testLoadAFile").indexOf("eh?")<0) {
            fail("property is not all in the file");
        }
    }
    public void testLoadAFileEnc()
            throws BuildException {
        executeTarget("testLoadAFileEnc");
        if(project.getProperty("testLoadAFileEnc")==null) {
            fail("file load failed");
        }
    }
    public void testEvalProps()
            throws BuildException {
        executeTarget("testEvalProps");
        if(project.getProperty("testEvalProps").indexOf("rain")<0) {
            fail("property eval broken");
        }
    }
    public void testFilterChain()
            throws BuildException {
        executeTarget("testFilterChain");
        if(project.getProperty("testFilterChain").indexOf("World!")<0) {
            fail("Filter Chain broken");
        }
    }
    public final void testStripJavaComments()
            throws BuildException {
        executeTarget("testStripJavaComments");
        final String expected = project.getProperty("expected");
        final String generated = project.getProperty("testStripJavaComments");
        assertEquals(expected, generated);
    }
    public void testOneLine()
            throws BuildException {
            expectPropertySet("testOneLine","testOneLine","1,2,3,4");
    }
}
