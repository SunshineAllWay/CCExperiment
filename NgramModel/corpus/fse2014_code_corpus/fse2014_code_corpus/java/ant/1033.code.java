package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class IsSignedTest extends BuildFileTest {
    public IsSignedTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/issigned.xml");
    }
    public void testPass() {
        executeTarget("pass");
    }
    public void testPassword() {
        executeTarget("password");
    }
    public void testAPassword() {
        executeTarget("apassword");
    }
    public void testAllSigned() {
        executeTarget("allsigned");
    }
}
