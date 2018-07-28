package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class EchoXMLTest extends BuildFileTest {
    public EchoXMLTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/echoxml.xml");
    }
    public void tearDown() {
        executeTarget("tearDown");
    }
    public void testPass() {
        executeTarget("testPass");
    }
    public void testFail() {
        expectBuildExceptionContaining("testFail", "must fail", "${foo}=bar");
    }
    public void testEmpty() {
        expectBuildExceptionContaining("testEmpty", "must fail", "No nested XML specified");
    }
}
