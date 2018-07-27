package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class IdentityConstraintCheckingTest extends BaseTest {
    public static final String DUPLICATE_UNIQUE = "cvc-identity-constraint.4.1";
    public static final String DUPLICATE_KEY = "cvc-identity-constraint.4.2.2";
    public static final String INVALID_KEYREF = "cvc-identity-constraint.4.3";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IdentityConstraintCheckingTest.class);
    }
    protected String getXMLDocument() {
        return "idc.xml";
    }
    protected String getSchemaFile() {
        return "idc.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { DUPLICATE_UNIQUE, DUPLICATE_KEY, INVALID_KEYREF };
    }
    public IdentityConstraintCheckingTest(String name) {
        super(name);
    }
    public void testDefault() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    public void testSetFalse() {
        try {
            fValidator.setFeature(IDC_CHECKING, false);
        } catch (SAXException e) {
            Assert.fail("Error setting feature.");
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkValidResult();
    }
    public void testSetTrue() {
        try {
            fValidator.setFeature(IDC_CHECKING, true);
        } catch (SAXException e) {
            Assert.fail("Error setting feature.");
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    private void checkDefault() {
        assertError(DUPLICATE_UNIQUE);
        assertError(DUPLICATE_KEY);
        assertError(INVALID_KEYREF);
        assertValidity(ItemPSVI.VALIDITY_INVALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("itemList", fRootNode.getElementDeclaration()
                .getName());
        assertTypeName("itemListType", fRootNode.getTypeDefinition().getName());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_INVALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(3);
        assertValidity(ItemPSVI.VALIDITY_INVALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(4);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("itemRef", child.getElementDeclaration().getName());
        assertTypeName("string", child.getTypeDefinition().getName());
    }
    private void checkValidResult() {
        assertNoError(DUPLICATE_UNIQUE);
        assertNoError(DUPLICATE_KEY);
        assertNoError(INVALID_KEYREF);
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("itemList", fRootNode.getElementDeclaration()
                .getName());
        assertTypeName("itemListType", fRootNode.getTypeDefinition().getName());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(3);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("item", child.getElementDeclaration().getName());
        assertTypeName("itemType", child.getTypeDefinition().getName());
        child = super.getChild(4);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("itemRef", child.getElementDeclaration().getName());
        assertTypeName("string", child.getTypeDefinition().getName());
    }
}
