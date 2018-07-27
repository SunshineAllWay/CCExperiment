package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class TypeFoundTest extends BuildFileTest {
    public TypeFoundTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/typefound.xml");
    }
    public void testTask() {
        expectPropertySet("testTask", "testTask");
    }
    public void testUndefined() {
        expectBuildExceptionContaining("testUndefined","left out the name attribute", "No type specified");
    }
    public void testTaskThatIsntDefined() {
        expectPropertyUnset("testTaskThatIsntDefined", "testTaskThatIsntDefined");
    }
    public void testTaskThatDoesntReallyExist() {
        expectPropertyUnset("testTaskThatDoesntReallyExist", "testTaskThatDoesntReallyExist");
    }
    public void testType() {
        expectPropertySet("testType", "testType");
    }
    public void testPreset() {
        expectPropertySet("testPreset", "testPreset");
    }
    public void testMacro() {
        expectPropertySet("testMacro", "testMacro");
    }
}
