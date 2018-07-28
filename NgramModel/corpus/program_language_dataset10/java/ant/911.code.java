package org.apache.tools.ant;
public class ExtendedTaskdefTest extends BuildFileTest {
    public ExtendedTaskdefTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/extended-taskdef.xml");
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        executeTarget("teardown");
    }
    public void testRun() throws Exception {
        expectBuildExceptionContaining("testRun",
                "exception thrown by the subclass",
                "executing the Foo task");
    }
    public void testRun2() throws Exception {
        expectBuildExceptionContaining("testRun2",
                "exception thrown by the subclass",
                "executing the Foo task");
    }
}
