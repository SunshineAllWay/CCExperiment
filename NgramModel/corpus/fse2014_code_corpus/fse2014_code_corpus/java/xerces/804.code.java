package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
public class FixedAttrTest extends BaseTest {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(FixedAttrTest.class);
    }
    protected String getXMLDocument() {
        return "fixedAttr.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    public FixedAttrTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testDefault() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("B", child.getElementDeclaration().getName());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("D", child.getElementDeclaration().getName());
    }
}
