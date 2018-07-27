package org.apache.tools.ant.types.mappers;
import org.apache.tools.ant.BuildFileTest;
public class GlobMapperTest extends BuildFileTest {
    public GlobMapperTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/mappers/globmapper.xml");
    }
    public void testIgnoreCase() {
        executeTarget("ignore.case");
    }
    public void testHandleDirSep() {
        executeTarget("handle.dirsep");
    }
}
