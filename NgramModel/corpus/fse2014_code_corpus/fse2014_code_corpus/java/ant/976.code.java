package org.apache.tools.ant.taskdefs;
import java.io.FileInputStream;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.input.PropertyFileInputHandler;
public class InputTest extends BuildFileTest {
    public InputTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/input.xml");
        System.getProperties()
            .put(PropertyFileInputHandler.FILE_NAME_KEY,
                 getProject().resolveFile("input.properties")
                 .getAbsolutePath());
        getProject().setInputHandler(new PropertyFileInputHandler());
    }
    public void test1() {
        executeTarget("test1");
    }
    public void test2() {
        executeTarget("test2");
    }
    public void test3() {
        expectSpecificBuildException("test3", "invalid input",
                                     "Found invalid input test for \'"
                                     + getKey("All data is"
                                              + " going to be deleted from DB"
                                              + " continue?")
                                     + "\'");
    }
    public void test5() {
        executeTarget("test5");
    }
    public void test6() {
        executeTarget("test6");
        assertEquals("scott", project.getProperty("db.user"));
    }
    public void testPropertyFileInlineHandler() {
        executeTarget("testPropertyFileInlineHandler");
    }
    public void testDefaultInlineHandler() {
        stdin();
        executeTarget("testDefaultInlineHandler");
    }
    public void testGreedyInlineHandler() {
        stdin();
        executeTarget("testGreedyInlineHandler");
    }
    public void testGreedyInlineHandlerClassname() {
        stdin();
        executeTarget("testGreedyInlineHandlerClassname");
    }
    public void testGreedyInlineHandlerRefid() {
        stdin();
        executeTarget("testGreedyInlineHandlerRefid");
    }
    private void stdin() {
        try {
            System.setIn(new FileInputStream(
                getProject().resolveFile("input.stdin")));
        } catch (Exception e) {
            throw e instanceof RuntimeException
                ? (RuntimeException) e : new RuntimeException(e.getMessage());
        }
    }
    private String getKey(String key) {
        return key; 
    }
}
