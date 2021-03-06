package schema.config;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import junit.framework.TestCase;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
public class FeaturePropagationTest extends TestCase {
    public final String FEATURE_STRING_DEFAULT_FALSE = "http://apache.org/xml/features/honour-all-schemaLocations";
    public final String FEATURE_STRING_DEFAULT_TRUE = "http://apache.org/xml/features/validation/schema-full-checking";
    public final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(FeaturePropagationTest.class);
    }
    public FeaturePropagationTest(String name) {
        super(name);
    }
    public void testPropertyReset() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = makeSchema(factory, null);
        Validator validator = schema.newValidator();
        Object beforeReset = validator.getProperty(SECURITY_MANAGER);
        validator.setProperty(SECURITY_MANAGER, null);
        Object changed = validator.getProperty(SECURITY_MANAGER);
        assertFalse("Property value should have changed after calling setProperty().", beforeReset != changed);
        validator.reset();
        Object afterReset = validator.getProperty(SECURITY_MANAGER);
        assertTrue("Property value should be the same after calling reset()", beforeReset == afterReset);
    }
    public void testFeatureReset() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = makeSchema(factory, null);
        Validator validator = schema.newValidator();
        validator.setFeature(FEATURE_STRING_DEFAULT_TRUE, false);
        validator.setFeature(FEATURE_STRING_DEFAULT_FALSE, true);
        validator.reset();
        boolean value = validator.getFeature(FEATURE_STRING_DEFAULT_TRUE);
        assertTrue("After reset, value of feature on Validator should be true.", value);
        value = validator.getFeature(FEATURE_STRING_DEFAULT_FALSE);
        assertFalse("After reset, value of feature on Validator should be false.", value);        
    }
    public void testSecureProcessingFeaturePropagationAndReset() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        boolean value;
        value = factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        assertFalse("Default value of feature on SchemaFactory should have been false.", value);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Schema schema = makeSchema(factory, null);
        Validator validator = schema.newValidator();
        value = validator.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        assertTrue("Value of feature on Validator should have been true.", value);
        validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        value = validator.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        assertFalse("Value of feature on Validator should have been false.", value);
        validator.reset();
        value = validator.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        assertTrue("After reset, value of feature on Validator should be true.", value);
    }
    public void testFeaturePropagationNull() throws Exception {
        checkFeaturesOnValidator(null);
    }
    public void testFeaturePropagationEmpty() throws Exception {
        checkFeaturesOnValidator(new Source[] {});
    }
    public void testFeaturePropagationSingle() throws Exception {
        checkFeaturesOnValidator(new Source[] {makeSource("base.xsd")});
    }
    public void testFeaturePropagationMultiple() throws Exception {
        checkFeaturesOnValidator(new Source[] {makeSource("base.xsd"), makeSource("idc.xsd")});
    }
    private void checkFeaturesOnValidator(Source[] sources) throws Exception {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = makeSchema(factory, sources);
            Validator validator = schema.newValidator();
            boolean value;
            value = validator.getFeature(FEATURE_STRING_DEFAULT_TRUE);
            assertTrue("Default value of feature on Validator should have been true.", value);
            value = validator.getFeature(FEATURE_STRING_DEFAULT_FALSE);
            assertFalse("Default value of feature on Validator should have been false.", value);
            factory.setFeature(FEATURE_STRING_DEFAULT_TRUE, false);
            factory.setFeature(FEATURE_STRING_DEFAULT_FALSE, true);
            schema = makeSchema(factory, sources);
            validator = schema.newValidator();
            value = validator.getFeature(FEATURE_STRING_DEFAULT_TRUE);
            assertFalse("Value of feature on Validator should have been false.", value);
            value = validator.getFeature(FEATURE_STRING_DEFAULT_FALSE);
            assertTrue("Value of feature on Validator should have been true.", value);
            factory.setFeature(FEATURE_STRING_DEFAULT_TRUE, true);
            factory.setFeature(FEATURE_STRING_DEFAULT_FALSE, false);
            value = validator.getFeature(FEATURE_STRING_DEFAULT_TRUE);
            assertFalse("Value of feature on Validator should have stayed false.", value);
            value = validator.getFeature(FEATURE_STRING_DEFAULT_FALSE);
            assertTrue("Value of feature on Validator should have stayed true.", value);            
        }
        catch (SAXNotRecognizedException e) {
            fail(e.getMessage());
        }
        catch (SAXNotSupportedException e) {
            fail(e.getMessage());
        }
    }
    private Schema makeSchema(SchemaFactory factory, Source[] sources) throws SAXException {
        if (sources == null) {
            return factory.newSchema();
        }
        else {
            return factory.newSchema(sources);
        }
    }
    private Source makeSource(String xsd) throws FileNotFoundException {
        String packageDir = this.getClass().getPackage().getName().replace('.',
            File.separatorChar);
        String schemaPath = packageDir + File.separatorChar + xsd;
        URL schemaURL = ClassLoader.getSystemResource(schemaPath);
        if (schemaURL == null) {
            throw new FileNotFoundException("Couldn't find schema file: " + schemaPath);
        }
        return new StreamSource(schemaURL.toExternalForm());
    }
}
