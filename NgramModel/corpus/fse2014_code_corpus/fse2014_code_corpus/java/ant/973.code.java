package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class GzipTest extends BuildFileTest {
    public GzipTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/gzip.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument missing");
    }
    public void test2() {
        expectBuildException("test2", "required argument missing");
    }
    public void test3() {
        expectBuildException("test3", "required argument missing");
    }
    public void test4() {
        expectBuildException("test4", "zipfile must not point to a directory");
    }
    public void testGZip(){
        executeTarget("realTest");
        String log = getLog();
        assertTrue("Expecting message starting with 'Building:' but got '"
            + log + "'", log.startsWith("Building:"));
        assertTrue("Expecting message ending with 'asf-logo.gif.gz' but got '"
            + log + "'", log.endsWith("asf-logo.gif.gz"));
    }
    public void testResource(){
        executeTarget("realTestWithResource");
    }
    public void testDateCheck(){
        executeTarget("testDateCheck");
        String log = getLog();
        assertTrue(
            "Expecting message ending with 'asf-logo.gif.gz is up to date.' but got '" + log + "'",
            log.endsWith("asf-logo.gif.gz is up to date."));
    }
    public void tearDown(){
        executeTarget("cleanup");
    }
}
