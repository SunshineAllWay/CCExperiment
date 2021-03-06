package org.apache.tools.ant.types.mappers;
import org.apache.tools.ant.BuildFileTest;
public class RegexpPatternMapperTest extends BuildFileTest {
    public RegexpPatternMapperTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/mappers/regexpmapper.xml");
    }
    public void testIgnoreCase() {
        executeTarget("ignore.case");
    }
    public void testHandleDirSep() {
        executeTarget("handle.dirsep");
    }
}
