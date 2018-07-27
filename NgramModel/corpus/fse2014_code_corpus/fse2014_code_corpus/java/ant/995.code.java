package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.tools.ant.BuildFileTest;
public class ProtectedJarMethodsTest extends BuildFileTest {
    private static String tempJar = "tmp.jar";
    public ProtectedJarMethodsTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/jar.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testGrabFilesAndDirs() throws IOException {
        executeTarget("testIndexTests");
        String archive = getProject().resolveFile(tempJar).getAbsolutePath();
        ArrayList dirs = new ArrayList();
        ArrayList files = new ArrayList();
        String[] expectedDirs = new String[] {
            "META-INF/",
            "sub/",
        };
        String[] expectedFiles = new String[] {
            "foo",
        };
        Jar.grabFilesAndDirs(archive, dirs, files);
        assertEquals(expectedDirs.length, dirs.size());
        for (int i = 0; i < expectedDirs.length; i++) {
            assertTrue("Found " + expectedDirs[i],
                       dirs.contains(expectedDirs[i]));
        }
        assertEquals(expectedFiles.length, files.size());
        for (int i = 0; i < expectedFiles.length; i++) {
            assertTrue("Found " + expectedFiles[i],
                       files.contains(expectedFiles[i]));
        }
    }
    public void testFindJarNameNoClasspath() {
        assertEquals("foo", Jar.findJarName("foo", null));
        assertEquals("foo", Jar.findJarName("lib" + File.separatorChar + "foo",
                                            null));
    }
    public void testFindJarNameNoMatch() {
        assertNull(Jar.findJarName("foo", new String[] {"bar"}));
    }
    public void testFindJarNameSimpleMatches() {
        assertEquals("foo", Jar.findJarName("foo", new String[] {"foo"}));
        assertEquals("lib/foo", Jar.findJarName("foo",
                                                new String[] {"lib/foo"}));
        assertEquals("foo", Jar.findJarName("bar" + File.separatorChar + "foo",
                                            new String[] {"foo"}));
        assertEquals("lib/foo",
                     Jar.findJarName("bar" + File.separatorChar + "foo",
                                     new String[] {"lib/foo"}));
    }
    public void testFindJarNameLongestMatchWins() {
        assertEquals("lib/foo",
                     Jar.findJarName("lib/foo", 
                                     new String[] {"foo", "lib/foo", 
                                                   "lib/bar/foo"}));
    }
}
