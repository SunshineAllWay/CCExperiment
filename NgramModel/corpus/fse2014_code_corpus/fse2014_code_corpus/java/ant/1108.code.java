package org.apache.tools.ant.types;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildFileTest;
public class RedirectorElementTest extends BuildFileTest {
    public RedirectorElementTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/redirector.xml", Project.MSG_VERBOSE);
    }
    public void test1() {
        executeTarget("test1");
        assertTrue((getProject().getReference("test1")
            instanceof RedirectorElement));
    }
    public void test2() {
        expectBuildException("test2", "You must not specify more than one "
            + "attribute when using refid");
    }
    public void test3() {
        expectBuildException("test3", "You must not specify nested elements "
            + "when using refid");
    }
    public void test4() {
        executeTarget("test4");
    }
    public void testLogInputString() {
        executeTarget("testLogInputString");
        if (super.getLog().indexOf("testLogInputString can-cat") >=0 ) {
            assertDebuglogContaining("Using input string");
        }
    }
    public void testRefid() {
        executeTarget("testRefid");
    }
}
