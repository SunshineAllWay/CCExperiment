package org.apache.tools.ant;
import java.io.File;
import junit.framework.TestCase;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.StringResource;
public class ProjectHelperRepositoryTest extends TestCase {
    public static class SomeHelper extends ProjectHelper {
        public boolean canParseBuildFile(Resource buildFile) {
            return buildFile instanceof FileResource
                    && buildFile.getName().endsWith(".myext");
        }
        public boolean canParseAntlibDescriptor(Resource r) {
            return r instanceof FileResource && r.getName().endsWith(".myext");
        }
    }
    public void testFind() throws Exception {
        ProjectHelperRepository repo = ProjectHelperRepository.getInstance();
        repo.registerProjectHelper(SomeHelper.class);
        Resource r = new FileResource(new File("test.xml"));
        ProjectHelper helper = repo.getProjectHelperForBuildFile(r);
        assertTrue(helper instanceof ProjectHelper2);
        helper = repo.getProjectHelperForAntlib(r);
        assertTrue(helper instanceof ProjectHelper2);
        r = new FileResource(new File("test.myext"));
        helper = repo.getProjectHelperForBuildFile(r);
        assertTrue(helper instanceof SomeHelper);
        helper = repo.getProjectHelperForAntlib(r);
        assertTrue(helper instanceof SomeHelper);
        r = new StringResource("test.myext");
        helper = repo.getProjectHelperForBuildFile(r);
        assertTrue(helper instanceof ProjectHelper2);
        helper = repo.getProjectHelperForAntlib(r);
        assertTrue(helper instanceof ProjectHelper2);
        r = new StringResource("test.other");
        helper = repo.getProjectHelperForBuildFile(r);
        assertTrue(helper instanceof ProjectHelper2);
        helper = repo.getProjectHelperForAntlib(r);
        assertTrue(helper instanceof ProjectHelper2);
    }
    public void testNoDefaultContructor() throws Exception {
        class IncrrectHelper extends ProjectHelper {
        }
        ProjectHelperRepository repo = ProjectHelperRepository.getInstance();
        try {
            repo.registerProjectHelper(IncrrectHelper.class);
            fail("Registring an helper with no default constructor should fail");
        } catch (BuildException e) {
        }
    }
    public void testUnkwnowHelper() throws Exception {
        ProjectHelperRepository repo = ProjectHelperRepository.getInstance();
        try {
            repo.registerProjectHelper("xxx.yyy.zzz.UnknownHelper");
            fail("Registring an unknwon helper should fail");
        } catch (BuildException e) {
        }
    }
}
