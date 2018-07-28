package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class IgnoreXSIType_A_A_Test extends BaseTest {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IgnoreXSIType_A_A_Test.class);
    }
    protected String getXMLDocument() {
        return "xsitype_A_A.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    public IgnoreXSIType_A_A_Test(String name) {
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
        checkResult();
    }
    private void checkFalseResult() {
        checkResult();
    }
    private void checkResult() {
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("Y", fRootNode.getTypeDefinition().getName());
        assertTypeNamespaceNull(fRootNode.getTypeDefinition().getNamespace());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("Y", child.getTypeDefinition().getName());
        assertTypeNamespaceNull(child.getTypeDefinition().getNamespace());
    }
}