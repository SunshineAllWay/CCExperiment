package org.apache.tools.ant;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.PropertyFileInputHandler;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.*;
import java.io.File;
import junit.framework.TestCase;
public class ProjectTest extends TestCase {
    private Project p;
    private String root;
    private MockBuildListener mbl;
    public ProjectTest(String name) {
        super(name);
    }
    public void setUp() {
        p = new Project();
        p.init();
        root = new File(File.separator).getAbsolutePath().toUpperCase();
        mbl = new MockBuildListener(p);
    }
    public void testDataTypes() throws BuildException {
        assertNull("dummy is not a known data type",
                   p.createDataType("dummy"));
        Object o = p.createDataType("fileset");
        assertNotNull("fileset is a known type", o);
        assertTrue("fileset creates FileSet", o instanceof FileSet);
        assertTrue("PatternSet",
               p.createDataType("patternset") instanceof PatternSet);
        assertTrue("Path", p.createDataType("path") instanceof Path);
    }
    public void testResolveFile() {
        if (Os.isFamily("netware") || Os.isFamily("dos")) {
            assertEqualsIgnoreDriveCase(localize(File.separator),
                p.resolveFile("/", null).getPath());
            assertEqualsIgnoreDriveCase(localize(File.separator),
                p.resolveFile("\\", null).getPath());
            String driveSpec = "C:";
            String driveSpecLower = "c:";
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpec + "/", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpec + "\\", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpecLower + "/", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpecLower + "\\", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpec + "\\",
                         p.resolveFile(driveSpec + "/////", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpec + "\\",
                         p.resolveFile(driveSpec + "\\\\\\\\\\\\", null).getPath());
        } else {
            assertEquals(File.separator,
                         p.resolveFile("/", null).getPath());
            assertEquals(File.separator,
                         p.resolveFile("\\", null).getPath());
            String driveSpec = "C:";
            String udir = System.getProperty("user.dir") + File.separatorChar;
            assertEquals(udir + driveSpec,
                         p.resolveFile(driveSpec + "/", null).getPath());
            assertEquals(udir + driveSpec,
                         p.resolveFile(driveSpec + "\\", null).getPath());
            String driveSpecLower = "c:";
            assertEquals(udir + driveSpecLower,
                         p.resolveFile(driveSpecLower + "/", null).getPath());
            assertEquals(udir + driveSpecLower,
                         p.resolveFile(driveSpecLower + "\\", null).getPath());
        }
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("./4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile(".\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("./.\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("../3/4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("..\\3\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("../../5/.././2/./3/6/../4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("..\\../5/..\\./2/./3/6\\../4", new File(localize("/1/2/3"))).getPath());
    }
    private String localize(String path) {
        path = root + path.substring(1);
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }
    private void assertEqualsIgnoreDriveCase(String s1, String s2) {
        if ((Os.isFamily("dos") || Os.isFamily("netware"))
            && s1.length() >= 1 && s2.length() >= 1) {
            StringBuffer sb1 = new StringBuffer(s1);
            StringBuffer sb2 = new StringBuffer(s2);
            sb1.setCharAt(0, Character.toUpperCase(s1.charAt(0)));
            sb2.setCharAt(0, Character.toUpperCase(s2.charAt(0)));
            assertEquals(sb1.toString(), sb2.toString());
        } else {
            assertEquals(s1, s2);
        }
    }
    private void assertTaskDefFails(final Class taskClass,
                                       final String message) {
        final String dummyName = "testTaskDefinitionDummy";
        try {
            mbl.addBuildEvent(message, Project.MSG_ERR);
            p.addTaskDefinition(dummyName, taskClass);
            fail("expected BuildException(\""+message+"\", Project.MSG_ERR) when adding task " + taskClass);
        }
        catch(BuildException e) {
            assertEquals(message, e.getMessage());
            mbl.assertEmpty();
            assertTrue(!p.getTaskDefinitions().containsKey(dummyName));
        }
    }
    public void testAddTaskDefinition() {
        p.addBuildListener(mbl);
        p.addTaskDefinition("Ok", DummyTaskOk.class);
        assertEquals(DummyTaskOk.class, p.getTaskDefinitions().get("Ok"));
        p.addTaskDefinition("OkNonTask", DummyTaskOkNonTask.class);
        assertEquals(DummyTaskOkNonTask.class, p.getTaskDefinitions().get("OkNonTask"));
        mbl.assertEmpty();
        assertTaskDefFails(DummyTaskPrivate.class,   DummyTaskPrivate.class   + " is not public");
        assertTaskDefFails(DummyTaskProtected.class,
                           DummyTaskProtected.class + " is not public");
        assertTaskDefFails(DummyTaskPackage.class,   DummyTaskPackage.class   + " is not public");
        assertTaskDefFails(DummyTaskAbstract.class,  DummyTaskAbstract.class  + " is abstract");
        assertTaskDefFails(DummyTaskInterface.class, DummyTaskInterface.class + " is abstract");
        assertTaskDefFails(DummyTaskWithoutDefaultConstructor.class, "No public no-arg constructor in " + DummyTaskWithoutDefaultConstructor.class);
        assertTaskDefFails(DummyTaskWithoutPublicConstructor.class,  "No public no-arg constructor in " + DummyTaskWithoutPublicConstructor.class);
        assertTaskDefFails(DummyTaskWithoutExecute.class,       "No public execute() in " + DummyTaskWithoutExecute.class);
        assertTaskDefFails(DummyTaskWithNonPublicExecute.class, "No public execute() in " + DummyTaskWithNonPublicExecute.class);
        mbl.addBuildEvent("return type of execute() should be void but was \"int\" in " + DummyTaskWithNonVoidExecute.class, Project.MSG_WARN);
        p.addTaskDefinition("NonVoidExecute", DummyTaskWithNonVoidExecute.class);
        mbl.assertEmpty();
        assertEquals(DummyTaskWithNonVoidExecute.class, p.getTaskDefinitions().get("NonVoidExecute"));
    }
    public void testInputHandler() {
        InputHandler ih = p.getInputHandler();
        assertNotNull(ih);
        assertTrue(ih instanceof DefaultInputHandler);
        InputHandler pfih = new PropertyFileInputHandler();
        p.setInputHandler(pfih);
        assertSame(pfih, p.getInputHandler());
    }
    public void testTaskDefinitionContainsKey() {
        assertTrue(p.getTaskDefinitions().containsKey("echo"));
    }
    public void testTaskDefinitionContains() {
        assertTrue(p.getTaskDefinitions().contains(org.apache.tools.ant.taskdefs.Echo.class));
    }
    public void testDuplicateTargets() {
        try {
            BFT bft = new BFT("", "core/duplicate-target.xml");
        } catch (BuildException ex) {
            assertEquals("specific message",
                         "Duplicate target 'twice'",
                         ex.getMessage());
            return;
        }
        fail("Should throw BuildException about duplicate target");
    }
    public void testDuplicateTargetsImport() {
        BFT bft = new BFT("", "core/duplicate-target2.xml");
        bft.expectLog("once", "once from buildfile");
    }
    public void testOutputDuringMessageLoggedIsSwallowed()
        throws InterruptedException {
        final String FOO = "foo", BAR = "bar";
        p.addBuildListener(new BuildListener() {
                public void buildStarted(BuildEvent event) {}
                public void buildFinished(BuildEvent event) {}
                public void targetStarted(BuildEvent event) {}
                public void targetFinished(BuildEvent event) {}
                public void taskStarted(BuildEvent event) {}
                public void taskFinished(BuildEvent event) {}
                public void messageLogged(final BuildEvent actual) {
                    assertEquals(FOO, actual.getMessage());
                    System.err.println(BAR);
                    System.out.println(BAR);
                    p.log(BAR, Project.MSG_INFO);
                }
            });
        final boolean[] done = new boolean[] {false};
        Thread t = new Thread() {
                public void run() {
                    p.log(FOO, Project.MSG_INFO);
                    done[0] = true;
                }
            };
        t.start();
        t.join(2000);
        assertTrue("Expected logging thread to finish successfully", done[0]);
    }
    public void testNullThrowableMessageLog() {
        p.log(new Task() {}, null, new Throwable(), Project.MSG_ERR);
    }
    private class DummyTaskPrivate extends Task {
        public DummyTaskPrivate() {}
        public void execute() {}
    }
    protected class DummyTaskProtected extends Task {
        public DummyTaskProtected() {}
        public void execute() {}
    }
    private class BFT extends org.apache.tools.ant.BuildFileTest {
        BFT(String name, String buildfile) {
            super(name);
            this.buildfile = buildfile;
            setUp();
        }
        boolean isConfigured = false;
        String buildfile = "";
        public void setUp() {
            if (!isConfigured) {
                configureProject("src/etc/testcases/"+buildfile);
                isConfigured = true;
            }
        }
        public void tearDown() { }
        public void doTarget(String target) {
            if (!isConfigured) setUp();
            executeTarget(target);
        }
        public org.apache.tools.ant.Project getProject() {
            return super.getProject();
        }
    }
}
class DummyTaskPackage extends Task {
    public DummyTaskPackage() {}
    public void execute() {}
}
