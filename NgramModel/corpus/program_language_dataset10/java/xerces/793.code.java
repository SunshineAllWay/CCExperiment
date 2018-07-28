package schema.annotations;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
public class XSElementAnnotationsTest extends TestCase {
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
            System.err.println("SETUP FAILED: XSElementTest");
        }
    }
    protected void tearDown() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
    }
    public void testElem1Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem1",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertNull("TEST1_NO_ANNOTATION", annotation);
        XSObjectList annotations = elem.getAnnotations();
        assertEquals("TEST1_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testElem2Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem2",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertNull("TEST2_NO_ANNOTATION", annotation);
        XSObjectList annotations = elem.getAnnotations();
        assertEquals("TEST2_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testElem2SynthAnnotation() {
        String expected = trim("<annotation sn:att=\"SYNTH\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSElement\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "<documentation>SYNTHETIC_ANNOTATION</documentation>"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem2",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertEquals("TEST3_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = elem.getAnnotations();
        assertEquals(
                "TEST3_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testElem3Annotation() {
        String expected = trim("<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:sv=\"XSElement\" "
                + "xmlns:sn=\"SyntheticAnnotation\" >" + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem3",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertEquals("TEST3_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = elem.getAnnotations();
        assertEquals(
                "TEST3_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testElem4Annotation() {
        String expected = trim("<annotation sn:att=\"SYNTH\"  id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:sv=\"XSElement\" "
                + "xmlns:sn=\"SyntheticAnnotation\" >" + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem4",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertEquals("TEST4_NO_SYNTH_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = elem.getAnnotations();
        assertEquals(
                "TEST4_NO_SYNTH_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testElem5Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem5",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertNull("TEST5_NO_ANNOTATION", annotation);
        XSObjectList annotations = elem.getAnnotations();
        assertEquals("TEST5_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testElem6Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSElementTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("elem6",
                "XSElement");
        XSAnnotation annotation = elem.getAnnotation();
        assertNull("TEST5_NO_ANNOTATION", annotation);
        XSObjectList annotations = elem.getAnnotations();
        assertEquals("TEST5_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(XSElementAnnotationsTest.class);
    }
}
