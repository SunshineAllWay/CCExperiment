package org.apache.tools.ant.types.optional;
import org.apache.tools.ant.BuildFileTest;
public class ScriptMapperTest extends BuildFileTest {
    public ScriptMapperTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/mappers/scriptmapper.xml");
    }
    public void testClear() {
        executeTarget("testClear");
    }
    public void testSetMultiple() {
        executeTarget("testSetMultiple");
    }
    public void testPassthrough() {
        executeTarget("testPassthrough");
    }
}
