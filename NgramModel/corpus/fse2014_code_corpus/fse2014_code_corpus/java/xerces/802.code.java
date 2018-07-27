package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
public class BasicTest extends BaseTest {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BasicTest.class);
    }
    protected String getXMLDocument() {
        return "base.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    public BasicTest(String name) {
        super(name);
    }
    public void testSimpleValidation() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        doValidityAsserts();
    }
    public void testSimpleValidationWithTrivialXSIType() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance", "type", "X");
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        doValidityAsserts();
    }
    private void doValidityAsserts() {
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertElementNamespaceNull(fRootNode.getElementDeclaration()
                .getNamespace());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
        assertTypeNamespaceNull(fRootNode.getTypeDefinition().getNamespace());
    }
}
