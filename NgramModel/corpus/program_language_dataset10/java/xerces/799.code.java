package schema.annotations;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeGroupDefinition;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSWildcard;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
public class XSWildcardAnnotationsTest extends TestCase {
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
            System.err.println("SETUP FAILED: XSWildcardTest");
        }
    }
    protected void tearDown() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
    }
    public void testAttrWCNoAnnotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("test",
                "XSWildcardTest");
        XSWildcard attrWC = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getAttributeWildcard();
        XSAnnotation annotation = attrWC.getAnnotation();
        assertNull("TEST1_ATTRWC_NO_ANNOTATION", annotation);
        XSObjectList annotations = attrWC.getAnnotations();
        assertEquals("TEST1_ATTRWC_NO_ANNOTATIONS", 0, annotations.getLength());
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        elem = model.getElementDeclaration("test", "XSWildcardTest");
        attrWC = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getAttributeWildcard();
        annotation = attrWC.getAnnotation();
        assertNotNull("TEST1_ATTRWC_SYNTH_ANNOTATION", annotation);
        annotations = attrWC.getAnnotations();
        assertEquals("TEST1_ATTRWC_SYNTHO_ANNOTATIONS", 1, annotations
                .getLength());
    }
    public void testAttrWCAnnotation() {
        attrWCAnnotationTest(false);
        attrWCAnnotationTest(true);
    }
    private XSModel getXSModel(boolean synthetic) {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        return model;
    }
    private void attrWCAnnotationTest(boolean synthetic) {
        String expected = trim("<annotation sn:attr=\"SYNTH\" "
                + "id=\"ANNOT4\" xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSWildcardTest\" xmlns:sn=\"SyntheticAnnotation\" > "
                + "<appinfo>APPINFO</appinfo><documentation>DOC</documentation>"
                + "</annotation>");
        XSModel model = getXSModel(synthetic);
        XSElementDeclaration elem = model.getElementDeclaration("root",
                "XSWildcardTest");
        XSWildcard attrWC = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getAttributeWildcard();
        XSAnnotation annotation = attrWC.getAnnotation();
        assertEquals("TEST2_ATTRWC_ANNOTATION_" + synthetic, expected,
                trim(annotation.getAnnotationString()));
        XSObjectList annotations = attrWC.getAnnotations();
        assertEquals(
                "TEST2_ATTRWC_ANNOTATIONS_" + synthetic,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testAttGpWCAnnotations() {
        attrgpWCAnnotationTest(false);
        attrgpWCAnnotationTest(true);
    }
    private void attrgpWCAnnotationTest(boolean synthetic) {
        String expected = trim("<annotation sn:attr=\"SYNTH\" "
                + "id=\"ANNOT5\" xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSWildcardTest\" xmlns:sn=\"SyntheticAnnotation\" > "
                + "<appinfo>APPINFO</appinfo><documentation>DOC</documentation>"
                + "</annotation>");
        XSModel model = getXSModel(synthetic);
        XSAttributeGroupDefinition attgp = model.getAttributeGroup("attgrp",
                "XSWildcardTest");
        XSWildcard attrWC = attgp.getAttributeWildcard();
        XSAnnotation annotation = attrWC.getAnnotation();
        assertEquals("TEST3_ATTRWC_ANNOTATION_" + synthetic, expected,
                trim(annotation.getAnnotationString()));
        XSObjectList annotations = attrWC.getAnnotations();
        assertEquals(
                "TEST3_ATTRWC_ANNOTATIONS_" + synthetic,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testElemWCNoAnnotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("root",
                "XSWildcardTest");
        XSParticle seq = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getParticle();
        XSModelGroup mg = (XSModelGroup) seq.getTerm();
        XSParticle anyPart = (XSParticle) mg.getParticles().item(1);
        XSWildcard elemWC = (XSWildcard) anyPart.getTerm();
        XSAnnotation annotation = elemWC.getAnnotation();
        assertNull("TEST4_ELEMWC_NO_ANNOTATION", annotation);
        XSObjectList annotations = elemWC.getAnnotations();
        assertEquals("TEST4_ELEMWC_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testElemWCSynthAnnotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("root",
                "XSWildcardTest");
        XSParticle seq = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getParticle();
        XSModelGroup mg = (XSModelGroup) seq.getTerm();
        XSParticle anyPart = (XSParticle) mg.getParticles().item(2);
        XSWildcard elemWC = (XSWildcard) anyPart.getTerm();
        XSAnnotation annotation = elemWC.getAnnotation();
        assertNotNull("TEST5_ELEMWC_SYNTH_ANNOTATION", annotation);
        XSObjectList annotations = elemWC.getAnnotations();
        assertEquals("TEST5_ELEMWC_SYNTH_ANNOTATIONS", 1, annotations
                .getLength());
    }
    public void testElemWCAnnotation() {
        String expected = trim("<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSWildcardTest\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("root",
                "XSWildcardTest");
        XSParticle seq = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getParticle();
        XSModelGroup mg = (XSModelGroup) seq.getTerm();
        XSParticle anyPart = (XSParticle) mg.getParticles().item(3);
        XSWildcard elemWC = (XSWildcard) anyPart.getTerm();
        XSAnnotation annotation = elemWC.getAnnotation();
        assertEquals("TEST6_ELEMWC_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = elemWC.getAnnotations();
        assertEquals(
                "TEST6_ELEMWC_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testElemWCAnnotationnoSynth() {
        String expected = trim("<annotation sn:attr=\"SYNTH\"  id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSWildcardTest\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "<appinfo>APPINFO</appinfo><documentation>DOC</documentation>"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSWildcardTest01.xsd"));
        XSElementDeclaration elem = model.getElementDeclaration("root",
                "XSWildcardTest");
        XSParticle seq = ((XSComplexTypeDefinition) elem.getTypeDefinition())
                .getParticle();
        XSModelGroup mg = (XSModelGroup) seq.getTerm();
        XSParticle anyPart = (XSParticle) mg.getParticles().item(4);
        XSWildcard elemWC = (XSWildcard) anyPart.getTerm();
        XSAnnotation annotation = elemWC.getAnnotation();
        assertEquals("TEST7_ELEMWC_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = elemWC.getAnnotations();
        assertEquals(
                "TEST7_ELEMWC_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(XSWildcardAnnotationsTest.class);
    }
}
