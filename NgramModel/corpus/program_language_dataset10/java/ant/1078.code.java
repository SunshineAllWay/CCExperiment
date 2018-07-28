package org.apache.tools.ant.taskdefs.optional.junit;
import org.apache.tools.ant.BuildFileTest;
public class TearDownOnVmCrashTest extends BuildFileTest {
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/junit/teardownlistener.xml");
    }
    public void testNoTeardown() {
        expectPropertySet("testNoTeardown", "error");
        assertOutputNotContaining(null, "tearDown called on Timeout");
    }
    public void testTeardown() {
        expectPropertySet("testTeardown", "error");
        assertOutputContaining("tearDown called on Timeout");
    }
}