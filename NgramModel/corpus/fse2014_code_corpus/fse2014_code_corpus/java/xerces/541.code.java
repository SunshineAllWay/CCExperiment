package org.apache.xerces.jaxp.validation;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.XML11Configuration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.xml.sax.SAXException;
final class StreamValidatorHelper implements ValidatorHelper {
    private static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;    
    private static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    private static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private SoftReference fConfiguration = new SoftReference(null);
    private final XMLSchemaValidator fSchemaValidator;
    private final XMLSchemaValidatorComponentManager fComponentManager;
    private SoftReference fParser = new SoftReference(null);
    private SerializerFactory fSerializerFactory;
    public StreamValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
    }
    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result instanceof StreamResult || result == null) {
            final StreamSource streamSource = (StreamSource) source;
            final StreamResult streamResult = (StreamResult) result;
            XMLInputSource input = new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), null);
            input.setByteStream(streamSource.getInputStream());
            input.setCharacterStream(streamSource.getReader());
            boolean newConfig = false;
            XMLParserConfiguration config = (XMLParserConfiguration) fConfiguration.get();
            if (config == null) {
                config = initialize();
                newConfig = true;
            }
            else if (fComponentManager.getFeature(PARSER_SETTINGS)) {
                config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
                config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
                config.setProperty(SECURITY_MANAGER, fComponentManager.getProperty(SECURITY_MANAGER));
            }
            fComponentManager.reset();
            if (streamResult != null) {
                if (fSerializerFactory == null) {
                    fSerializerFactory = SerializerFactory.getSerializerFactory(Method.XML);
                }
                Serializer ser;
                if (streamResult.getWriter() != null) {
                    ser = fSerializerFactory.makeSerializer(streamResult.getWriter(), new OutputFormat());
                }
                else if (streamResult.getOutputStream() != null) {
                    ser = fSerializerFactory.makeSerializer(streamResult.getOutputStream(), new OutputFormat());
                }
                else if (streamResult.getSystemId() != null) {
                    String uri = streamResult.getSystemId();
                    OutputStream out = XMLEntityManager.createOutputStream(uri);
                    ser = fSerializerFactory.makeSerializer(out, new OutputFormat());
                }
                else {
                    throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "StreamResultNotInitialized", null));
                }
                SAXParser parser = (SAXParser) fParser.get();
                if (newConfig || parser == null) {
                    parser = new SAXParser(config);
                    fParser = new SoftReference(parser);
                }
                else {
                    parser.reset();
                }
                config.setDocumentHandler(fSchemaValidator);
                fSchemaValidator.setDocumentHandler(parser);
                parser.setContentHandler(ser.asContentHandler());
            }
            else {
                fSchemaValidator.setDocumentHandler(null);
            }
            try {
                config.parse(input);
            }
            catch (XMLParseException e) {
                throw Util.toSAXParseException(e);
            }
            catch (XNIException e) {
                throw Util.toSAXException(e);
            }
            finally {
                fSchemaValidator.setDocumentHandler(null);
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    private XMLParserConfiguration initialize() {
        XML11Configuration config = new XML11Configuration();
        config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
        config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
        XMLErrorReporter errorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        config.setProperty(ERROR_REPORTER, errorReporter);
        if (errorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            errorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            errorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        config.setProperty(SYMBOL_TABLE, fComponentManager.getProperty(SYMBOL_TABLE));
        config.setProperty(VALIDATION_MANAGER, fComponentManager.getProperty(VALIDATION_MANAGER));
        config.setProperty(SECURITY_MANAGER, fComponentManager.getProperty(SECURITY_MANAGER));
        config.setDocumentHandler(fSchemaValidator);
        config.setDTDHandler(null);
        config.setDTDContentModelHandler(null);
        fConfiguration = new SoftReference(config);
        return config;
    }
} 
