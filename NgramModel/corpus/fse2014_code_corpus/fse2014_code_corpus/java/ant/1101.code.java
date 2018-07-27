package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
public class FlexIntegerTest extends BuildFileTest {
    public FlexIntegerTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/flexinteger.xml");
    }
    public void testFlexInteger() {
        executeTarget("test");
        assertEquals(project.getProperty("flexint.value1"), "10");
        assertEquals(project.getProperty("flexint.value2"), "8");
    }
    private Project taskProject;
    String propName;
    private FlexInteger value;
    public FlexIntegerTest() {
        super("FlexIntegerTest");
    }
    public void setPropName(String propName) {
        this.propName = propName;
    }
    public void setValue(FlexInteger value) {
        this.value = value;
    }
    public void setProject(Project project) {
        taskProject = project;
    }
    public void execute() {
        if (propName == null || value == null) {
            throw new BuildException("name and value required");
        }
        taskProject.setNewProperty(propName, value.toString());
    }
}
