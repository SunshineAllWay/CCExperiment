package schema.config;
import junit.framework.Assert;
import org.apache.xerces.xs.ItemPSVI;
public class UseGrammarPoolOnly_False_Test extends BaseTest {
    private final static String UNKNOWN_TYPE_ERROR = "cvc-type.1";
    private final static String INVALID_DERIVATION_ERROR = "cvc-elt.4.3";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(UseGrammarPoolOnly_False_Test.class);
    }
    protected String getXMLDocument() {
        return "otherNamespace.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { UNKNOWN_TYPE_ERROR, INVALID_DERIVATION_ERROR };
    }
    protected boolean getUseGrammarPoolOnly() {
        return false;
    }
    public UseGrammarPoolOnly_False_Test(String name) {
        super(name);
    }
    public void testUsingOnlyGrammarPool() {
        try {
            validateDocument();
        } 
        catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertElementNamespace("xslt.unittests", fRootNode
                .getElementDeclaration().getNamespace());
        assertTypeName("W", fRootNode.getTypeDefinition().getName());
        assertTypeNamespace("xslt.unittests", fRootNode.getTypeDefinition()
                .getNamespace());
    }
}
