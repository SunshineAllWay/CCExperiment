package org.apache.tools.ant.types.resources;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class TarResourceTest extends BuildFileTest {
    private static final FileUtils FU = FileUtils.getFileUtils();
    public TarResourceTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        configureProject("src/etc/testcases/types/resources/tarentry.xml");
    }
    protected void tearDown() throws Exception {
        executeTarget("tearDown");
    }
    public void testUncompressSource() throws java.io.IOException {
        executeTarget("uncompressSource");
        assertTrue(FU.contentEquals(project.resolveFile("../../asf-logo.gif"),
                                    project.resolveFile("testout/asf-logo.gif")));
    }
}
