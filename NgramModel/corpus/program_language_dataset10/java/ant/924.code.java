package org.apache.tools.ant;
public class TaskContainerTest extends BuildFileTest {
    public TaskContainerTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/taskcontainer.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testPropertyExpansion() {
        executeTarget("testPropertyExpansion");
        assertTrue("attribute worked",
                   getLog().indexOf("As attribute: it worked") > -1);
        assertTrue("nested text worked",
                   getLog().indexOf("As nested text: it worked") > -1);
    }
    public void testTaskdef() {
        executeTarget("testTaskdef");
        assertTrue("attribute worked",
                   getLog().indexOf("As attribute: it worked") > -1);
        assertTrue("nested text worked",
                   getLog().indexOf("As nested text: it worked") > -1);
        assertTrue("nested text worked",
                   getLog().indexOf("As nested task: it worked") > -1);
    }
    public void testCaseInsensitive() {
        executeTarget("testCaseInsensitive");
        assertTrue("works outside of container",
                   getLog().indexOf("hello ") > -1);
        assertTrue("works inside of container",
                   getLog().indexOf("world") > -1);
    }
}
