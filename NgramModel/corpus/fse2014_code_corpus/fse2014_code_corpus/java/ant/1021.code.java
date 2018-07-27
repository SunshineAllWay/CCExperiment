package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Task;
public class XmlnsTest extends BuildFileTest {
    public XmlnsTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/xmlns.xml");
    }
    public void testXmlns() {
        expectLog("xmlns", "MyTask called");
    }
    public void testXmlnsFile() {
        expectLog("xmlns.file", "MyTask called");
    }
    public void testCore() {
        expectLog("core", "MyTask called");
    }
    public void testExcluded() {
        expectBuildExceptionContaining(
            "excluded", "excluded uri",
            "Attempt to use a reserved URI ant:notallowed");
    }
    public void testOther() {
        expectLog("other", "a message");
    }
    public void testNsAttributes() {
        expectLog("ns.attributes", "hello world");
    }
    public static class MyTask extends Task {
        public void execute() {
            log("MyTask called");
        }
    }
}
