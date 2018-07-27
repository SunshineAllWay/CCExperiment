package org.apache.tools.ant.types.selectors;
import java.io.File;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
public class ContainsRegexpTest extends TestCase {
    private Project project;
    public ContainsRegexpTest(String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.setBasedir(".");
    }
    public void testContainsRegexp() {
        TaskdefForRegexpTest MyTask =
            new TaskdefForRegexpTest("containsregexp");
        try {
            MyTask.setUp();
            MyTask.test();
        } finally {
            MyTask.tearDown();
        }
    }
    private class TaskdefForRegexpTest extends BuildFileTest {
        TaskdefForRegexpTest(String name) {
            super(name);
        }
        public void setUp() {
            configureProject("src/etc/testcases/types/selectors.xml");
        }
        public void tearDown() {
            executeTarget("cleanupregexp");
        }
        public void test() {
            File dir = null;
            File[] files = null;
            int filecount;
            executeTarget("containsregexp");
            dir = new File(getProjectDir() + "/regexpseltestdest/");
            files = dir.listFiles();
            filecount = files.length;
            if (filecount != 1)
                assertEquals("ContainsRegexp test should have copied 1 file",
                             1, files.length);
        }
    }
}
