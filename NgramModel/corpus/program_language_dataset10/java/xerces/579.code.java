package org.apache.xerces.parsers;
import java.io.IOException;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public abstract class XMLParser {
    protected static final String ENTITY_RESOLVER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    private static final String[] RECOGNIZED_PROPERTIES = {
        ENTITY_RESOLVER,
        ERROR_HANDLER,
    };
    protected final XMLParserConfiguration fConfiguration;
    protected XMLParser(XMLParserConfiguration config) {
        fConfiguration = config;
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
    } 
    public void parse(XMLInputSource inputSource) 
        throws XNIException, IOException {
        reset();
        fConfiguration.parse(inputSource);
    } 
    protected void reset() throws XNIException {
    } 
} 
