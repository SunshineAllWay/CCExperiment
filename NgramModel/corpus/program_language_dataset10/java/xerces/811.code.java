package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class IgnoreXSIType_C_CA_Test extends BaseTest {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IgnoreXSIType_C_CA_Test.class);
    }
    protected String getXMLDocument() {
        return "xsitype_C_CA.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    public IgnoreXSIType_C_CA_Test(String name) {
        super(name);
    }
    public void testDefaultDocument() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkFalseResult();
    }
    public void testDefaultFragment() {
        try {
            validateFragment();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkFalseResult();
    }
    public void testSetFalseDocument() {
        try {
            fValidator.setFeature(IGNORE_XSI_TYPE, false);
        } catch (SAXException e1) {
            Assert.fail("Problem setting feature: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkFalseResult();
    }
    public void testSetFalseFragment() {
        try {
            fValidator.setFeature(IGNORE_XSI_TYPE, false);
        } catch (SAXException e1) {
            Assert.fail("Problem setting feature: " + e1.getMessage());
        }
        try {
            validateFragment();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkFalseResult();
    }
    public void testSetTrueDocument() {
        try {
            fValidator.setFeature(IGNORE_XSI_TYPE, true);
        } catch (SAXException e1) {
            Assert.fail("Problem setting feature: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkTrueResult();
    }
    public void testSetTrueFragment() {
        try {
            fValidator.setFeature(IGNORE_XSI_TYPE, true);
        } catch (SAXException e1) {
            Assert.fail("Problem setting feature: " + e1.getMessage());
        }
        try {
            validateFragment();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkTrueResult();
    }
    private void checkTrueResult() {
        assertValidity(ItemPSVI.VALIDITY_NOTKNOWN, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_PARTIAL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertAnyType(fRootNode.getTypeDefinition());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_NOTKNOWN, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_NONE, child
                .getValidationAttempted());
        assertElementNull(child.getElementDeclaration());
        assertAnyType(child.getTypeDefinition());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("Y", child.getTypeDefinition().getName());
        assertTypeNamespaceNull(child.getTypeDefinition().getNamespace());
    }
    private void checkFalseResult() {
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("Y", fRootNode.getTypeDefinition().getName());
        assertTypeNamespaceNull(fRootNode.getTypeDefinition().getNamespace());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementNull(child.getElementDeclaration());
        assertTypeName("Y", child.getTypeDefinition().getName());
        assertTypeNamespaceNull(child.getTypeDefinition().getNamespace());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("Y", child.getTypeDefinition().getName());
        assertTypeNamespaceNull(child.getTypeDefinition().getNamespace());
    }
}