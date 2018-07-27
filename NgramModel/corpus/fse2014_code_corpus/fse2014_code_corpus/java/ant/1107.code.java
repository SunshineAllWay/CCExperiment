package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
public class PropertySetTest extends BuildFileTest {
    public PropertySetTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/propertyset.xml");
    }
    public void testReferenceToTwoReferences() {
        executeTarget("reference-to-two-references");
    }
    public void testNestedMapped() {
        executeTarget("nested-mapped");
    }
    public void testNestedMappedMapped() {
        executeTarget("nested-mapped-mapped");
    }
}
