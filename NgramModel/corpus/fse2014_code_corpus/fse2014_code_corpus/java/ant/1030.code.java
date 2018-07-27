package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class IsFileSelectedTest extends BuildFileTest {
    public IsFileSelectedTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/isfileselected.xml");
    }
    public void testSimple() {
        executeTarget("simple");
    }
    public void testName() {
        executeTarget("name");
    }
    public void testBaseDir() {
        executeTarget("basedir");
    }
    public void testType() {
        executeTarget("type");
    }
    public void testNotSelector() {
        expectBuildExceptionContaining(
            "not.selector", "checking for use as a selector (not allowed)",
            "fileset doesn't support the nested \"isfile");
    }
}
