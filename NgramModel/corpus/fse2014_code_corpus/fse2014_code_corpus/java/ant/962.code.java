package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class DynamicTest extends BuildFileTest {
    public DynamicTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/dynamictask.xml");
    }
    public void testSimple() {
        executeTarget("simple");
        assertEquals("1", project.getProperty("prop1"));
        assertEquals("2", project.getProperty("prop2"));
        assertEquals("3", project.getProperty("prop3"));
        assertEquals("4", project.getProperty("prop4"));
    }
}
