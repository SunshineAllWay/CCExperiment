package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
public class XMLCatalogBuildFileTest extends BuildFileTest {
    public XMLCatalogBuildFileTest(String name) {
        super(name);
    }
    public void setUp() {
    }
    public void tearDown() {
    }
    public void testEntityNoCatalog() {
        configureProject("src/etc/testcases/types/xmlcatalog.xml");
        expectPropertySet("testentitynocatalog", "val1",
                          "A stitch in time saves nine");
    }
    public void testEntityWithCatalog() {
        configureProject("src/etc/testcases/types/xmlcatalog.xml");
        expectPropertySet("testentitywithcatalog", "val2",
                          "No news is good news");
    }
    public void testDocumentNoCatalog() {
        configureProject("src/etc/testcases/types/xmlcatalog.xml");
        expectPropertySet("testdocumentnocatalog", "val3",
                          "A stitch in time saves nine");
    }
    public void testDocumentWithCatalog() {
        configureProject("src/etc/testcases/types/xmlcatalog.xml");
        expectPropertySet("testdocumentwithcatalog", "val4",
                          "No news is good news");
    }
}
