package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class BeanShellScriptTest extends BuildFileTest {
    public BeanShellScriptTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/script.xml");
    }
    public void testCanLoad() {
        expectLog("useBeanshell", "I'm here");
    }
}
