package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
public class MakeUrlTest extends BuildFileTest {
    public MakeUrlTest(String s) {
        super(s);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/makeurl.xml");
    }
    public void testEmpty() {
        expectBuildExceptionContaining("testEmpty", "missing property", "property");
    }
    public void testNoProperty() {
        expectBuildExceptionContaining("testNoProperty", "missing property", "property");
    }
    public void testNoFile() {
        expectBuildExceptionContaining("testNoFile", "missing file", "file");
    }
    public void testValidation() {
        expectBuildExceptionContaining("testValidation", MakeUrl.ERROR_MISSING_FILE, "file");
    }
    public void testWorks() {
        executeTarget("testWorks");
        assertPropertyContains("testWorks", "file:");
        assertPropertyContains("testWorks", "/foo");
    }
    public void testIllegalChars() {
        executeTarget("testIllegalChars");
        assertPropertyContains("testIllegalChars", "file:");
        assertPropertyContains("testIllegalChars", "fo%20o%25");
    }
    public void testRoundTrip() throws IOException {
        executeTarget("testRoundTrip");
        assertPropertyContains("testRoundTrip", "file:");
        String property = getProperty("testRoundTrip");
        URL url = new URL(property);
        InputStream instream = url.openStream();
        instream.close();
    }
    public void testIllegalCombinations() {
        executeTarget("testIllegalCombinations");
        assertPropertyContains("testIllegalCombinations", "/foo");
        assertPropertyContains("testIllegalCombinations", ".xml");
    }
    public void testFileset() {
        executeTarget("testFileset");
        assertPropertyContains("testFileset", ".xml ");
        assertPropertyEndsWith("testFileset", ".xml");
    }
    public void testFilesetSeparator() {
        executeTarget("testFilesetSeparator");
        assertPropertyContains("testFilesetSeparator", ".xml\",\"");
        assertPropertyEndsWith("testFilesetSeparator", ".xml");
    }
    public void testPath() {
        executeTarget("testPath");
        assertPropertyContains("testPath", "makeurl.xml");
    }
    private void assertPropertyEndsWith(String property, String ending) {
        String result = getProperty(property);
        String substring = result.substring(result.length() - ending.length());
        assertEquals(ending, substring);
    }
    protected void assertPropertyContains(String property, String contains) {
        String result = getProperty(property);
        assertTrue("expected " + contains + " in " + result,
                result != null && result.indexOf(contains) >= 0);
    }
    protected String getProperty(String property) {
        return project.getProperty(property);
    }
}
