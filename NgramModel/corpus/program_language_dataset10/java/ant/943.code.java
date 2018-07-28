 package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class AptTest extends BuildFileTest {
    public AptTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/apt.xml");
    }
    protected void tearDown() throws Exception {
        executeTarget("clean");
    }
    public void testApt() {
        executeTarget("testApt");
    }
    public void testAptFork() {
        executeTarget("testAptFork");
    }
    public void testAptForkFalse() {
        executeTarget("testAptForkFalse");
        assertLogContaining(Apt.WARNING_IGNORING_FORK);
    }
    public void testListAnnotationTypes() {
        executeTarget("testListAnnotationTypes");
        assertLogContaining("Set of annotations found:");
        assertLogContaining("Distributed");
    }
    public void testAptNewFactory() {
        executeTarget("testAptNewFactory");
        assertProcessed();
    }
    public void testAptNewFactoryFork() {
        executeTarget("testAptNewFactoryFork");
        assertProcessed();
    }
    private void assertProcessed() {
        assertLogContaining("DistributedAnnotationProcessor-is-go");
        assertLogContaining("[-Abuild.dir=");
        assertLogContaining("visiting DistributedAnnotationFactory");
    }
}
