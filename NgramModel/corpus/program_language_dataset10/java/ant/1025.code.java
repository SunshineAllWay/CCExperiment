package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class AntVersionTest extends BuildFileTest {
    public AntVersionTest(String name) {
        super(name);
    }
    public void setUp() throws Exception {
        configureProject("src/etc/testcases/taskdefs/conditions/antversion.xml");
    }
    public void testAtLeast() {
        executeTarget("testatleast");
    }
    public void testExactly() {
        executeTarget("testexactly");
    }
}
