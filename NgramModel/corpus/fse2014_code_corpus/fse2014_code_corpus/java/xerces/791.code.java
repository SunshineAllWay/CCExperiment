package schema.annotations;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeGroupDefinition;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
public class XSAttributeGroupAnnotationsTest extends TestCase {
    private XSLoader fSchemaLoader;
    private DOMConfiguration fConfig;
    protected void setUp() {
        try {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                    "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry
                    .newInstance();
            XSImplementation impl = (XSImplementation) registry
                    .getDOMImplementation("XS-Loader");
            fSchemaLoader = impl.createXSLoader(null);
            fConfig = fSchemaLoader.getConfig();
            fConfig.setParameter("validate", Boolean.TRUE);
        } catch (Exception e) {
            fail("Expecting a NullPointerException");
            System.err.println("SETUP FAILED: XSAttributeGroupAnnotationsTest");
        }
    }
    protected void tearDown() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
    }
    public void testNoAnnotation() {
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest01.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSAnnotation annotation = AG.getAnnotation();
        assertNull(annotation);
    }
    public void testNoAnnotations() {
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest01.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSObjectList annotations = AG.getAnnotations();
        assertEquals(0, annotations.getLength());
    }
    public void testAnnotation() {
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest02.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSAnnotation annotation = AG.getAnnotation();
        String expectedResult = "<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSAttributeGroupAnnotationsTest\" >"
                + "<appinfo source=\"None\">"
                + "<!-- No Appinfo -->"
                + "</appinfo><documentation>ANNOT1 should be seen</documentation>"
                + "</annotation>";
        String actual = annotation.getAnnotationString();
        assertEquals(trim(expectedResult), trim(actual));
    }
    public void testAnnotations() {
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest02.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSObjectList annotations = AG.getAnnotations();
        String expectedResult = "<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSAttributeGroupAnnotationsTest\" >"
                + "<appinfo source=\"None\">"
                + "<!-- No Appinfo -->"
                + "</appinfo><documentation>ANNOT1 should be seen</documentation>"
                + "</annotation>";
        for (int i = 0; i < annotations.getLength(); i++) {
            XSAnnotation annotation = (XSAnnotation) annotations.item(i);
            String actual = annotation.getAnnotationString();
            assertEquals(trim(expectedResult), trim(actual));
        }
        AG = model.getAttributeGroup("AG2", "XSAttributeGroupAnnotationsTest");
        annotations = AG.getAnnotations();
        expectedResult = "<annotation id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSAttributeGroupAnnotationsTest\" >"
                + "</annotation>";
        for (int i = 0; i < annotations.getLength(); i++) {
            XSAnnotation annotation = (XSAnnotation) annotations.item(i);
            String actual = annotation.getAnnotationString();
            assertEquals(trim(expectedResult), trim(actual));
        }
    }
    public void testSyntheticAnnotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest03.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSAnnotation annotation = AG.getAnnotation();
        assertNotNull("Synthetic Annotation Null", annotation);
    }
    public void testSyntheticAnnotation6() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest03.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSObjectList annotations = AG.getAnnotations();
        assertEquals("Synthetic Annotation Empty", 1, annotations.getLength());
    }
    public void testNoSyntheticAnnotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest03.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSAnnotation annotation = AG.getAnnotation();
        assertNull("Synthetic Annotation Not Null", annotation);
    }
    public void testSyntheticAnnotationsAbsent() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest03.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG1",
                "XSAttributeGroupAnnotationsTest");
        XSObjectList annotations = AG.getAnnotations();
        assertEquals("Synthetic Annotation Empty", 0, annotations.getLength());
    }
    public void testAnnotationsInGroup() {
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSAttributeGroupAnnotationsTest04.xsd"));
        XSAttributeGroupDefinition AG = model.getAttributeGroup("AG",
                "XSAttributeGroupAnnotationsTest");
        XSObjectList annotations = AG.getAnnotations();
        String expectedResult = "<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSAttributeGroupAnnotationsTest\" >"
                + "<appinfo source=\"None\">"
                + "<!-- No Appinfo -->"
                + "</appinfo><documentation>ANNOT1 should be seen</documentation>"
                + "</annotation>";
        for (int i = 0; i < annotations.getLength(); i++) {
            XSAnnotation annotation = (XSAnnotation) annotations.item(i);
            String actual = annotation.getAnnotationString();
            assertEquals(trim(expectedResult), trim(actual));
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(XSAttributeGroupAnnotationsTest.class);
    }
}
