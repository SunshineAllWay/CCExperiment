package org.apache.tools.ant;
import java.util.Vector;
public class ExecutorTest extends BuildFileTest implements BuildListener {
    private static final String SINGLE_CHECK
        = "org.apache.tools.ant.helper.SingleCheckExecutor";
    private static final String IGNORE_DEPS
        = "org.apache.tools.ant.helper.IgnoreDependenciesExecutor";
    private static final Vector TARGET_NAMES;
    static {
        TARGET_NAMES = new Vector();
        TARGET_NAMES.add("a");
        TARGET_NAMES.add("b");
    }
    private int targetCount;
    public void targetStarted(BuildEvent event) {
        targetCount++;
    }
    public void buildStarted(BuildEvent event) {}
    public void buildFinished(BuildEvent event) {}
    public void targetFinished(BuildEvent event) {}
    public void taskStarted(BuildEvent event) {}
    public void taskFinished(BuildEvent event) {}
    public void messageLogged(BuildEvent event) {}
    public ExecutorTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/executor.xml");
        targetCount = 0;
        getProject().addBuildListener(this);
    }
    private Project getProject(String e) {
        return getProject(e, false);
    }
    private Project getProject(String e, boolean f) {
        return getProject(e, f, false);
    }
    private Project getProject(String e, boolean f, boolean k) {
        Project p = getProject();
        p.setNewProperty("ant.executor.class", e);
        p.setKeepGoingMode(k);
        if (f) {
            p.setNewProperty("failfoo", "foo");
        }
        return p;
    }
    public void testDefaultExecutor() {
        getProject().executeTargets(TARGET_NAMES);
        assertEquals(4, targetCount);
    }
    public void testSingleCheckExecutor() {
        getProject(SINGLE_CHECK).executeTargets(TARGET_NAMES);
        assertEquals(3, targetCount);
    }
    public void testIgnoreDependenciesExecutor() {
        getProject(IGNORE_DEPS).executeTargets(TARGET_NAMES);
        assertEquals(2, targetCount);
    }
    public void testDefaultFailure() {
        try {
            getProject(null, true).executeTargets(TARGET_NAMES);
            fail("should fail");
        } catch (BuildException e) {
            assertTrue(e.getMessage().equals("failfoo"));
            assertEquals(1, targetCount);
        }
    }
    public void testSingleCheckFailure() {
        try {
            getProject(SINGLE_CHECK, true).executeTargets(TARGET_NAMES);
            fail("should fail");
        } catch (BuildException e) {
            assertTrue(e.getMessage().equals("failfoo"));
            assertEquals(1, targetCount);
        }
    }
    public void testIgnoreDependenciesFailure() {
        getProject(IGNORE_DEPS, true).executeTargets(TARGET_NAMES);
    }
    public void testKeepGoingDefault() {
        try {
            getProject(null, true, true).executeTargets(TARGET_NAMES);
            fail("should fail");
        } catch (BuildException e) {
            assertTrue(e.getMessage().equals("failfoo"));
            assertEquals(2, targetCount);
        }
    }
    public void testKeepGoingSingleCheck() {
        try {
            getProject(SINGLE_CHECK, true, true).executeTargets(TARGET_NAMES);
            fail("should fail");
        } catch (BuildException e) {
            assertTrue(e.getMessage().equals("failfoo"));
            assertEquals(1, targetCount);
        }
    }
    public void testKeepGoingIgnoreDependencies() {
        try {
            Vector targetNames = new Vector(TARGET_NAMES);
            targetNames.add(0, "foo");
            getProject(IGNORE_DEPS, true, true).executeTargets(targetNames);
            fail("should fail");
        } catch (BuildException e) {
            assertTrue(e.getMessage().equals("failfoo"));
            assertEquals(3, targetCount);
        }
    }
}
