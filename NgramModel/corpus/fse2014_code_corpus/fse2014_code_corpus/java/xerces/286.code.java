package org.apache.xerces.impl;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;
import org.apache.xerces.impl.io.MalformedByteSequenceException;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XMLDocumentFragmentScannerImpl
    extends XMLScanner
    implements XMLDocumentScanner, XMLComponent, XMLEntityHandler {
    protected static final int SCANNER_STATE_START_OF_MARKUP = 1;
    protected static final int SCANNER_STATE_COMMENT = 2;
    protected static final int SCANNER_STATE_PI = 3;
    protected static final int SCANNER_STATE_DOCTYPE = 4;
    protected static final int SCANNER_STATE_ROOT_ELEMENT = 6;
    protected static final int SCANNER_STATE_CONTENT = 7;
    protected static final int SCANNER_STATE_REFERENCE = 8;
    protected static final int SCANNER_STATE_END_OF_INPUT = 13;
    protected static final int SCANNER_STATE_TERMINATED = 14;
    protected static final int SCANNER_STATE_CDATA = 15;
    protected static final int SCANNER_STATE_TEXT_DECL = 16;
    protected static final String NAMESPACES = 
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    protected static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES, 
        VALIDATION, 
        NOTIFY_BUILTIN_REFS,
        NOTIFY_CHAR_REFS, 
    };
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
        null,
        Boolean.FALSE,
        Boolean.FALSE,
    };
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ENTITY_MANAGER,
        ENTITY_RESOLVER,
    };
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
        null,
    };
    private static final boolean DEBUG_SCANNER_STATE = false;
    private static final boolean DEBUG_DISPATCHER = false;
    protected static final boolean DEBUG_CONTENT_SCANNING = false;
    protected XMLDocumentHandler fDocumentHandler;
    protected int[] fEntityStack = new int[4];
    protected int fMarkupDepth;
    protected int fScannerState;
    protected boolean fInScanContent = false;
    protected boolean fHasExternalDTD;
    protected boolean fStandalone;
    protected boolean fIsEntityDeclaredVC;
    protected ExternalSubsetResolver fExternalSubsetResolver;
    protected QName fCurrentElement;
    protected final ElementStack fElementStack = new ElementStack();
    protected boolean fNotifyBuiltInRefs = false;
    protected Dispatcher fDispatcher;
    protected final Dispatcher fContentDispatcher = createContentDispatcher();
    protected final QName fElementQName = new QName();
    protected final QName fAttributeQName = new QName();
    protected final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    protected final XMLString fTempString = new XMLString();
    protected final XMLString fTempString2 = new XMLString();
    private final String[] fStrings = new String[3];
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private final QName fQName = new QName();
    private final char[] fSingleChar = new char[1];
    private boolean fSawSpace;
    private Augmentations fTempAugmentations = null;
    public XMLDocumentFragmentScannerImpl() {} 
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        fEntityManager.setEntityHandler(this);
        fEntityManager.startEntity("$fragment$", inputSource, false, true);
    } 
    public boolean scanDocument(boolean complete) 
        throws IOException, XNIException {
        fEntityScanner = fEntityManager.getEntityScanner();
        fEntityManager.setEntityHandler(this);
        do {
            if (!fDispatcher.dispatch(complete)) {
                return false;
            }
        } while (complete);
        return true;
    } 
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {
        super.reset(componentManager);
        fAttributes.setNamespaces(fNamespaces);
        fMarkupDepth = 0;
        fCurrentElement = null;
        fElementStack.clear();
        fHasExternalDTD = false;
        fStandalone = false;
        fIsEntityDeclaredVC = false;
        fInScanContent = false;
		setScannerState(SCANNER_STATE_CONTENT);
		setDispatcher(fContentDispatcher);
        if (fParserSettings) {
            try {
                fNotifyBuiltInRefs = componentManager.getFeature(NOTIFY_BUILTIN_REFS);
            } catch (XMLConfigurationException e) {
                fNotifyBuiltInRefs = false;
            }
            try {
                Object resolver = componentManager.getProperty(ENTITY_RESOLVER);
                fExternalSubsetResolver = (resolver instanceof ExternalSubsetResolver) ?
                    (ExternalSubsetResolver) resolver : null;
            }
            catch (XMLConfigurationException e) {
                fExternalSubsetResolver = null;
            }
        }
    } 
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        super.setFeature(featureId, state);
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.NOTIFY_BUILTIN_REFS_FEATURE.length() && 
                featureId.endsWith(Constants.NOTIFY_BUILTIN_REFS_FEATURE)) {
                fNotifyBuiltInRefs = state;
            }
        }
    } 
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        super.setProperty(propertyId, value);
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.ENTITY_MANAGER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (XMLEntityManager)value;
                return;
            }
            if (suffixLength == Constants.ENTITY_RESOLVER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
                fExternalSubsetResolver = (value instanceof ExternalSubsetResolver) ?
                    (ExternalSubsetResolver) value : null;
                return;
            }
        }
    } 
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } 
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } 
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } 
    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    }
    public void startEntity(String name, 
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {
        if (fEntityDepth == fEntityStack.length) {
            int[] entityarray = new int[fEntityStack.length * 2];
            System.arraycopy(fEntityStack, 0, entityarray, 0, fEntityStack.length);
            fEntityStack = entityarray;
        }
        fEntityStack[fEntityDepth] = fMarkupDepth;
        super.startEntity(name, identifier, encoding, augs);
        if(fStandalone && fEntityManager.isEntityDeclInExternalSubset(name)) {
            reportFatalError("MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE",
                new Object[]{name});
        }
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.startGeneralEntity(name, identifier, encoding, augs);
            }
        }
    } 
    public void endEntity(String name, Augmentations augs) throws XNIException {
        if (fInScanContent && fStringBuffer.length != 0
            && fDocumentHandler != null) {
            fDocumentHandler.characters(fStringBuffer, null);
            fStringBuffer.length = 0; 
        }
        super.endEntity(name, augs);
        if (fMarkupDepth != fEntityStack[fEntityDepth]) {
            reportFatalError("MarkupEntityMismatch", null);
        }
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.endGeneralEntity(name, augs);
            }
        }
    } 
    protected Dispatcher createContentDispatcher() {
        return new FragmentContentDispatcher();
    } 
    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl) 
        throws IOException, XNIException {
        super.scanXMLDeclOrTextDecl(scanningTextDecl, fStrings);
        fMarkupDepth--;
        String version = fStrings[0];
        String encoding = fStrings[1];
        String standalone = fStrings[2];
        fStandalone = standalone != null && standalone.equals("yes");
        fEntityManager.setStandalone(fStandalone);
        fEntityScanner.setXMLVersion(version);
        if (fDocumentHandler != null) {
            if (scanningTextDecl) {
                fDocumentHandler.textDecl(version, encoding, null);
            }
            else {
                fDocumentHandler.xmlDecl(version, encoding, standalone, null);
            }
        }
        if (encoding != null && !fEntityScanner.fCurrentEntity.isEncodingExternallySpecified()) {
            fEntityScanner.setEncoding(encoding);
        }
    } 
    protected void scanPIData(String target, XMLString data) 
        throws IOException, XNIException {
        super.scanPIData(target, data);
        fMarkupDepth--;
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, null);
        }
    } 
    protected void scanComment() throws IOException, XNIException {
        scanComment(fStringBuffer);
        fMarkupDepth--;
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(fStringBuffer, null);
        }
    } 
    protected boolean scanStartElement() 
        throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanStartElement()");
        if (fNamespaces) {
            fEntityScanner.scanQName(fElementQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fElementQName.setValues(null, name, name, null);
        }
        String rawname = fElementQName.rawname;
        fCurrentElement = fElementStack.pushElement(fElementQName);
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
            boolean sawSpace = fEntityScanner.skipSpaces();
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !sawSpace) {
                if (!isValidNameStartHighSurrogate(c) || !sawSpace) {
                    reportFatalError("ElementUnterminated",
                                     new Object[] { rawname });
                }
            }
            scanAttribute(fAttributes);
        } while (true);
        if (fDocumentHandler != null) {
            if (empty) {
                fMarkupDepth--;
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }
                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);
                fElementStack.popElement(fElementQName);
            }
            else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElement(): "+empty);
        return empty;
    } 
    protected void scanStartElementName ()
        throws IOException, XNIException {
        if (fNamespaces) {
            fEntityScanner.scanQName(fElementQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fElementQName.setValues(null, name, name, null);
        }
        fSawSpace = fEntityScanner.skipSpaces();
    } 
    protected boolean scanStartElementAfterName()
        throws IOException, XNIException {
        String rawname = fElementQName.rawname;
        fCurrentElement = fElementStack.pushElement(fElementQName);
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !fSawSpace) {
                if (!isValidNameStartHighSurrogate(c) || !fSawSpace) {
                    reportFatalError("ElementUnterminated",
                                     new Object[] { rawname });
                }
            }
            scanAttribute(fAttributes);
            fSawSpace = fEntityScanner.skipSpaces();
        } while (true);
        if (fDocumentHandler != null) {
            if (empty) {
                fMarkupDepth--;
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }
                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);
                fElementStack.popElement(fElementQName);
            }
            else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElementAfterName(): "+empty);
        return empty;
    } 
    protected void scanAttribute(XMLAttributes attributes) 
        throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanAttribute()");
        if (fNamespaces) {
            fEntityScanner.scanQName(fAttributeQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fAttributeQName.setValues(null, name, name, null);
        }
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError("EqRequiredInAttribute",
                             new Object[]{fCurrentElement.rawname,fAttributeQName.rawname});
        }
        fEntityScanner.skipSpaces();
        int oldLen = attributes.getLength();
        int attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);
        if (oldLen == attributes.getLength()) {
            reportFatalError("AttributeNotUnique",
                             new Object[]{fCurrentElement.rawname,
                                          fAttributeQName.rawname});
        }      
        boolean isSameNormalizedAttr =  scanAttributeValue(fTempString, fTempString2,
                fAttributeQName.rawname, fIsEntityDeclaredVC, fCurrentElement.rawname);
        attributes.setValue(attrIndex, fTempString.toString());
        if (!isSameNormalizedAttr) {
            attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
        }
        attributes.setSpecified(attrIndex, true);
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanAttribute()");
    } 
    protected int scanContent() throws IOException, XNIException {
        XMLString content = fTempString;
        int c = fEntityScanner.scanContent(content);
        if (c == '\r') {
            fEntityScanner.scanChar();
            fStringBuffer.clear();
            fStringBuffer.append(fTempString);
            fStringBuffer.append((char)c);
            content = fStringBuffer;
            c = -1;
        }
        if (fDocumentHandler != null && content.length > 0) {
            fDocumentHandler.characters(content, null);
        }
        if (c == ']' && fTempString.length == 0) {
            fStringBuffer.clear();
            fStringBuffer.append((char)fEntityScanner.scanChar());
            fInScanContent = true;
            if (fEntityScanner.skipChar(']')) {
                fStringBuffer.append(']');
                while (fEntityScanner.skipChar(']')) {
                    fStringBuffer.append(']');
                }
                if (fEntityScanner.skipChar('>')) {
                    reportFatalError("CDEndInContent", null);
                }
            }
            if (fDocumentHandler != null && fStringBuffer.length != 0) {
                fDocumentHandler.characters(fStringBuffer, null);
            }
            fInScanContent = false;
            c = -1;
        }
        return c;
    } 
    protected boolean scanCDATASection(boolean complete) 
        throws IOException, XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(null);
        }
        while (true) {
            fStringBuffer.clear();
            if (!fEntityScanner.scanData("]]", fStringBuffer)) {
                if (fDocumentHandler != null && fStringBuffer.length > 0) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int brackets = 0;
                while (fEntityScanner.skipChar(']')) {
                    brackets++;
                }
                if (fDocumentHandler != null && brackets > 0) {
                    fStringBuffer.clear();
                    if (brackets > XMLEntityManager.DEFAULT_BUFFER_SIZE) {
                        int chunks = brackets / XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        int remainder = brackets % XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        for (int i = 0; i < XMLEntityManager.DEFAULT_BUFFER_SIZE; i++) {
                            fStringBuffer.append(']');
                        }
                        for (int i = 0; i < chunks; i++) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                        if (remainder != 0) {
                            fStringBuffer.length = remainder;
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    }
                    else {
                    	for (int i = 0; i < brackets; i++) {
                    	    fStringBuffer.append(']');
                    	}
                       fDocumentHandler.characters(fStringBuffer, null);
                    }
                }
                if (fEntityScanner.skipChar('>')) {
                    break;
                }
                if (fDocumentHandler != null) {
                    fStringBuffer.clear();
                    fStringBuffer.append("]]");
                    fDocumentHandler.characters(fStringBuffer, null);
                }
            }
            else {
                if (fDocumentHandler != null) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int c = fEntityScanner.peekChar();
                if (c != -1 && isInvalidLiteral(c)) {
                    if (XMLChar.isHighSurrogate(c)) {
                        fStringBuffer.clear();
                        scanSurrogates(fStringBuffer);
                        if (fDocumentHandler != null) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    }
                    else {
                        reportFatalError("InvalidCharInCDSect",
                                        new Object[]{Integer.toString(c,16)});
                        fEntityScanner.scanChar();
                    }
                }
            }
        }
        fMarkupDepth--;
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(null);
        }
        return true;
    } 
    protected int scanEndElement() throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanEndElement()");
        fElementStack.popElement(fElementQName) ;
        if (!fEntityScanner.skipString(fElementQName.rawname)) {
            reportFatalError("ETagRequired", new Object[]{fElementQName.rawname});
        }
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("ETagUnterminated",
                             new Object[]{fElementQName.rawname});
        }
        fMarkupDepth--;
        fMarkupDepth--;
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                             new Object[]{fCurrentElement.rawname});
        }
        if (fDocumentHandler != null ) {
            fDocumentHandler.endElement(fElementQName, null);
        }
        return fMarkupDepth;
    } 
    protected void scanCharReference() 
        throws IOException, XNIException {
        fStringBuffer2.clear();
        int ch = scanCharReferenceValue(fStringBuffer2, null);
        fMarkupDepth--;
        if (ch != -1) {
            if (fDocumentHandler != null) {
                if (fNotifyCharRefs) {
                    fDocumentHandler.startGeneralEntity(fCharRefLiteral, null, null, null);
                }
                Augmentations augs = null;
                if (fValidation && ch <= 0x20) {
                    if (fTempAugmentations != null) {
                        fTempAugmentations.removeAllItems();
                    }
                    else {
                        fTempAugmentations = new AugmentationsImpl();
                    }
                    augs = fTempAugmentations;
                    augs.putItem(Constants.CHAR_REF_PROBABLE_WS, Boolean.TRUE);
                }
                fDocumentHandler.characters(fStringBuffer2, augs);
                if (fNotifyCharRefs) {
                    fDocumentHandler.endGeneralEntity(fCharRefLiteral, null);
                }
            }
        }
    } 
    protected void scanEntityReference() throws IOException, XNIException {
        String name = fEntityScanner.scanName();
        if (name == null) {
            reportFatalError("NameRequiredInReference", null);
            return;
        }
        if (!fEntityScanner.skipChar(';')) {
            reportFatalError("SemicolonRequiredInReference", new Object []{name});
        }
        fMarkupDepth--;
        if (name == fAmpSymbol) {
            handleCharacter('&', fAmpSymbol);
        }
        else if (name == fLtSymbol) {
            handleCharacter('<', fLtSymbol);
        }
        else if (name == fGtSymbol) {
            handleCharacter('>', fGtSymbol);
        }
        else if (name == fQuotSymbol) {
            handleCharacter('"', fQuotSymbol);
        }
        else if (name == fAposSymbol) {
            handleCharacter('\'', fAposSymbol);
        }
        else if (fEntityManager.isUnparsedEntity(name)) {
            reportFatalError("ReferenceToUnparsedEntity", new Object[]{name});
        }
        else {
            if (!fEntityManager.isDeclaredEntity(name)) {
                if (fIsEntityDeclaredVC) {
                    if (fValidation)
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,"EntityNotDeclared", 
                                                    new Object[]{name}, XMLErrorReporter.SEVERITY_ERROR);
                }
                else {
                    reportFatalError("EntityNotDeclared", new Object[]{name});
                }
            }
            fEntityManager.startEntity(name, false);
        }
    } 
    private void handleCharacter(char c, String entity) throws XNIException {
        if (fDocumentHandler != null) {
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.startGeneralEntity(entity, null, null, null);
            }
            fSingleChar[0] = c;
            fTempString.setValues(fSingleChar, 0, 1);
            fDocumentHandler.characters(fTempString, null);
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.endGeneralEntity(entity, null);
            }
        }
    } 
    protected int handleEndElement(QName element, boolean isEmpty) 
        throws XNIException {
        fMarkupDepth--;
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                             new Object[]{fCurrentElement.rawname});
        }
        QName startElement = fQName;
        fElementStack.popElement(startElement);
        if (element.rawname != startElement.rawname) {
            reportFatalError("ETagRequired",
                             new Object[]{startElement.rawname});
        }
        if (fNamespaces) {
            element.uri = startElement.uri;
        }
        if (fDocumentHandler != null && !isEmpty) {
            fDocumentHandler.endElement(element, null);
        }
        return fMarkupDepth;
    } 
    protected final void setScannerState(int state) {
        fScannerState = state;
        if (DEBUG_SCANNER_STATE) {
            System.out.print("### setScannerState: ");
            System.out.print(getScannerStateName(state));
            System.out.println();
        }
    } 
    protected final void setDispatcher(Dispatcher dispatcher) {
        fDispatcher = dispatcher;
        if (DEBUG_DISPATCHER) {
            System.out.print("%%% setDispatcher: ");
            System.out.print(getDispatcherName(dispatcher));
            System.out.println();
        }
    }
    protected String getScannerStateName(int state) {
        switch (state) {
            case SCANNER_STATE_DOCTYPE: return "SCANNER_STATE_DOCTYPE";
            case SCANNER_STATE_ROOT_ELEMENT: return "SCANNER_STATE_ROOT_ELEMENT";
            case SCANNER_STATE_START_OF_MARKUP: return "SCANNER_STATE_START_OF_MARKUP";
            case SCANNER_STATE_COMMENT: return "SCANNER_STATE_COMMENT";
            case SCANNER_STATE_PI: return "SCANNER_STATE_PI";
            case SCANNER_STATE_CONTENT: return "SCANNER_STATE_CONTENT";
            case SCANNER_STATE_REFERENCE: return "SCANNER_STATE_REFERENCE";
            case SCANNER_STATE_END_OF_INPUT: return "SCANNER_STATE_END_OF_INPUT";
            case SCANNER_STATE_TERMINATED: return "SCANNER_STATE_TERMINATED";
            case SCANNER_STATE_CDATA: return "SCANNER_STATE_CDATA";
            case SCANNER_STATE_TEXT_DECL: return "SCANNER_STATE_TEXT_DECL";
        }
        return "??? ("+state+')';
    } 
    public String getDispatcherName(Dispatcher dispatcher) {
        if (DEBUG_DISPATCHER) {
            if (dispatcher != null) {
                String name = dispatcher.getClass().getName();
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    name = name.substring(index + 1);
                    index = name.lastIndexOf('$');
                    if (index != -1) {
                        name = name.substring(index + 1);
                    }
                }
                return name;
            }
        }
        return "null";
    } 
    protected static class ElementStack {
        protected QName[] fElements;
        protected int fSize;
        public ElementStack() {
            fElements = new QName[10];
            for (int i = 0; i < fElements.length; i++) {
                fElements[i] = new QName();
            }
        } 
        public QName pushElement(QName element) {
            if (fSize == fElements.length) {
                QName[] array = new QName[fElements.length * 2];
                System.arraycopy(fElements, 0, array, 0, fSize);
                fElements = array;
                for (int i = fSize; i < fElements.length; i++) {
                    fElements[i] = new QName();
                }
            }
            fElements[fSize].setValues(element);
            return fElements[fSize++];
        } 
        public void popElement(QName element) {
            element.setValues(fElements[--fSize]);
        } 
        public void clear() {
            fSize = 0;
        } 
    } 
    protected interface Dispatcher {
        public boolean dispatch(boolean complete) 
            throws IOException, XNIException;
    } 
    protected class FragmentContentDispatcher
        implements Dispatcher {
        public boolean dispatch(boolean complete) 
            throws IOException, XNIException {
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_CONTENT: {
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('&')) {
                                setScannerState(SCANNER_STATE_REFERENCE);
                                again = true;
                            }
                            else {
                                do {
                                    int c = scanContent();
                                    if (c == '<') {
                                        fEntityScanner.scanChar();
                                        setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                        break;
                                    }
                                    else if (c == '&') {
                                        fEntityScanner.scanChar();
                                        setScannerState(SCANNER_STATE_REFERENCE);
                                        break;
                                    }
                                    else if (c != -1 && isInvalidLiteral(c)) {
                                        if (XMLChar.isHighSurrogate(c)) {
                                            fStringBuffer.clear();
                                            if (scanSurrogates(fStringBuffer)) {
                                                if (fDocumentHandler != null) {
                                                    fDocumentHandler.characters(fStringBuffer, null);
                                                }
                                            }
                                        }
                                        else {
                                            reportFatalError("InvalidCharInContent",
                                                             new Object[] {
                                                Integer.toString(c, 16)});
                                            fEntityScanner.scanChar();
                                        }
                                    }
                                } while (complete);
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('/')) {
                                if (scanEndElement() == 0) {
                                    if (elementDepthIsZeroHook()) {
                                        return true;
                                    }
                                }
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart",
                                                         null);
                                    }
                                    setScannerState(SCANNER_STATE_COMMENT);
                                    again = true;
                                }
                                else if (fEntityScanner.skipString("[CDATA[")) {
                                    setScannerState(SCANNER_STATE_CDATA);
                                    again = true;
                                }
                                else if (!scanForDoctypeHook()) {
                                    reportFatalError("MarkupNotRecognizedInContent",
                                                     null);
                                }
                            }
                            else if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInContent",
                                                 null);
                                setScannerState(SCANNER_STATE_CONTENT);                 
                            }
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            scanComment();
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;  
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;  
                        }
                        case SCANNER_STATE_CDATA: {
                            scanCDATASection(complete);
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_REFERENCE: {
                            fMarkupDepth++;
                            setScannerState(SCANNER_STATE_CONTENT);
                            if (fEntityScanner.skipChar('#')) {
                                scanCharReference();
                            }
                            else {
                                scanEntityReference();
                            }
                            break;
                        }
                        case SCANNER_STATE_TEXT_DECL: {
                            if (fEntityScanner.skipString("<?xml")) {
                                fMarkupDepth++;
                                if (isValidNameChar(fEntityScanner.peekChar())) {
                                    fStringBuffer.clear();
                                    fStringBuffer.append("xml");
                                    if (fNamespaces) {
                                        while (isValidNCName(fEntityScanner.peekChar())) {
                                            fStringBuffer.append((char)fEntityScanner.scanChar());
                                        }
                                    }
                                    else {
                                        while (isValidNameChar(fEntityScanner.peekChar())) {
                                            fStringBuffer.append((char)fEntityScanner.scanChar());
                                        }
                                    }
                                    String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
                                    scanPIData(target, fTempString);
                                }
                                else {
                                    scanXMLDeclOrTextDecl(true);
                                }
                            }
                            fEntityManager.fCurrentEntity.mayReadChunks = true;
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_ROOT_ELEMENT: {
                            if (scanRootElementHook()) {
                                return true;
                            }
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_DOCTYPE: {
                            reportFatalError("DoctypeIllegalInContent",
                                             null);
                            setScannerState(SCANNER_STATE_CONTENT);
                        }
                    }
                } while (complete || again);
            }
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(), 
                    e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (EOFException e) {
                endOfFileHook(e);
                return false;
            }
            return true;
        } 
        protected boolean scanForDoctypeHook() 
            throws IOException, XNIException {
            return false;
        } 
        protected boolean elementDepthIsZeroHook()
            throws IOException, XNIException {
            return false;
        } 
        protected boolean scanRootElementHook()
            throws IOException, XNIException {
            return false;
        } 
        protected void endOfFileHook(EOFException e) 
            throws IOException, XNIException {
            if (fMarkupDepth != 0) {
                reportFatalError("PrematureEOF", null);
            }
        } 
    } 
} 
