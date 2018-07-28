package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class XmlValidateCatalogTest extends BuildFileTest {
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";
    public XmlValidateCatalogTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "xmlvalidate.xml");
    }
    public void tearDown() {
    }
    public void testXmlCatalogFiles() {
        executeTarget("xmlcatalogfiles");
    }
    public void testXmlCatalogPath() {
        executeTarget("xmlcatalogpath");
    }
}
