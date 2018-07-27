package org.apache.tools.ant.util;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceFactory;
public class ResourceUtilsTest extends TestCase
    implements ResourceFactory, FileNameMapper {
    private Echo taskINeedForLogging = new Echo();
    public ResourceUtilsTest(String name) {
        super(name);
        taskINeedForLogging.setProject(new Project());
    }
    public void testNoDuplicates() {
        Resource r = new Resource("samual vimes", true, 1, false);
        Resource[] toNew =
            ResourceUtils.selectOutOfDateSources(taskINeedForLogging,
                                                 new Resource[] {r},
                                                 this, this);
        assertEquals(1, toNew.length);
    }
    public Resource getResource(String name) {
        return new Resource(name); 
    }
    public void setFrom(String s) {}
    public void setTo(String s) {}
    public String[] mapFileName(String s) {
        return new String[] {"fred colon", "carrot ironfoundersson"};
    }
}
