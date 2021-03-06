package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
public class AntlibTest extends BuildFileTest {
    public AntlibTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/antlib.xml");
    }
    private boolean isSharedJVM() {
        String property = System.getProperty("tests.and.ant.share.classloader");
        return property!=null && Project.toBoolean(property);
    }
    public void testAntlibFile() {
        expectLog("antlib.file", "MyTask called");
    }
    public void testAntlibResource() {
        expectLog("antlib.resource", "MyTask called-and-then-MyTask2 called");
    }
    public void testNsCurrent() {
        expectLog("ns.current", "Echo2 inside a macroHello from x:p");
    }
    public void testAntlib_uri() {
        if (isSharedJVM()) {
            executeTarget("antlib_uri");
        }
    }
    public void testAntlib_uri_auto() {
        if (isSharedJVM()) {
            executeTarget("antlib_uri_auto");
        }
    }
    public void testAntlib_uri_auto2() {
        if (isSharedJVM()) {
            executeTarget("antlib_uri_auto2");
        }
    }
    public static class MyTask extends Task {
        public void execute() {
            log("MyTask called");
        }
    }
    public static class MyTask2 extends Task {
        public void execute() {
            log("MyTask2 called");
        }
    }
}
