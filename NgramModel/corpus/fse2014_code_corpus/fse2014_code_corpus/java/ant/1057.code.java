package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class SchemaValidateTest extends BuildFileTest {
    private final static String TASKDEFS_DIR =
            "src/etc/testcases/taskdefs/optional/";
    public SchemaValidateTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "schemavalidate.xml");
    }
    public void testNoNamespace() throws Exception {
        executeTarget("testNoNamespace");
    }
    public void testNSMapping() throws Exception {
        executeTarget("testNSMapping");
    }
    public void testNoEmptySchemaNamespace() throws Exception {
        expectBuildExceptionContaining("testNoEmptySchemaNamespace",
                "empty namespace URI",SchemaValidate.SchemaLocation.ERROR_NO_URI);
    }
    public void testNoEmptySchemaLocation() throws Exception {
        expectBuildExceptionContaining("testNoEmptySchemaLocation",
                "empty schema location",
                SchemaValidate.SchemaLocation.ERROR_NO_LOCATION);
    }
    public void testNoFile() throws Exception {
        expectBuildExceptionContaining("testNoFile",
                "no file at file attribute",
                SchemaValidate.SchemaLocation.ERROR_NO_FILE);
    }
    public void testNoDoubleSchemaLocation() throws Exception {
        expectBuildExceptionContaining("testNoDoubleSchemaLocation",
                "two locations for schemas",
                SchemaValidate.SchemaLocation.ERROR_TWO_LOCATIONS);
    }
    public void testNoDuplicateSchema() throws Exception {
        expectBuildExceptionContaining("testNoDuplicateSchema",
                "duplicate schemas with different values",
                SchemaValidate.ERROR_DUPLICATE_SCHEMA);
    }
    public void testEqualsSchemasOK() throws Exception {
        executeTarget("testEqualsSchemasOK");
    }
    public void testFileset() throws Exception {
        executeTarget("testFileset");
    }
}
