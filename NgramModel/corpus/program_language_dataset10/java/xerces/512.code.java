package org.apache.xerces.jaxp;
import java.util.Hashtable;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.SAXMessageFormatter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {
    private static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    private static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    private static final String INCLUDE_IGNORABLE_WHITESPACE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_IGNORABLE_WHITESPACE;
    private static final String CREATE_ENTITY_REF_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_ENTITY_REF_NODES_FEATURE;
    private static final String INCLUDE_COMMENTS_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_COMMENTS_FEATURE;
    private static final String CREATE_CDATA_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_CDATA_NODES_FEATURE;
    private Hashtable attributes;
    private Hashtable features;
    private Schema grammar;
    private boolean isXIncludeAware;
    private boolean fSecureProcess = false;
    public DocumentBuilder newDocumentBuilder()
        throws ParserConfigurationException 
    {
        if (grammar != null && attributes != null) {
            if (attributes.containsKey(JAXPConstants.JAXP_SCHEMA_LANGUAGE)) {
                throw new ParserConfigurationException(
                        SAXMessageFormatter.formatMessage(null, 
                        "schema-already-specified", new Object[] {JAXPConstants.JAXP_SCHEMA_LANGUAGE}));
            }
            else if (attributes.containsKey(JAXPConstants.JAXP_SCHEMA_SOURCE)) {
                throw new ParserConfigurationException(
                        SAXMessageFormatter.formatMessage(null, 
                        "schema-already-specified", new Object[] {JAXPConstants.JAXP_SCHEMA_SOURCE}));                
            }
        }
        try {
            return new DocumentBuilderImpl(this, attributes, features, fSecureProcess);
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
    }
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException
    {
        if (value == null) {
            if (attributes != null) {
                attributes.remove(name);
            }
            return;
        }
        if (attributes == null) {
            attributes = new Hashtable();
        }
        attributes.put(name, value);
        try {
            new DocumentBuilderImpl(this, attributes, features);
        } catch (Exception e) {
            attributes.remove(name);
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    public Object getAttribute(String name)
        throws IllegalArgumentException
    {
        if (attributes != null) {
            Object val = attributes.get(name);
            if (val != null) {
                return val;
            }
        }
        DOMParser domParser = null;
        try {
            domParser =
                new DocumentBuilderImpl(this, attributes, features).getDOMParser();
            return domParser.getProperty(name);
        } catch (SAXException se1) {
            try {
                boolean result = domParser.getFeature(name);
                return result ? Boolean.TRUE : Boolean.FALSE;
            } catch (SAXException se2) {
                throw new IllegalArgumentException(se1.getMessage());
            }
        }
    }
    public Schema getSchema() {
        return grammar;
    }
    public void setSchema(Schema grammar) {
        this.grammar = grammar;
    }
    public boolean isXIncludeAware() {
        return this.isXIncludeAware;
    }
    public void setXIncludeAware(boolean state) {
        this.isXIncludeAware = state;
    }
    public boolean getFeature(String name) 
        throws ParserConfigurationException {
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            return fSecureProcess;
        }
        else if (name.equals(NAMESPACES_FEATURE)) {
            return isNamespaceAware();
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            return isValidating();
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            return isXIncludeAware();
        }
        else if (name.equals(INCLUDE_IGNORABLE_WHITESPACE)) {
            return !isIgnoringElementContentWhitespace();
        }
        else if (name.equals(CREATE_ENTITY_REF_NODES_FEATURE)) {
            return !isExpandEntityReferences();
        }
        else if (name.equals(INCLUDE_COMMENTS_FEATURE)) {
            return !isIgnoringComments();
        }
        else if (name.equals(CREATE_CDATA_NODES_FEATURE)) {
            return !isCoalescing();
        }
        if (features != null) {
            Object val = features.get(name);
            if (val != null) {
                return ((Boolean) val).booleanValue();
            }
        }
        try {
            DOMParser domParser = new DocumentBuilderImpl(this, attributes, features).getDOMParser();
            return domParser.getFeature(name);
        }
        catch (SAXException e) {
            throw new ParserConfigurationException(e.getMessage());
        }
    }
    public void setFeature(String name, boolean value) 
        throws ParserConfigurationException {
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            fSecureProcess = value;
            return;
        }
        else if (name.equals(NAMESPACES_FEATURE)) {
            setNamespaceAware(value);
            return;
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            setValidating(value);
            return;
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            setXIncludeAware(value);
            return;
        }
        else if (name.equals(INCLUDE_IGNORABLE_WHITESPACE)) {
            setIgnoringElementContentWhitespace(!value);
            return;
        }
        else if (name.equals(CREATE_ENTITY_REF_NODES_FEATURE)) {
            setExpandEntityReferences(!value);
            return;
        }
        else if (name.equals(INCLUDE_COMMENTS_FEATURE)) {
            setIgnoringComments(!value);
            return;
        }
        else if (name.equals(CREATE_CDATA_NODES_FEATURE)) {
            setCoalescing(!value);
            return;
        }
        if (features == null) {
            features = new Hashtable();
        }
        features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
        try {
            new DocumentBuilderImpl(this, attributes, features);
        } 
        catch (SAXNotSupportedException e) {
            features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        } 
        catch (SAXNotRecognizedException e) {
            features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        }
    }
}
