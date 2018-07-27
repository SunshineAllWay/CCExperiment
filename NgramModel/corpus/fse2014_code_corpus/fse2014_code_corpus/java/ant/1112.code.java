package org.apache.tools.ant.types;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.InputSource;
public class XMLCatalogTest extends TestCase {
    private Project project;
    private XMLCatalog catalog;
    private XMLCatalog newCatalog() {
        XMLCatalog cat = new XMLCatalog();
        cat.setProject(project);
        return cat;
    }
    private String toURLString(File file) throws MalformedURLException {
        return JAXPUtils.getSystemId(file);
    }
    public XMLCatalogTest(String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.setBasedir(System.getProperty("root"));
        catalog = newCatalog();
    }
    public void tearDown() {
        project = null;
        catalog = null;
    }
    public void testEmptyCatalog() {
        try {
            InputSource result = catalog.resolveEntity("PUBLIC ID ONE",
                                                       "i/dont/exist.dtd");
            assertNull("Empty catalog should return null", result);
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        try {
            Source result = catalog.resolve("i/dont/exist.dtd", null);
            String expected = toURLString(new File(project.getBaseDir() +
                                                   "/i/dont/exist.dtd"));
            String resultStr =
                fileURLPartWithoutLeadingSlashes((SAXSource)result);
            assertTrue("Empty catalog should return input with a system ID like "
                       + expected + " but was " + resultStr,
                       expected.endsWith(resultStr));
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
    private static String fileURLPartWithoutLeadingSlashes(SAXSource result)
        throws MalformedURLException {
        String resultStr =
            new URL(result.getInputSource().getSystemId()).getFile();
        while (resultStr.startsWith("/")) {
            resultStr = resultStr.substring(1);
        }
        return resultStr;
    }
    public void testNonExistentEntry() {
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId("PUBLIC ID ONE");
        dtd.setLocation("i/dont/exist.dtd");
        try {
            InputSource result = catalog.resolveEntity("PUBLIC ID ONE",
                                                       "i/dont/exist.dtd");
            assertNull("Nonexistent Catalog entry should not be returned", result);
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        try {
            Source result = catalog.resolve("i/dont/exist.dtd", null);
            String expected = toURLString(new File(project.getBaseDir().toURL() +
                                                   "/i/dont/exist.dtd"));
            String resultStr =
                fileURLPartWithoutLeadingSlashes((SAXSource)result);
            assertTrue("Nonexistent Catalog entry return input with a system ID like "
                       + expected + " but was " + resultStr,
                       expected.endsWith(resultStr));
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
    public void testEmptyElementIfIsReference() {
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId("PUBLIC ID ONE");
        dtd.setLocation("i/dont/exist.dtd");
        catalog.addDTD(dtd);
        project.addReference("catalog", catalog);
        try {
            catalog.setRefid(new Reference(project, "dummyref"));
            fail("Can add reference to nonexistent XMLCatalog");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one "
                         + "attribute when using refid", be.getMessage());
        }
        XMLCatalog catalog2 = newCatalog();
        catalog2.setRefid(new Reference(project, "catalog"));
        try {
            catalog2.addConfiguredXMLCatalog(catalog);
            fail("Can add nested XMLCatalog to XMLCatalog that is a reference");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
    }
    public void testCircularReferenceCheck() {
        project.addReference("catalog", catalog);
        catalog.setRefid(new Reference(project, "catalog"));
        try {
            InputSource result = catalog.resolveEntity("PUBLIC ID ONE",
                                                       "i/dont/exist.dtd");
            fail("Can make XMLCatalog a Reference to itself.");
        } catch (BuildException be) {
            assertEquals("This data type contains a circular reference.",
                         be.getMessage());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        XMLCatalog catalog1 = newCatalog();
        project.addReference("catalog1", catalog1);
        XMLCatalog catalog2 = newCatalog();
        project.addReference("catalog2", catalog2);
        XMLCatalog catalog3 = newCatalog();
        project.addReference("catalog3", catalog3);
        catalog3.setRefid(new Reference(project, "catalog1"));
        catalog2.setRefid(new Reference(project, "catalog3"));
        catalog1.setRefid(new Reference(project, "catalog2"));
        try {
            InputSource result = catalog1.resolveEntity("PUBLIC ID ONE",
                                                        "i/dont/exist.dtd");
            fail("Can make circular reference");
        } catch (BuildException be) {
            assertEquals("This data type contains a circular reference.",
                         be.getMessage());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
    }
    public void testAbsolutePath() {
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId("-//stevo//DTD doc 1.0//EN");
        String sysid = System.getProperty("root") + File.separator + "src/etc/testcases/taskdefs/optional/xml/doc.dtd";
        dtd.setLocation(sysid);
        catalog.addDTD(dtd);
        File dtdFile = project.resolveFile(sysid);
        try {
            InputSource result = catalog.resolveEntity("-//stevo//DTD doc 1.0//EN",
                                                       "nap:chemical+brothers");
            assertNotNull(result);
            assertEquals(toURLString(dtdFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
    }
    public void testSimpleEntry() {
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId("-//stevo//DTD doc 1.0//EN");
        String sysid = "src/etc/testcases/taskdefs/optional/xml/doc.dtd";
        dtd.setLocation(sysid);
        catalog.addDTD(dtd);
        File dtdFile = project.resolveFile(sysid);
        try {
            InputSource result = catalog.resolveEntity("-//stevo//DTD doc 1.0//EN",
                                                       "nap:chemical+brothers");
            assertNotNull(result);
            assertEquals(toURLString(dtdFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
    }
    public void testEntryReference() {
        String publicId = "-//stevo//DTD doc 1.0//EN";
        String sysid = "src/etc/testcases/taskdefs/optional/xml/doc.dtd";
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId(publicId);
        dtd.setLocation(sysid);
        catalog.addDTD(dtd);
        File dtdFile = project.resolveFile(sysid);
        String uri = "http://foo.com/bar/blah.xml";
        String uriLoc = "src/etc/testcases/taskdefs/optional/xml/about.xml";
        ResourceLocation entity = new ResourceLocation();
        entity.setPublicId(uri);
        entity.setLocation(uriLoc);
        catalog.addEntity(entity);
        File xmlFile = project.resolveFile(uriLoc);
        project.addReference("catalog", catalog);
        XMLCatalog catalog1 = newCatalog();
        project.addReference("catalog1", catalog1);
        XMLCatalog catalog2 = newCatalog();
        project.addReference("catalog2", catalog1);
        catalog1.setRefid(new Reference(project, "catalog"));
        catalog2.setRefid(new Reference(project, "catalog1"));
        try {
            InputSource result = catalog2.resolveEntity(publicId,
                                                        "nap:chemical+brothers");
            assertNotNull(result);
            assertEquals(toURLString(dtdFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        try {
            Source result = catalog.resolve(uri, null);
            assertNotNull(result);
            assertEquals(toURLString(xmlFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
    public void testNestedCatalog() {
        String publicId = "-//stevo//DTD doc 1.0//EN";
        String dtdLoc = "src/etc/testcases/taskdefs/optional/xml/doc.dtd";
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId(publicId);
        dtd.setLocation(dtdLoc);
        catalog.addDTD(dtd);
        File dtdFile = project.resolveFile(dtdLoc);
        String uri = "http://foo.com/bar/blah.xml";
        String uriLoc = "src/etc/testcases/taskdefs/optional/xml/about.xml";
        ResourceLocation entity = new ResourceLocation();
        entity.setPublicId(uri);
        entity.setLocation(uriLoc);
        catalog.addEntity(entity);
        File xmlFile = project.resolveFile(uriLoc);
        XMLCatalog catalog1 = newCatalog();
        catalog1.addConfiguredXMLCatalog(catalog);
        try {
            InputSource result = catalog1.resolveEntity(publicId,
                                                        "nap:chemical+brothers");
            assertNotNull(result);
            assertEquals(toURLString(dtdFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        try {
            Source result = catalog.resolve(uri, null);
            assertNotNull(result);
            assertEquals(toURLString(xmlFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
    public void testResolverBase() {
        String uri = "http://foo.com/bar/blah.xml";
        String uriLoc = "etc/testcases/taskdefs/optional/xml/about.xml";
        String base = null;
        try {
            base = toURLString(project.getBaseDir()) + "/src/";
        } catch (MalformedURLException ex) {
            fail (ex.toString());
        }
        ResourceLocation entity = new ResourceLocation();
        entity.setPublicId(uri);
        entity.setLocation(uriLoc);
        catalog.addEntity(entity);
        File xmlFile = project.resolveFile("src/" + uriLoc);
        try {
            Source result = catalog.resolve(uri, base);
            assertNotNull(result);
            assertEquals(toURLString(xmlFile),
                         result.getSystemId());
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
    public void testClasspath() {
        String publicId = "-//stevo//DTD doc 1.0//EN";
        String dtdLoc = "testcases/taskdefs/optional/xml/doc.dtd";
        String path1 = project.getBaseDir().toString() + "/src/etc";
        ResourceLocation dtd = new ResourceLocation();
        dtd.setPublicId(publicId);
        dtd.setLocation(dtdLoc);
        catalog.addDTD(dtd);
        File dtdFile = project.resolveFile("src/etc/" + dtdLoc);
        String uri = "http://foo.com/bar/blah.xml";
        String uriLoc = "etc/testcases/taskdefs/optional/xml/about.xml";
        String path2 = project.getBaseDir().toString() + "/src";
        ResourceLocation entity = new ResourceLocation();
        entity.setPublicId(uri);
        entity.setLocation(uriLoc);
        catalog.addEntity(entity);
        File xmlFile = project.resolveFile("src/" + uriLoc);
        Path aPath = new Path(project, path1);
        aPath.append(new Path(project, path2));
        catalog.setClasspath(aPath);
        try {
            InputSource result = catalog.resolveEntity(publicId,
                                                       "nap:chemical+brothers");
            assertNotNull(result);
            String resultStr = new URL(result.getSystemId()).getFile();
            assertTrue(toURLString(dtdFile).endsWith(resultStr));
        } catch (Exception e) {
            fail("resolveEntity() failed!" + e.toString());
        }
        try {
            Source result = catalog.resolve(uri, null);
            assertNotNull(result);
            String resultStr = new URL(result.getSystemId()).getFile();
            assertTrue(toURLString(xmlFile).endsWith(resultStr));
        } catch (Exception e) {
            fail("resolve() failed!" + e.toString());
        }
    }
}
