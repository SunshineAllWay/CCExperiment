package org.apache.tools.ant;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public class AntClassLoaderDelegationTest extends TestCase {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Project p;
    public AntClassLoaderDelegationTest(String name) {
        super(name);
    }
    public void setUp() {
        p = new Project();
        p.init();
    }
    private static final String TEST_RESOURCE
        = "apache/tools/ant/IncludeTest.class";
    public void testFindResources() throws Exception {
        String buildTestcases = System.getProperty("build.tests");
        assertNotNull("defined ${build.tests}", buildTestcases);
        assertTrue("have a dir " + buildTestcases,
                   new File(buildTestcases).isDirectory());
        Path path = new Path(p, buildTestcases + "/org");
        ClassLoader parent = new ParentLoader();
        ClassLoader acl = new AntClassLoader(parent, p, path, true);
        URL urlFromPath = new URL(
            FILE_UTILS.toURI(buildTestcases) + "org/" + TEST_RESOURCE);
        URL urlFromParent = new URL("http://ant.apache.org/" + TEST_RESOURCE);
        assertEquals("correct resources (regular delegation order)",
            Arrays.asList(new URL[] {urlFromParent, urlFromPath}),
            enum2List(acl.getResources(TEST_RESOURCE)));
        acl = new AntClassLoader(parent, p, path, false);
        assertEquals("correct resources (reverse delegation order)",
            Arrays.asList(new URL[] {urlFromPath, urlFromParent}),
            enum2List(acl.getResources(TEST_RESOURCE)));
    }
    public void testFindIsolateResources() throws Exception {
        String buildTestcases = System.getProperty("build.tests");
        assertNotNull("defined ${build.tests}", buildTestcases);
        assertTrue("have a dir " + buildTestcases,
                   new File(buildTestcases).isDirectory());
        Path path = new Path(p, buildTestcases + "/org");
        ClassLoader parent = new ParentLoader();
        URL urlFromPath = new URL(
            FILE_UTILS.toURI(buildTestcases) + "org/" + TEST_RESOURCE);
        AntClassLoader acl = new AntClassLoader(parent, p, path, false);
        acl.setIsolated(true);
        assertEquals("correct resources (reverse delegation order)",
            Arrays.asList(new URL[] {urlFromPath}),
            enum2List(acl.getResources(TEST_RESOURCE)));
    }
    private static List enum2List(Enumeration e) {
        return Collections.list(e);
    }
    private static final class ParentLoader extends ClassLoader {
        public ParentLoader() {}
        protected Enumeration findResources(String name) throws IOException {
            if (name.equals(TEST_RESOURCE)) {
                return Collections.enumeration(
                    Collections.singleton(
                        new URL("http://ant.apache.org/" + name)));
            } else {
                return Collections.enumeration(Collections.EMPTY_SET);
            }
        }
    }
}
