package org.apache.tools.ant;
import org.apache.tools.ant.BuildFileTest;
public class DispatchTaskTest extends BuildFileTest {
    public DispatchTaskTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/dispatch/dispatch.xml");
    }
    public void testDisp() {
        expectBuildException("disp", "list");
    }
}
