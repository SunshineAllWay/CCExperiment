package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class NiceTest extends BuildFileTest {
    public NiceTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/nice.xml");
    }
    public void testNoop() {
        executeTarget("noop");
    }
    public void testCurrent() {
        executeTarget("current");
    }
    public void testFaster() {
        executeTarget("faster");
    }
    public void testSlower() {
        executeTarget("slower");
    }
    public void testTooSlow() {
        expectBuildExceptionContaining(
                "too_slow","out of range","out of the range 1-10");
    }
    public void testTooFast() {
        expectBuildExceptionContaining(
                "too_fast", "out of range", "out of the range 1-10");
    }
}
