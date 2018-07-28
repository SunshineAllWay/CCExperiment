package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xs.opti.SchemaDOMParser;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SAXLocatorWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
final class SchemaContentHandler implements ContentHandler {
    private SymbolTable fSymbolTable;
    private SchemaDOMParser fSchemaDOMParser;
    private final SAXLocatorWrapper fSAXLocatorWrapper = new SAXLocatorWrapper();
    private NamespaceSupport fNamespaceContext = new NamespaceSupport();
    private boolean fNeedPushNSContext;
    private boolean fNamespacePrefixes = false;
    private boolean fStringsInternalized = false;
    private final QName fElementQName = new QName();
    private final QName fAttributeQName = new QName();
    private final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private final XMLString fTempString = new XMLString();
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    public SchemaContentHandler() {}
    public Document getDocument() {
        return fSchemaDOMParser.getDocument();
    }
    public void setDocumentLocator(Locator locator) {
        fSAXLocatorWrapper.setLocator(locator);
    }
    public void startDocument() throws SAXException {
        fNeedPushNSContext = true;
        fNamespaceContext.reset();
        try {
            fSchemaDOMParser.startDocument(fSAXLocatorWrapper, null, fNamespaceContext, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }
    public void endDocument() throws SAXException {
        fSAXLocatorWrapper.setLocator(null);
        try {
            fSchemaDOMParser.endDocument(null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (fNeedPushNSContext) {
            fNeedPushNSContext = false;
            fNamespaceContext.pushContext();
        }
        if (!fStringsInternalized) {
            prefix = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
            uri = (uri != null && uri.length() > 0) ? fSymbolTable.addSymbol(uri) : null;
        }
        else {
            if (prefix == null) {
                prefix = XMLSymbols.EMPTY_STRING;
            }
            if (uri != null && uri.length() == 0) {
                uri = null;
            }
        }
        fNamespaceContext.declarePrefix(prefix, uri);
    }
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (fNeedPushNSContext) {
            fNamespaceContext.pushContext();
        }
        fNeedPushNSContext = true;
        fillQName(fElementQName, uri, localName, qName);
        fillXMLAttributes(atts);
        if (!fNamespacePrefixes) {
            final int prefixCount = fNamespaceContext.getDeclaredPrefixCount();
            if (prefixCount > 0) {
                addNamespaceDeclarations(prefixCount);
            }
        }
        try {
            fSchemaDOMParser.startElement(fElementQName, fAttributes, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        fillQName(fElementQName, uri, localName, qName);
        try {
            fSchemaDOMParser.endElement(fElementQName, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
        finally {
            fNamespaceContext.popContext();
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            fTempString.setValues(ch, start, length);
            fSchemaDOMParser.characters(fTempString, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            fTempString.setValues(ch, start, length);
            fSchemaDOMParser.ignorableWhitespace(fTempString, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            fTempString.setValues(data.toCharArray(), 0, data.length());
            fSchemaDOMParser.processingInstruction(target, fTempString, null);
        }
        catch (XMLParseException e) {
            convertToSAXParseException(e);
        }
        catch (XNIException e) {
            convertToSAXException(e);
        }
    }
    public void skippedEntity(String arg) throws SAXException {
    }
    private void fillQName(QName toFill, String uri, String localpart, String rawname) {
        if (!fStringsInternalized) {
            uri = (uri != null && uri.length() > 0) ? fSymbolTable.addSymbol(uri) : null;
            localpart = (localpart != null) ? fSymbolTable.addSymbol(localpart) : XMLSymbols.EMPTY_STRING;
            rawname = (rawname != null) ? fSymbolTable.addSymbol(rawname) : XMLSymbols.EMPTY_STRING;
        }
        else {
            if (uri != null && uri.length() == 0) {
                uri = null;
            }
            if (localpart == null) {
                localpart = XMLSymbols.EMPTY_STRING;
            }
            if (rawname == null) {
                rawname = XMLSymbols.EMPTY_STRING;
            }
        }
        String prefix = XMLSymbols.EMPTY_STRING;
        int prefixIdx = rawname.indexOf(':');
        if (prefixIdx != -1) {
            prefix = fSymbolTable.addSymbol(rawname.substring(0, prefixIdx));
            if (localpart == XMLSymbols.EMPTY_STRING) {
                localpart = fSymbolTable.addSymbol(rawname.substring(prefixIdx + 1));
            }
        }
        else if (localpart == XMLSymbols.EMPTY_STRING) {
            localpart = rawname;
        }
        toFill.setValues(prefix, localpart, rawname, uri);
    }
    private void fillXMLAttributes(Attributes atts) {
        fAttributes.removeAllAttributes();
        final int attrCount = atts.getLength();
        for (int i = 0; i < attrCount; ++i) {
            fillQName(fAttributeQName, atts.getURI(i), atts.getLocalName(i), atts.getQName(i));
            String type = atts.getType(i);
            fAttributes.addAttributeNS(fAttributeQName, (type != null) ? type : XMLSymbols.fCDATASymbol, atts.getValue(i));
            fAttributes.setSpecified(i, true);
        }
    }
    private void addNamespaceDeclarations(final int prefixCount) {
        String prefix = null;
        String localpart = null;
        String rawname = null;
        String nsPrefix = null;
        String nsURI = null;
        for (int i = 0; i < prefixCount; ++i) {
            nsPrefix = fNamespaceContext.getDeclaredPrefixAt(i);
            nsURI = fNamespaceContext.getURI(nsPrefix);
            if (nsPrefix.length() > 0) {
                prefix = XMLSymbols.PREFIX_XMLNS;
                localpart = nsPrefix;
                fStringBuffer.clear();
                fStringBuffer.append(prefix);
                fStringBuffer.append(':');
                fStringBuffer.append(localpart);
                rawname = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
            }
            else {
                prefix = XMLSymbols.EMPTY_STRING;
                localpart = XMLSymbols.PREFIX_XMLNS;
                rawname = XMLSymbols.PREFIX_XMLNS;
            }
            fAttributeQName.setValues(prefix, localpart, rawname, NamespaceContext.XMLNS_URI);
            fAttributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, 
                    (nsURI != null) ? nsURI : XMLSymbols.EMPTY_STRING);
        }
    }
    public void reset(SchemaDOMParser schemaDOMParser, SymbolTable symbolTable,
            boolean namespacePrefixes, boolean stringsInternalized) {
        fSchemaDOMParser = schemaDOMParser;
        fSymbolTable = symbolTable;
        fNamespacePrefixes = namespacePrefixes;
        fStringsInternalized = stringsInternalized;
    }
    static void convertToSAXParseException(XMLParseException e) throws SAXException {
        Exception ex = e.getException();
        if (ex == null) {
            LocatorImpl locatorImpl = new LocatorImpl();
            locatorImpl.setPublicId(e.getPublicId());
            locatorImpl.setSystemId(e.getExpandedSystemId());
            locatorImpl.setLineNumber(e.getLineNumber());
            locatorImpl.setColumnNumber(e.getColumnNumber());
            throw new SAXParseException(e.getMessage(), locatorImpl);
        }
        if (ex instanceof SAXException) {
            throw (SAXException) ex;
        }
        throw new SAXException(ex);
    }
    static void convertToSAXException(XNIException e) throws SAXException {
        Exception ex = e.getException();
        if (ex == null) {
            throw new SAXException(e.getMessage());
        }
        if (ex instanceof SAXException) {
            throw (SAXException) ex;
        }
        throw new SAXException(ex);
    }
} 
