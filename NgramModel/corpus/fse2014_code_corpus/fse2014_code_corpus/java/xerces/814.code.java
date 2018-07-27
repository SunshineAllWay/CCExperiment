package schema.config;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class RootTypeDefinitionTest extends BaseTest {
    private QName unknownType;
    private QName typeX;
    private QName typeY;
    private QName typeZ;
    private QName typeOtherNamespace;
    private final static String UNKNOWN_TYPE_ERROR = "cvc-type.1";
    private final static String INVALID_DERIVATION_ERROR = "cvc-elt.4.3";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RootTypeDefinitionTest.class);
    }
    protected String getXMLDocument() {
        return "base.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { UNKNOWN_TYPE_ERROR, INVALID_DERIVATION_ERROR };
    }
    public RootTypeDefinitionTest(String name) {
        super(name);
        unknownType = new QName("W");
        typeX = new QName("X");
        typeY = new QName("Y");
        typeZ = new QName("Z");
        typeOtherNamespace = new QName("xslt.unittests", "W", "unit");
    }
    public void testDefault() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    public void testUsingDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setAttribute(ROOT_TYPE, typeX);
        dbf.setAttribute(DOCUMENT_CLASS_NAME,"org.apache.xerces.dom.PSVIDocumentImpl");
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        dbf.setSchema(sf.newSchema(fSchemaURL));
        DocumentBuilder db = dbf.newDocumentBuilder();
        fDocument = db.parse(fDocumentURL.toExternalForm());
        fRootNode = (ElementPSVI) fDocument.getDocumentElement();
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingNull() {
        try {
            fValidator.setProperty(ROOT_TYPE, null);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    public void testSettingToUnknownType() {
        try {
            fValidator.setProperty(ROOT_TYPE, unknownType);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertError(UNKNOWN_TYPE_ERROR);
        checkDefault();
    }
    public void testSettingToEqualType() {
        try {
            fValidator.setProperty(ROOT_TYPE, typeX);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingToDerivedType() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(null, "attr", "typeY");
        try {
            fValidator.setProperty(ROOT_TYPE, typeY);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("Y", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingToNonDerivedType() {
        try {
            fValidator.setProperty(ROOT_TYPE, typeZ);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("Z", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingToOtherSchemaType() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(SchemaSymbols.URI_XSI,
                SchemaSymbols.XSI_SCHEMALOCATION,
        "xslt.unittests otherNamespace.xsd");
        try {
            fValidator.setProperty(ROOT_TYPE, typeOtherNamespace);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("W", fRootNode.getTypeDefinition().getName());
        assertTypeNamespace("xslt.unittests", fRootNode.getTypeDefinition()
                .getNamespace());
    }
    public void testSettingTypeAndXSIType() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(null, "attr", "typeY");
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(SchemaSymbols.URI_XSI,
                SchemaSymbols.XSI_TYPE, "Y");
        try {
            fValidator.setProperty(ROOT_TYPE, typeX);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("Y", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingTypeAndInvalidXSIType() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(SchemaSymbols.URI_XSI,
                SchemaSymbols.XSI_TYPE, "Z");
        try {
            fValidator.setProperty(ROOT_TYPE, typeX);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertError(INVALID_DERIVATION_ERROR);
        assertValidity(ItemPSVI.VALIDITY_INVALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("Z", fRootNode.getTypeDefinition().getName());
    }
    private void checkDefault() {
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
    }
}
