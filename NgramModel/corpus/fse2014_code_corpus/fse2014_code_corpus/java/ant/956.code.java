package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DirectoryScanner;
public class DefaultExcludesTest extends BuildFileTest {
    public DefaultExcludesTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/defaultexcludes.xml");
    }
    public void tearDown() {
        project.executeTarget("cleanup");
    }
    public void test1() {
        String[] expected = {
                          "**/*~",
                          "**/#*#",
                          "**/.#*",
                          "**/%*%",
                          "**/._*",
                          "**/CVS",
                          "**/CVS/**",
                          "**/.cvsignore",
                          "**/SCCS",
                          "**/SCCS/**",
                          "**/vssver.scc",
                          "**/.svn",
                          "**/.svn/**",
                          "**/.git",
                          "**/.git/**",
                          "**/.gitattributes",
                          "**/.gitignore",
                          "**/.gitmodules",
                          "**/.hg",
                          "**/.hg/**",
                          "**/.hgignore",
                          "**/.hgsub",
                          "**/.hgsubstate",
                          "**/.hgtags",
                          "**/.bzr",
                          "**/.bzr/**",
                          "**/.bzrignore",
                          "**/.DS_Store"};
        project.executeTarget("test1");
        assertEquals("current default excludes", expected, DirectoryScanner.getDefaultExcludes());
    }
    public void test2() {
        String[] expected = {
                          "**/*~",
                          "**/#*#",
                          "**/.#*",
                          "**/%*%",
                          "**/._*",
                          "**/CVS",
                          "**/CVS/**",
                          "**/.cvsignore",
                          "**/SCCS",
                          "**/SCCS/**",
                          "**/vssver.scc",
                          "**/.svn",
                          "**/.svn/**",
                          "**/.git",
                          "**/.git/**",
                          "**/.gitattributes",
                          "**/.gitignore",
                          "**/.gitmodules",
                          "**/.hg",
                          "**/.hg/**",
                          "**/.hgignore",
                          "**/.hgsub",
                          "**/.hgsubstate",
                          "**/.hgtags",
                          "**/.bzr",
                          "**/.bzr/**",
                          "**/.bzrignore",
                          "**/.DS_Store",
                          "foo"};
        project.executeTarget("test2");
        assertEquals("current default excludes", expected, DirectoryScanner.getDefaultExcludes());
    }
    public void test3() {
        String[] expected = {
                          "**/*~",
                          "**/#*#",
                          "**/.#*",
                          "**/%*%",
                          "**/._*",
                          "**/CVS/**",
                          "**/.cvsignore",
                          "**/SCCS",
                          "**/SCCS/**",
                          "**/vssver.scc",
                          "**/.svn",
                          "**/.svn/**",
                          "**/.git",
                          "**/.git/**",
                          "**/.gitattributes",
                          "**/.gitignore",
                          "**/.gitmodules",
                          "**/.hg",
                          "**/.hg/**",
                          "**/.hgignore",
                          "**/.hgsub",
                          "**/.hgsubstate",
                          "**/.hgtags",
                          "**/.bzr",
                          "**/.bzr/**",
                          "**/.bzrignore",
                          "**/.DS_Store"};
        project.executeTarget("test3");
        assertEquals("current default excludes", expected, DirectoryScanner.getDefaultExcludes());
    }
    private void assertEquals(String message, String[] expected, String[] actual) {
        assertEquals(message + " : string array length match", expected.length, actual.length);
        for (int counter=0; counter < expected.length; counter++) {
            boolean found = false;
            for (int i = 0; !found && i < actual.length; i++) {
                found |= expected[counter].equals(actual[i]);
            }
            assertTrue(message + " : didn't find element "
                       + expected[counter] + " in array match", found);
        }
    }
}
