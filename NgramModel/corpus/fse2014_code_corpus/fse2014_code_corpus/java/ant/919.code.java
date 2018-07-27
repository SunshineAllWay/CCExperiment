package org.apache.tools.ant;
import junit.framework.TestCase;
public class ProjectComponentTest extends TestCase {
    public ProjectComponentTest(String name) {
        super(name);
    }
    public void testClone() throws CloneNotSupportedException {
        Project expectedProject = new Project();
        Location expectedLocation = new Location("foo");
        String expectedDescription = "bar";
        ProjectComponent pc = new ProjectComponent() {
            };
        pc.setProject(expectedProject);
        pc.setLocation(expectedLocation);
        pc.setDescription(expectedDescription);
        ProjectComponent cloned = (ProjectComponent) pc.clone();
        assertNotSame(pc, cloned);
        assertSame(cloned.getProject(), expectedProject);
        assertSame(cloned.getLocation(), expectedLocation);
        assertSame(cloned.getDescription(), expectedDescription);
    }
}