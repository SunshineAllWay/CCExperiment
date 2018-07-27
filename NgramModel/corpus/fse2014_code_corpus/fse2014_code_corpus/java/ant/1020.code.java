package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class WhichResourceTest extends BuildFileTest {
    public static final String TEST_BUILD_FILE
        = "src/etc/testcases/taskdefs/whichresource.xml";
    public WhichResourceTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TEST_BUILD_FILE);
    }
    public void testClassname() {
        executeTarget("testClassname");
        assertNotNull(getProject().getProperty("antmain"));
    }
    public void testResourcename() {
        executeTarget("testResourcename");
        assertNotNull(getProject().getProperty("defaults"));
    }
    public void testResourcenameWithLeadingSlash() {
        executeTarget("testResourcenameWithLeadingSlash");
        assertNotNull(getProject().getProperty("defaults"));
    }
}
