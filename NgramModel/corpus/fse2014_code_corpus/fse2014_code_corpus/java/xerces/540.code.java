package org.apache.xerces.jaxp.validation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.util.JAXPNamespaceContextWrapper;
import org.apache.xerces.util.StAXLocationWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.SAXException;
final class StAXValidatorHelper implements ValidatorHelper, EntityState {
    private static final String STRING_INTERNING = "javax.xml.stream.isInterning";
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private final XMLErrorReporter fErrorReporter;
    private final XMLSchemaValidator fSchemaValidator;
    private final SymbolTable fSymbolTable;
    private final ValidationManager fValidationManager;
    private final XMLSchemaValidatorComponentManager fComponentManager;
    private final JAXPNamespaceContextWrapper fNamespaceContext;
    private final StAXLocationWrapper fStAXLocationWrapper = new StAXLocationWrapper();
    private final XMLStreamReaderLocation fXMLStreamReaderLocation = new XMLStreamReaderLocation();
    private HashMap fEntities = null;
    private boolean fStringsInternalized = false;
    private StreamHelper fStreamHelper;
    private EventHelper fEventHelper;
    private StAXDocumentHandler fStAXValidatorHandler;
    private StAXStreamResultBuilder fStAXStreamResultBuilder;
    private StAXEventResultBuilder fStAXEventResultBuilder;
    private int fDepth = 0;
    private XMLEvent fCurrentEvent = null;
    final QName fElementQName = new QName();
    final QName fAttributeQName = new QName();
    final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    final ArrayList fDeclaredPrefixes = new ArrayList();
    final XMLString fTempString = new XMLString();
    final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    public StAXValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fErrorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
        fSymbolTable = (SymbolTable) fComponentManager.getProperty(SYMBOL_TABLE);        
        fValidationManager = (ValidationManager) fComponentManager.getProperty(VALIDATION_MANAGER);
        fNamespaceContext = new JAXPNamespaceContextWrapper(fSymbolTable);
        fNamespaceContext.setDeclaredPrefixes(fDeclaredPrefixes);
    }
    public void validate(Source source, Result result) throws SAXException,
            IOException {
        if (result instanceof StAXResult || result == null) {
            StAXSource staxSource = (StAXSource) source;
            StAXResult staxResult = (StAXResult) result;
            try {
                XMLStreamReader streamReader = staxSource.getXMLStreamReader();
                if (streamReader != null) {
                    if (fStreamHelper == null) {
                        fStreamHelper = new StreamHelper();
                    }
                    fStreamHelper.validate(streamReader, staxResult);
                }
                else {
                    if (fEventHelper == null) {
                        fEventHelper = new EventHelper();
                    }
                    fEventHelper.validate(staxSource.getXMLEventReader(), staxResult);
                } 
            }
            catch (XMLStreamException e) {
                throw new SAXException(e);
            }
            catch (XMLParseException e) {
                throw Util.toSAXParseException(e);
            }
            catch (XNIException e) {
                throw Util.toSAXException(e);
            }
            finally {
                fCurrentEvent = null;
                fStAXLocationWrapper.setLocation(null);
                fXMLStreamReaderLocation.setXMLStreamReader(null);
                if (fStAXValidatorHandler != null) {
                    fStAXValidatorHandler.setStAXResult(null);
                }
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    public boolean isEntityDeclared(String name) {
        if (fEntities != null) {
            return fEntities.containsKey(name);
        }
        return false;
    }
    public boolean isEntityUnparsed(String name) {
        if (fEntities != null) {
            EntityDeclaration entityDecl = (EntityDeclaration) fEntities.get(name);
            if (entityDecl != null) {
                return (entityDecl.getNotationName() != null);
            }
        }
        return false;
    }
    final EntityDeclaration getEntityDeclaration(String name) {
        return (fEntities != null) ? (EntityDeclaration) fEntities.get(name) : null;
    }
    final XMLEvent getCurrentEvent() {
        return fCurrentEvent;
    }
    final void fillQName(QName toFill, String uri, String localpart, String prefix) {
        if (!fStringsInternalized) {
            uri = (uri != null && uri.length() > 0) ? fSymbolTable.addSymbol(uri) : null;
            localpart = (localpart != null) ? fSymbolTable.addSymbol(localpart) : XMLSymbols.EMPTY_STRING;
            prefix = (prefix != null && prefix.length() > 0) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
        }
        else {
            if (uri != null && uri.length() == 0) {
                uri = null;
            }
            if (localpart == null) {
                localpart = XMLSymbols.EMPTY_STRING;
            }
            if (prefix == null) {
                prefix = XMLSymbols.EMPTY_STRING;
            }
        }
        String raw = localpart;
        if (prefix != XMLSymbols.EMPTY_STRING) {
            fStringBuffer.clear();
            fStringBuffer.append(prefix);
            fStringBuffer.append(':');
            fStringBuffer.append(localpart);
            raw = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
        }
        toFill.setValues(prefix, localpart, raw, uri);
    }
    final void setup(Location location, StAXResult result, boolean stringsInternalized) {
        fDepth = 0;
        fComponentManager.reset();
        setupStAXResultHandler(result);
        fValidationManager.setEntityState(this);
        if (fEntities != null && !fEntities.isEmpty()) {
            fEntities.clear();
        }
        fStAXLocationWrapper.setLocation(location);
        fErrorReporter.setDocumentLocator(fStAXLocationWrapper);
        fStringsInternalized = stringsInternalized;
    }
    final void processEntityDeclarations(List entityDecls) {
        int size = (entityDecls != null) ? entityDecls.size() : 0;
        if (size > 0) {
            if (fEntities == null) {
                fEntities = new HashMap();
            }
            for (int i = 0; i < size; ++i) {
                EntityDeclaration decl = (EntityDeclaration) entityDecls.get(i);
                fEntities.put(decl.getName(), decl);
            }
        }
    }
    private void setupStAXResultHandler(StAXResult result) {
        if (result == null) {
            fStAXValidatorHandler = null;
            fSchemaValidator.setDocumentHandler(null);
            return;
        }
        XMLStreamWriter writer = result.getXMLStreamWriter();
        if (writer != null) {
            if (fStAXStreamResultBuilder == null) {
                fStAXStreamResultBuilder = new StAXStreamResultBuilder(fNamespaceContext);
            }
            fStAXValidatorHandler = fStAXStreamResultBuilder;
            fStAXStreamResultBuilder.setStAXResult(result);
        }
        else {
            if (fStAXEventResultBuilder == null) {
                fStAXEventResultBuilder = new StAXEventResultBuilder(this, fNamespaceContext);
            }
            fStAXValidatorHandler = fStAXEventResultBuilder;
            fStAXEventResultBuilder.setStAXResult(result);
        }
        fSchemaValidator.setDocumentHandler(fStAXValidatorHandler);
    }
    final class StreamHelper {
        StreamHelper() {}
        final void validate(XMLStreamReader reader, StAXResult result) 
            throws SAXException, XMLStreamException {
            if (reader.hasNext()) {
                int eventType = reader.getEventType();
                if (eventType != XMLStreamConstants.START_DOCUMENT &&
                    eventType != XMLStreamConstants.START_ELEMENT) {
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                            "StAXIllegalInitialState", null));
                }
                fXMLStreamReaderLocation.setXMLStreamReader(reader);
                setup(fXMLStreamReaderLocation, result, Boolean.TRUE.equals(reader.getProperty(STRING_INTERNING)));
                fSchemaValidator.startDocument(fStAXLocationWrapper, null, fNamespaceContext, null);
                do {
                    switch (eventType) {
                        case XMLStreamConstants.START_ELEMENT:
                            ++fDepth;
                            fillQName(fElementQName, reader.getNamespaceURI(), 
                                    reader.getLocalName(), reader.getPrefix());
                            fillXMLAttributes(reader);
                            fillDeclaredPrefixes(reader);
                            fNamespaceContext.setNamespaceContext(reader.getNamespaceContext());
                            fSchemaValidator.startElement(fElementQName, fAttributes, null);
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            fillQName(fElementQName, reader.getNamespaceURI(), 
                                    reader.getLocalName(), reader.getPrefix());
                            fillDeclaredPrefixes(reader);
                            fNamespaceContext.setNamespaceContext(reader.getNamespaceContext());
                            fSchemaValidator.endElement(fElementQName, null);
                            --fDepth;
                            break;
                        case XMLStreamConstants.CHARACTERS:
                        case XMLStreamConstants.SPACE:
                            fTempString.setValues(reader.getTextCharacters(), 
                                    reader.getTextStart(), reader.getTextLength());
                            fSchemaValidator.characters(fTempString, null);
                            break;
                        case XMLStreamConstants.CDATA:
                            fSchemaValidator.startCDATA(null);
                            fTempString.setValues(reader.getTextCharacters(), 
                                    reader.getTextStart(), reader.getTextLength());
                            fSchemaValidator.characters(fTempString, null);
                            fSchemaValidator.endCDATA(null);
                            break;
                        case XMLStreamConstants.START_DOCUMENT:
                            ++fDepth;
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.startDocument(reader);
                            }
                            break;
                        case XMLStreamConstants.PROCESSING_INSTRUCTION:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.processingInstruction(reader);
                            }
                            break;
                        case XMLStreamConstants.COMMENT:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.comment(reader);
                            }
                            break;
                        case XMLStreamConstants.ENTITY_REFERENCE:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.entityReference(reader);
                            }
                            break;
                        case XMLStreamConstants.DTD:
                            processEntityDeclarations((List) reader.getProperty("javax.xml.stream.entities"));
                            break;
                    }
                    eventType = reader.next();
                }
                while (reader.hasNext() && fDepth > 0);
                fSchemaValidator.endDocument(null);
                if (eventType == XMLStreamConstants.END_DOCUMENT && fStAXValidatorHandler != null) {
                    fStAXValidatorHandler.endDocument(reader);
                }
            } 
        }
        private void fillXMLAttributes(XMLStreamReader reader) {
            fAttributes.removeAllAttributes();
            final int len = reader.getAttributeCount();
            for (int i = 0; i < len; ++i) {
                fillQName(fAttributeQName, reader.getAttributeNamespace(i), 
                        reader.getAttributeLocalName(i), reader.getAttributePrefix(i));
                String type = reader.getAttributeType(i);
                fAttributes.addAttributeNS(fAttributeQName, 
                        (type != null) ? type : XMLSymbols.fCDATASymbol, reader.getAttributeValue(i));
                fAttributes.setSpecified(i, reader.isAttributeSpecified(i));
            }
        }
        private void fillDeclaredPrefixes(XMLStreamReader reader) {
            fDeclaredPrefixes.clear();
            final int len = reader.getNamespaceCount();
            for (int i = 0; i < len; ++i) {
                String prefix = reader.getNamespacePrefix(i);
                fDeclaredPrefixes.add(prefix != null ? prefix : "");
            }
        }
    }
    final class EventHelper {
        private static final int CHUNK_SIZE = (1 << 10);
        private static final int CHUNK_MASK = CHUNK_SIZE - 1;
        private final char [] fCharBuffer = new char[CHUNK_SIZE];
        EventHelper() {}
        final void validate(XMLEventReader reader, StAXResult result) 
            throws SAXException, XMLStreamException {
            fCurrentEvent = reader.peek();
            if (fCurrentEvent != null) {
                int eventType = fCurrentEvent.getEventType();
                if (eventType != XMLStreamConstants.START_DOCUMENT &&
                    eventType != XMLStreamConstants.START_ELEMENT) {
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                            "StAXIllegalInitialState", null));
                }
                setup(null, result, false);
                fSchemaValidator.startDocument(fStAXLocationWrapper, null, fNamespaceContext, null);
                loop : while (reader.hasNext()) {
                    fCurrentEvent = reader.nextEvent();
                    eventType = fCurrentEvent.getEventType();
                    switch (eventType) {
                        case XMLStreamConstants.START_ELEMENT:
                            ++fDepth;
                            StartElement start = fCurrentEvent.asStartElement();
                            fillQName(fElementQName, start.getName());
                            fillXMLAttributes(start);
                            fillDeclaredPrefixes(start);
                            fNamespaceContext.setNamespaceContext(start.getNamespaceContext());
                            fStAXLocationWrapper.setLocation(start.getLocation());
                            fSchemaValidator.startElement(fElementQName, fAttributes, null);
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            EndElement end = fCurrentEvent.asEndElement();
                            fillQName(fElementQName, end.getName());
                            fillDeclaredPrefixes(end);
                            fStAXLocationWrapper.setLocation(end.getLocation());
                            fSchemaValidator.endElement(fElementQName, null);
                            if (--fDepth <= 0) {
                                break loop;
                            }
                            break;
                        case XMLStreamConstants.CHARACTERS:
                        case XMLStreamConstants.SPACE:
                            if (fStAXValidatorHandler != null) {
                                Characters chars = fCurrentEvent.asCharacters();
                                fStAXValidatorHandler.setIgnoringCharacters(true);
                                sendCharactersToValidator(chars.getData());
                                fStAXValidatorHandler.setIgnoringCharacters(false);
                                fStAXValidatorHandler.characters(chars);
                            }
                            else {
                                sendCharactersToValidator(fCurrentEvent.asCharacters().getData());
                            }
                            break;
                        case XMLStreamConstants.CDATA:
                            if (fStAXValidatorHandler != null) {
                                Characters chars = fCurrentEvent.asCharacters();
                                fStAXValidatorHandler.setIgnoringCharacters(true);
                                fSchemaValidator.startCDATA(null);
                                sendCharactersToValidator(fCurrentEvent.asCharacters().getData());
                                fSchemaValidator.endCDATA(null);
                                fStAXValidatorHandler.setIgnoringCharacters(false);
                                fStAXValidatorHandler.cdata(chars);
                            }
                            else {
                                fSchemaValidator.startCDATA(null);
                                sendCharactersToValidator(fCurrentEvent.asCharacters().getData());
                                fSchemaValidator.endCDATA(null);
                            }
                            break;
                        case XMLStreamConstants.START_DOCUMENT:
                            ++fDepth;
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.startDocument((StartDocument) fCurrentEvent);
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.endDocument((EndDocument) fCurrentEvent);
                            }
                            break;
                        case XMLStreamConstants.PROCESSING_INSTRUCTION:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.processingInstruction((ProcessingInstruction) fCurrentEvent);
                            }
                            break;
                        case XMLStreamConstants.COMMENT:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.comment((Comment) fCurrentEvent);
                            }
                            break;
                        case XMLStreamConstants.ENTITY_REFERENCE:
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.entityReference((EntityReference) fCurrentEvent);
                            }
                            break;
                        case XMLStreamConstants.DTD:
                            DTD dtd = (DTD) fCurrentEvent;
                            processEntityDeclarations(dtd.getEntities());
                            if (fStAXValidatorHandler != null) {
                                fStAXValidatorHandler.doctypeDecl(dtd);
                            }
                            break;
                    }
                }
                fSchemaValidator.endDocument(null);
            }
        }
        private void fillQName(QName toFill, javax.xml.namespace.QName toCopy) {
            StAXValidatorHelper.this.fillQName(toFill, toCopy.getNamespaceURI(), toCopy.getLocalPart(), toCopy.getPrefix());
        }
        private void fillXMLAttributes(StartElement event) {
            fAttributes.removeAllAttributes();
            final Iterator attrs = event.getAttributes();
            while (attrs.hasNext()) {
                Attribute attr = (Attribute) attrs.next();
                fillQName(fAttributeQName, attr.getName());
                String type = attr.getDTDType();
                int idx = fAttributes.getLength();
                fAttributes.addAttributeNS(fAttributeQName, 
                        (type != null) ? type : XMLSymbols.fCDATASymbol, attr.getValue());
                fAttributes.setSpecified(idx, attr.isSpecified());
            }
        }
        private void fillDeclaredPrefixes(StartElement event) {
            fillDeclaredPrefixes(event.getNamespaces());
        }
        private void fillDeclaredPrefixes(EndElement event) {
            fillDeclaredPrefixes(event.getNamespaces());
        }
        private void fillDeclaredPrefixes(Iterator namespaces) {
            fDeclaredPrefixes.clear();
            while (namespaces.hasNext()) {
                Namespace ns = (Namespace) namespaces.next();
                String prefix = ns.getPrefix();
                fDeclaredPrefixes.add(prefix != null ? prefix : "");
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
    }
    static final class XMLStreamReaderLocation implements Location {
        private XMLStreamReader reader;
        public XMLStreamReaderLocation() {}
        public int getCharacterOffset() {
            Location loc = getLocation();
            if (loc != null) {
                return loc.getCharacterOffset();
            }
            return -1;
        }
        public int getColumnNumber() {
            Location loc = getLocation();
            if (loc != null) {
                return loc.getColumnNumber();
            }
            return -1;
        }
        public int getLineNumber() {
            Location loc = getLocation();
            if (loc != null) {
                return loc.getLineNumber();
            }
            return -1;
        }
        public String getPublicId() {
            Location loc = getLocation();
            if (loc != null) {
                return loc.getPublicId();
            }
            return null;
        }
        public String getSystemId() {
            Location loc = getLocation();
            if (loc != null) {
                return loc.getSystemId();
            } 
            return null;
        }
        public void setXMLStreamReader(XMLStreamReader reader) {
            this.reader = reader;
        }
        private Location getLocation() {
            return reader != null ? reader.getLocation() : null;
        }
    }
} 
