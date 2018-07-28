package schema.config;
import junit.framework.Assert;
import org.apache.xerces.xs.ItemPSVI;
public class UseGrammarPoolOnly_True_Test extends BaseTest {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(UseGrammarPoolOnly_True_Test.class);
    }
    protected String getXMLDocument() {
        return "otherNamespace.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected boolean getUseGrammarPoolOnly() {
        return true;
    }
    public UseGrammarPoolOnly_True_Test(String name) {
        super(name);
    }
    public void testUsingOnlyGrammarPool() {
        try {
            validateDocument();
        } 
        catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_NOTKNOWN, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_NONE, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertAnyType(fRootNode.getTypeDefinition());
    }
}
