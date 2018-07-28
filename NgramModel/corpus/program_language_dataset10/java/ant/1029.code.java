package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class IsFailureTest extends BuildFileTest {
    public IsFailureTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/isfailure.xml");
    }
    public void testIsFailure() {
       executeTarget("testisfailure");
    }
}
