package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class RhinoScriptTest extends BuildFileTest {
    public RhinoScriptTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/script.xml");
    }
    public void testExample1() {
        executeTarget("example1");
        int index = getLog().indexOf("1");
        assertTrue(index > -1);
        index = getLog().indexOf("4", index);
        assertTrue(index > -1);
        index = getLog().indexOf("9", index);
        assertTrue(index > -1);
        index = getLog().indexOf("16", index);
        assertTrue(index > -1);
        index = getLog().indexOf("25", index);
        assertTrue(index > -1);
        index = getLog().indexOf("36", index);
        assertTrue(index > -1);
        index = getLog().indexOf("49", index);
        assertTrue(index > -1);
        index = getLog().indexOf("64", index);
        assertTrue(index > -1);
        index = getLog().indexOf("81", index);
        assertTrue(index > -1);
        index = getLog().indexOf("100", index);
        assertTrue(index > -1);
    }
}
