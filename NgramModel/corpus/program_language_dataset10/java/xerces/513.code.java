package org.apache.xerces.jaxp;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.Schema;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.jaxp.validation.XSGrammarPoolContainer;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
public class DocumentBuilderImpl extends DocumentBuilder
        implements JAXPConstants
{
    private static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    private static final String INCLUDE_IGNORABLE_WHITESPACE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_IGNORABLE_WHITESPACE;
    private static final String CREATE_ENTITY_REF_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_ENTITY_REF_NODES_FEATURE;
    private static final String INCLUDE_COMMENTS_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_COMMENTS_FEATURE;
    private static final String CREATE_CDATA_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_CDATA_NODES_FEATURE;
    private static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    private static final String XMLSCHEMA_VALIDATION_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private final DOMParser domParser;
    private final Schema grammar;
    private final XMLComponent fSchemaValidator;
    private final XMLComponentManager fSchemaValidatorComponentManager;
    private final ValidationManager fSchemaValidationManager;
    private final UnparsedEntityHandler fUnparsedEntityHandler;
    private final ErrorHandler fInitErrorHandler;
    private final EntityResolver fInitEntityResolver;
    DocumentBuilderImpl(DocumentBuilderFactoryImpl dbf, Hashtable dbfAttrs, Hashtable features)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        this(dbf, dbfAttrs, features, false);
    }
    DocumentBuilderImpl(DocumentBuilderFactoryImpl dbf, Hashtable dbfAttrs, Hashtable features, boolean secureProcessing)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        domParser = new DOMParser();
        if (dbf.isValidating()) {
            fInitErrorHandler = new DefaultValidationErrorHandler();
            setErrorHandler(fInitErrorHandler);
        }
        else {
            fInitErrorHandler = domParser.getErrorHandler();
        }
        domParser.setFeature(VALIDATION_FEATURE, dbf.isValidating());
        domParser.setFeature(NAMESPACES_FEATURE, dbf.isNamespaceAware());
        domParser.setFeature(INCLUDE_IGNORABLE_WHITESPACE, 
                !dbf.isIgnoringElementContentWhitespace());
        domParser.setFeature(CREATE_ENTITY_REF_NODES_FEATURE,
                !dbf.isExpandEntityReferences());
        domParser.setFeature(INCLUDE_COMMENTS_FEATURE,
                !dbf.isIgnoringComments());
        domParser.setFeature(CREATE_CDATA_NODES_FEATURE,
                !dbf.isCoalescing());
        if (dbf.isXIncludeAware()) {
            domParser.setFeature(XINCLUDE_FEATURE, true);
        }
        if (secureProcessing) {
            domParser.setProperty(SECURITY_MANAGER, new SecurityManager());
        }
        this.grammar = dbf.getSchema();
        if (grammar != null) {
            XMLParserConfiguration config = domParser.getXMLParserConfiguration();
            XMLComponent validatorComponent = null;
            if (grammar instanceof XSGrammarPoolContainer) {
                validatorComponent = new XMLSchemaValidator();
                fSchemaValidationManager = new ValidationManager();
                fUnparsedEntityHandler = new UnparsedEntityHandler(fSchemaValidationManager);
                config.setDTDHandler(fUnparsedEntityHandler);
                fUnparsedEntityHandler.setDTDHandler(domParser);
                domParser.setDTDSource(fUnparsedEntityHandler);
                fSchemaValidatorComponentManager = new SchemaValidatorConfiguration(config, 
                        (XSGrammarPoolContainer) grammar, fSchemaValidationManager);
            }
            else {
                validatorComponent = new JAXPValidatorComponent(grammar.newValidatorHandler());
                fSchemaValidationManager = null;
                fUnparsedEntityHandler = null;
                fSchemaValidatorComponentManager = config;
            }
            config.addRecognizedFeatures(validatorComponent.getRecognizedFeatures());
            config.addRecognizedProperties(validatorComponent.getRecognizedProperties());
            config.setDocumentHandler((XMLDocumentHandler) validatorComponent);
            ((XMLDocumentSource)validatorComponent).setDocumentHandler(domParser);
            domParser.setDocumentSource((XMLDocumentSource) validatorComponent);
            fSchemaValidator = validatorComponent;
        }
        else {
            fSchemaValidationManager = null;
            fUnparsedEntityHandler = null;
            fSchemaValidatorComponentManager = null;
            fSchemaValidator = null;
        }
        setFeatures(features);
        setDocumentBuilderFactoryAttributes(dbfAttrs);
        fInitEntityResolver = domParser.getEntityResolver();
    }
    private void setFeatures(Hashtable features)
        throws SAXNotSupportedException, SAXNotRecognizedException {
        if (features != null) {
            Iterator entries = features.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String feature = (String) entry.getKey();
                boolean value = ((Boolean) entry.getValue()).booleanValue();
                domParser.setFeature(feature, value);
            }
        }
    }
    private void setDocumentBuilderFactoryAttributes(Hashtable dbfAttrs)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (dbfAttrs == null) {
            return;
        }
        Iterator entries = dbfAttrs.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String name = (String) entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Boolean) {
                domParser.setFeature(name, ((Boolean)val).booleanValue());
            } else {
                if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
                    if ( W3C_XML_SCHEMA.equals(val) ) {
                        if( isValidating() ) {
                            domParser.setFeature(XMLSCHEMA_VALIDATION_FEATURE, true);
                            domParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                        }
                    }
        		} else if(JAXP_SCHEMA_SOURCE.equals(name)){
               		if( isValidating() ) {
						String value=(String)dbfAttrs.get(JAXP_SCHEMA_LANGUAGE);
						if(value !=null && W3C_XML_SCHEMA.equals(value)){
            				domParser.setProperty(name, val);
						}else{
                            throw new IllegalArgumentException(
                                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                                "jaxp-order-not-supported",
                                new Object[] {JAXP_SCHEMA_LANGUAGE, JAXP_SCHEMA_SOURCE}));
						}
					}
            	} else {
                    domParser.setProperty(name, val);
				}
			}
		}
	}
    public Document newDocument() {
        return new org.apache.xerces.dom.DocumentImpl();
    }
    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }
    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException(
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                "jaxp-null-input-source", null));
        }
        if (fSchemaValidator != null) {
            if (fSchemaValidationManager != null) {
                fSchemaValidationManager.reset();
                fUnparsedEntityHandler.reset();
            }
            resetSchemaValidator();
        }
        domParser.parse(is);
        Document doc = domParser.getDocument();
        domParser.dropDocumentReferences();
        return doc;
    }
    public boolean isNamespaceAware() {
        try {
            return domParser.getFeature(NAMESPACES_FEATURE);
        } 
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }
    public boolean isValidating() {
        try {
            return domParser.getFeature(VALIDATION_FEATURE);
        } 
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }
    public boolean isXIncludeAware() {
        try {
            return domParser.getFeature(XINCLUDE_FEATURE);
        }
        catch (SAXException exc) {
            return false;
        }
    }
    public void setEntityResolver(EntityResolver er) {
        domParser.setEntityResolver(er);
    }
    public void setErrorHandler(ErrorHandler eh) {
        domParser.setErrorHandler(eh);
    }
    public Schema getSchema() {
        return grammar;
    }
    public void reset() {
        if (domParser.getErrorHandler() != fInitErrorHandler) {
            domParser.setErrorHandler(fInitErrorHandler);
        }
        if (domParser.getEntityResolver() != fInitEntityResolver) {
            domParser.setEntityResolver(fInitEntityResolver);
        }
    }
    DOMParser getDOMParser() {
        return domParser;
    }
    private void resetSchemaValidator() throws SAXException {
        try {
            fSchemaValidator.reset(fSchemaValidatorComponentManager);
        }
        catch (XMLConfigurationException e) {
            throw new SAXException(e);
        }
    }
}
