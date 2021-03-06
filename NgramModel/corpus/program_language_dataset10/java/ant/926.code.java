package org.apache.tools.ant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class UnknownElementTest extends BuildFileTest {
    public void setUp() {
        configureProject("src/etc/testcases/core/unknownelement.xml");
    }
    public void testMaybeConfigure() {
        executeTarget("testMaybeConfigure");
    }
    public void XtestTaskFinishedEvent() {
        getProject().addBuildListener(new BuildListener() {
                public void buildStarted(BuildEvent event) {}
                public void buildFinished(BuildEvent event) {}
                public void targetStarted(BuildEvent event) {}
                public void targetFinished(BuildEvent event) {}
                public void taskStarted(BuildEvent event) {
                    assertTaskProperties(event.getTask());
                }
                public void taskFinished(BuildEvent event) {
                    assertTaskProperties(event.getTask());
                }
                public void messageLogged(BuildEvent event) {}
                private void assertTaskProperties(Task ue) {
                    assertNotNull(ue);
                    assertTrue(ue instanceof UnknownElement);
                    Task t = ((UnknownElement) ue).getTask();
                    assertNotNull(t);
                    assertEquals("org.apache.tools.ant.taskdefs.Echo",
                                 t.getClass().getName());
                }
            });
        executeTarget("echo");
    }
    public static class Child extends Task {
        Parent parent;
        public void injectParent(Parent parent) {
            this.parent = parent;
        }
        public void execute() {
            parent.fromChild();
        }
    }
    public static class Parent extends Task implements TaskContainer {
        List children = new ArrayList();
        public void addTask(Task t) {
            children.add(t);
        }
        public void fromChild() {
            log("fromchild");
        }
        public void execute() {
            for (Iterator i = children.iterator(); i.hasNext();) {
                UnknownElement el = (UnknownElement) i.next();
                el.maybeConfigure();
                Child child = (Child) el.getRealThing();
                child.injectParent(this);
                child.perform();
            }
        }
    }
}
