package org.apache.tools.ant.taskdefs.optional.jdepend;
import org.apache.tools.ant.BuildFileTest;
public class JDependTest extends BuildFileTest {
    public static final String RESULT_FILESET = "result";
    public JDependTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(
            "src/etc/testcases/taskdefs/optional/jdepend/jdepend.xml");
    }
    public void testSimple() {
        expectOutputContaining(
            "simple", "Package: org.apache.tools.ant.util.facade");
    }
    public void testXml() {
        expectOutputContaining(
            "xml", "<DependsUpon>");
    }
    public void testFork() {
        expectLogContaining(
            "fork", "Package: org.apache.tools.ant.util.facade");
    }
    public void testForkXml() {
        expectLogContaining(
            "fork-xml", "<DependsUpon>");
    }
    public void testTimeout() {
        expectLogContaining(
            "fork-timeout", "JDepend FAILED - Timed out");
    }
    public void testTimeoutNot() {
        expectLogContaining(
            "fork-timeout-not", "Package: org.apache.tools.ant.util.facade");
    }
    protected void expectOutputContaining(String target, String substring) {
        executeTarget(target);
        assertOutputContaining(substring);
    }
}
