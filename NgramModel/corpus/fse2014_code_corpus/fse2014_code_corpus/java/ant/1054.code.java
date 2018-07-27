package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class RhinoReferenceTest extends BuildFileTest {
    public RhinoReferenceTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(
            "src/etc/testcases/taskdefs/optional/script_reference.xml");
    }
    public void testScript() {
        executeTarget("script");
    }
}
