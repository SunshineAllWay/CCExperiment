package org.apache.xerces.jaxp.validation;
import java.io.IOException;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
final class DOMValidatorHelper implements ValidatorHelper, EntityState {
    private static final int CHUNK_SIZE = (1 << 10);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private final XMLErrorReporter fErrorReporter;
    private final NamespaceSupport fNamespaceContext;
    private final DOMNamespaceContext fDOMNamespaceContext = new DOMNamespaceContext();
    private final XMLSchemaValidator fSchemaValidator;
    private final SymbolTable fSymbolTable;
    private final ValidationManager fValidationManager;
    private final XMLSchemaValidatorComponentManager fComponentManager;
    private final SimpleLocator fXMLLocator = new SimpleLocator(null, null, -1, -1, -1);
    private DOMDocumentHandler fDOMValidatorHandler;
    private final DOMResultAugmentor fDOMResultAugmentor = new DOMResultAugmentor(this);
    private final DOMResultBuilder fDOMResultBuilder = new DOMResultBuilder();
    private NamedNodeMap fEntities = null;
    private final char [] fCharBuffer = new char[CHUNK_SIZE];
    private Node fRoot;
    private Node fCurrentElement;
    final QName fElementQName = new QName();
    final QName fAttributeQName = new QName();
    final XMLAttributesImpl fAttributes = new XMLAttributesImpl(); 
    final XMLString fTempString = new XMLString();
    public DOMValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fErrorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        fNamespaceContext = (NamespaceSupport) fComponentManager.getProperty(NAMESPACE_CONTEXT);
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
        fSymbolTable = (SymbolTable) fComponentManager.getProperty(SYMBOL_TABLE);        
        fValidationManager = (ValidationManager) fComponentManager.getProperty(VALIDATION_MANAGER);
    }
    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result instanceof DOMResult || result == null) {
            final DOMSource domSource = (DOMSource) source;
            final DOMResult domResult = (DOMResult) result;
            Node node = domSource.getNode();
            fRoot = node;
            if (node != null) {
                fComponentManager.reset();
                fValidationManager.setEntityState(this);
                fDOMNamespaceContext.reset();
                String systemId = domSource.getSystemId();
                fXMLLocator.setLiteralSystemId(systemId);
                fXMLLocator.setExpandedSystemId(systemId);
                fErrorReporter.setDocumentLocator(fXMLLocator);
                try {
                    setupEntityMap((node.getNodeType() == Node.DOCUMENT_NODE) ? (Document) node : node.getOwnerDocument());
                    setupDOMResultHandler(domSource, domResult);
                    fSchemaValidator.startDocument(fXMLLocator, null, fDOMNamespaceContext, null);
                    validate(node);
                    fSchemaValidator.endDocument(null);
                }
                catch (XMLParseException e) {
                    throw Util.toSAXParseException(e);
                }
                catch (XNIException e) {
                    throw Util.toSAXException(e);
                }
                finally {
                    fRoot = null;
                    fCurrentElement = null;
                    fEntities = null;
                    if (fDOMValidatorHandler != null) {
                        fDOMValidatorHandler.setDOMResult(null);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    public boolean isEntityDeclared(String name) {
        return false;
    }
    public boolean isEntityUnparsed(String name) {
        if (fEntities != null) {
            Entity entity = (Entity) fEntities.getNamedItem(name);
            if (entity != null) {
                return (entity.getNotationName() != null);
            }
        }
        return false;
    }
    private void validate(Node node) {
        final Node top = node;
        final boolean useIsSameNode = useIsSameNode(top);
        while (node != null) {
            beginNode(node);
            Node next = node.getFirstChild();
            while (next == null) {
                finishNode(node);           
                if (top == node) {
                    break;
                }
                next = node.getNextSibling();
                if (next == null) {
                    node = node.getParentNode();
                    if (node == null || ((useIsSameNode) ? 
                        top.isSameNode(node) : top == node)) {
                        if (node != null) {
                            finishNode(node);
                        }
                        next = null;
                        break;
                    }
                }
            }
            node = next;
        }
    }
    private void beginNode(Node node) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                fCurrentElement = node;
                fNamespaceContext.pushContext();
                fillQName(fElementQName, node);
                processAttributes(node.getAttributes());
                fSchemaValidator.startElement(fElementQName, fAttributes, null);
                break;
            case Node.TEXT_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    sendCharactersToValidator(node.getNodeValue());
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.characters((Text) node);
                }
                else {
                    sendCharactersToValidator(node.getNodeValue());
                }
                break;
            case Node.CDATA_SECTION_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null);
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.cdata((CDATASection) node);
                }
                else {
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null); 
                }
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.processingInstruction((ProcessingInstruction) node);
                }
                break;
            case Node.COMMENT_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.comment((Comment) node);
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.doctypeDecl((DocumentType) node);
                }
                break;
            default: 
                break;
        }
    }
    private void finishNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            fCurrentElement = node;
            fillQName(fElementQName, node);
            fSchemaValidator.endElement(fElementQName, null);
            fNamespaceContext.popContext();
        }
    }
    private void setupEntityMap(Document doc) {
        if (doc != null) {
            DocumentType docType = doc.getDoctype();
            if (docType != null) {
                fEntities = docType.getEntities();
                return;
            }
        }
        fEntities = null;
    }
    private void setupDOMResultHandler(DOMSource source, DOMResult result) throws SAXException {
        if (result == null) {
            fDOMValidatorHandler = null;
            fSchemaValidator.setDocumentHandler(null);
            return;
        }
        final Node nodeResult = result.getNode();
        if (source.getNode() == nodeResult) {
            fDOMValidatorHandler = fDOMResultAugmentor;
            fDOMResultAugmentor.setDOMResult(result);
            fSchemaValidator.setDocumentHandler(fDOMResultAugmentor);
            return;
        }
        if (result.getNode() == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                result.setNode(builder.newDocument());
            }
            catch (ParserConfigurationException e) {
                throw new SAXException(e);
            }
        }
        fDOMValidatorHandler = fDOMResultBuilder;
        fDOMResultBuilder.setDOMResult(result);
        fSchemaValidator.setDocumentHandler(fDOMResultBuilder);
    }
    private void fillQName(QName toFill, Node node) {
        final String prefix = node.getPrefix();
        final String localName = node.getLocalName();
        final String rawName = node.getNodeName();
        final String namespace = node.getNamespaceURI();
        toFill.prefix = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
        toFill.localpart = (localName != null) ? fSymbolTable.addSymbol(localName) : XMLSymbols.EMPTY_STRING;
        toFill.rawname = (rawName != null) ? fSymbolTable.addSymbol(rawName) : XMLSymbols.EMPTY_STRING; 
        toFill.uri = (namespace != null && namespace.length() > 0) ? fSymbolTable.addSymbol(namespace) : null;
    }
    private void processAttributes(NamedNodeMap attrMap) {
        final int attrCount = attrMap.getLength();
        fAttributes.removeAllAttributes();
        for (int i = 0; i < attrCount; ++i) {
            Attr attr = (Attr) attrMap.item(i);
            String value = attr.getValue();
            if (value == null) {
                value = XMLSymbols.EMPTY_STRING;
            }
            fillQName(fAttributeQName, attr);
            fAttributes.addAttributeNS(fAttributeQName, XMLSymbols.fCDATASymbol, value);
            fAttributes.setSpecified(i, attr.getSpecified());
            if (fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                if (fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                    fNamespaceContext.declarePrefix(fAttributeQName.localpart, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                }
                else {
                    fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                }
            }
        }
    }
    private void sendCharactersToValidator(String str) {
        if (str != null) {
            final int length = str.length();
            final int remainder = length & CHUNK_MASK;
            if (remainder > 0) {
                str.getChars(0, remainder, fCharBuffer, 0);
                fTempString.setValues(fCharBuffer, 0, remainder);
                fSchemaValidator.characters(fTempString, null);
            }
            int i = remainder;
            while (i < length) {
                str.getChars(i, i += CHUNK_SIZE, fCharBuffer, 0);
                fTempString.setValues(fCharBuffer, 0, CHUNK_SIZE);
                fSchemaValidator.characters(fTempString, null);
            }
        }
    }
    private boolean useIsSameNode(Node node) {
        if (node instanceof NodeImpl) {
            return false;
        }
        Document doc = node.getNodeType() == Node.DOCUMENT_NODE 
            ? (Document) node : node.getOwnerDocument();
        return (doc != null && doc.getImplementation().hasFeature("Core", "3.0"));
    }
    Node getCurrentElement() {
        return fCurrentElement;
    }
    final class DOMNamespaceContext implements NamespaceContext {
        protected String[] fNamespace = new String[16 * 2];
        protected int fNamespaceSize = 0;
        protected boolean fDOMContextBuilt = false;
        public void pushContext() {
            fNamespaceContext.pushContext();
        }
        public void popContext() {
            fNamespaceContext.popContext();
        }
        public boolean declarePrefix(String prefix, String uri) {
            return fNamespaceContext.declarePrefix(prefix, uri);
        }
        public String getURI(String prefix) {
            String uri = fNamespaceContext.getURI(prefix);
            if (uri == null) {
                if (!fDOMContextBuilt) {
                    fillNamespaceContext();
                    fDOMContextBuilt = true;
                }
                if (fNamespaceSize > 0 && 
                    !fNamespaceContext.containsPrefix(prefix)) {
                    uri = getURI0(prefix);
                }
            }
            return uri;
        }
        public String getPrefix(String uri) {
            return fNamespaceContext.getPrefix(uri);
        }
        public int getDeclaredPrefixCount() {
            return fNamespaceContext.getDeclaredPrefixCount();
        }
        public String getDeclaredPrefixAt(int index) {
            return fNamespaceContext.getDeclaredPrefixAt(index);
        }
        public Enumeration getAllPrefixes() {
            return fNamespaceContext.getAllPrefixes();
        }
        public void reset() {
            fDOMContextBuilt = false;
            fNamespaceSize = 0; 
        }
        private void fillNamespaceContext() {
            if (fRoot != null) {
                Node currentNode = fRoot.getParentNode();
                while (currentNode != null) {
                    if (Node.ELEMENT_NODE == currentNode.getNodeType()) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        final int attrCount = attributes.getLength();
                        for (int i = 0; i < attrCount; ++i) {
                            Attr attr = (Attr) attributes.item(i);
                            String value = attr.getValue();
                            if (value == null) {
                                value = XMLSymbols.EMPTY_STRING;
                            }
                            fillQName(fAttributeQName, attr);
                            if (fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                                if (fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                                    declarePrefix0(fAttributeQName.localpart, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                                else {
                                    declarePrefix0(XMLSymbols.EMPTY_STRING, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                            }
                        }
                    }
                    currentNode = currentNode.getParentNode();
                }
            }
        }
        private void declarePrefix0(String prefix, String uri) {           
            if (fNamespaceSize == fNamespace.length) {
                String[] namespacearray = new String[fNamespaceSize * 2];
                System.arraycopy(fNamespace, 0, namespacearray, 0, fNamespaceSize);
                fNamespace = namespacearray;
            }
            fNamespace[fNamespaceSize++] = prefix;
            fNamespace[fNamespaceSize++] = uri;
        }
        private String getURI0(String prefix) {
            for (int i = 0; i < fNamespaceSize; i += 2) {
                if (fNamespace[i] == prefix) {
                    return fNamespace[i + 1];
                }
            }
            return null;
        }
    }
} 
