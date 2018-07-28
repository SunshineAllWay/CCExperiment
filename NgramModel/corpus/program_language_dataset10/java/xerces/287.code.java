package org.apache.xerces.impl;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.io.MalformedByteSequenceException;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XMLDocumentScannerImpl
    extends XMLDocumentFragmentScannerImpl {
    protected static final int SCANNER_STATE_XML_DECL = 0;
    protected static final int SCANNER_STATE_PROLOG = 5;
    protected static final int SCANNER_STATE_TRAILING_MISC = 12;
    protected static final int SCANNER_STATE_DTD_INTERNAL_DECLS = 17;
    protected static final int SCANNER_STATE_DTD_EXTERNAL = 18;
    protected static final int SCANNER_STATE_DTD_EXTERNAL_DECLS = 19;
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;
    protected static final String DISALLOW_DOCTYPE_DECL_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE;
    protected static final String DTD_SCANNER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        LOAD_EXTERNAL_DTD,
        DISALLOW_DOCTYPE_DECL_FEATURE,
    };
    private static final Boolean[] FEATURE_DEFAULTS = {
        Boolean.TRUE,
        Boolean.FALSE,
    };
    private static final String[] RECOGNIZED_PROPERTIES = {
        DTD_SCANNER,
        VALIDATION_MANAGER,
        NAMESPACE_CONTEXT,
    };
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
    };
    protected XMLDTDScanner fDTDScanner;
    protected ValidationManager fValidationManager;
    protected boolean fScanningDTD;
    protected String fDoctypeName;
    protected String fDoctypePublicId;
    protected String fDoctypeSystemId;
    protected NamespaceContext fNamespaceContext = new NamespaceSupport();
    protected boolean fLoadExternalDTD = true;
    protected boolean fDisallowDoctype = false;
    protected boolean fSeenDoctypeDecl;
    protected final Dispatcher fXMLDeclDispatcher = new XMLDeclDispatcher();
    protected final Dispatcher fPrologDispatcher = new PrologDispatcher();
    protected final Dispatcher fDTDDispatcher = new DTDDispatcher();
    protected final Dispatcher fTrailingMiscDispatcher = new TrailingMiscDispatcher();
    private final String[] fStrings = new String[3];
    private final XMLString fString = new XMLString();
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private XMLInputSource fExternalSubsetSource = null;
    private final XMLDTDDescription fDTDDescription = new XMLDTDDescription(null, null, null, null, null);
    public XMLDocumentScannerImpl() {} 
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        fEntityManager.setEntityHandler(this);
        fEntityManager.startDocumentEntity(inputSource);
    } 
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {
        super.reset(componentManager);
        fDoctypeName = null;
        fDoctypePublicId = null;
        fDoctypeSystemId = null;
        fSeenDoctypeDecl = false;
        fScanningDTD = false;
        fExternalSubsetSource = null;
		if (!fParserSettings) {
			fNamespaceContext.reset();
			setScannerState(SCANNER_STATE_XML_DECL);
			setDispatcher(fXMLDeclDispatcher);
			return;
		}
        try {
            fLoadExternalDTD = componentManager.getFeature(LOAD_EXTERNAL_DTD);
        }
        catch (XMLConfigurationException e) {
            fLoadExternalDTD = true;
        }
        try {
            fDisallowDoctype = componentManager.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE);
        }
        catch (XMLConfigurationException e) {
            fDisallowDoctype = false;
        }
        fDTDScanner = (XMLDTDScanner)componentManager.getProperty(DTD_SCANNER);
        try {
            fValidationManager = (ValidationManager)componentManager.getProperty(VALIDATION_MANAGER);
        }
        catch (XMLConfigurationException e) {
            fValidationManager = null;
        }
        try {
            fNamespaceContext = (NamespaceContext)componentManager.getProperty(NAMESPACE_CONTEXT);
        }
        catch (XMLConfigurationException e) { }
        if (fNamespaceContext == null) {
            fNamespaceContext = new NamespaceSupport();
        }
        fNamespaceContext.reset();
        setScannerState(SCANNER_STATE_XML_DECL);
        setDispatcher(fXMLDeclDispatcher);
    } 
    public String[] getRecognizedFeatures() {
        String[] featureIds = super.getRecognizedFeatures();
        int length = featureIds != null ? featureIds.length : 0;
        String[] combinedFeatureIds = new String[length + RECOGNIZED_FEATURES.length];
        if (featureIds != null) {
            System.arraycopy(featureIds, 0, combinedFeatureIds, 0, featureIds.length);
        }
        System.arraycopy(RECOGNIZED_FEATURES, 0, combinedFeatureIds, length, RECOGNIZED_FEATURES.length);
        return combinedFeatureIds;
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        super.setFeature(featureId, state);
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.LOAD_EXTERNAL_DTD_FEATURE.length() && 
                featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                fLoadExternalDTD = state;
                return;
            }
            else if (suffixLength == Constants.DISALLOW_DOCTYPE_DECL_FEATURE.length() && 
                featureId.endsWith(Constants.DISALLOW_DOCTYPE_DECL_FEATURE)) {
                fDisallowDoctype = state;
                return;
            }
        }
    } 
    public String[] getRecognizedProperties() {
        String[] propertyIds = super.getRecognizedProperties();
        int length = propertyIds != null ? propertyIds.length : 0;
        String[] combinedPropertyIds = new String[length + RECOGNIZED_PROPERTIES.length];
        if (propertyIds != null) {
            System.arraycopy(propertyIds, 0, combinedPropertyIds, 0, propertyIds.length);
        }
        System.arraycopy(RECOGNIZED_PROPERTIES, 0, combinedPropertyIds, length, RECOGNIZED_PROPERTIES.length);
        return combinedPropertyIds;
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        super.setProperty(propertyId, value);
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.DTD_SCANNER_PROPERTY.length() && 
                propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                fDTDScanner = (XMLDTDScanner)value;
            }
            if (suffixLength == Constants.NAMESPACE_CONTEXT_PROPERTY.length() && 
                propertyId.endsWith(Constants.NAMESPACE_CONTEXT_PROPERTY)) {
                if (value != null) {
                    fNamespaceContext = (NamespaceContext)value;
                }
            }
            return;
        }
    } 
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return super.getFeatureDefault(featureId);
    } 
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return super.getPropertyDefault(propertyId);
    } 
    public void startEntity(String name,
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {
        super.startEntity(name, identifier, encoding, augs);
        if (!name.equals("[xml]") && fEntityScanner.isExternal()) {
            setScannerState(SCANNER_STATE_TEXT_DECL);
        } 
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.startDocument(fEntityScanner, encoding, fNamespaceContext, null);
        }
    } 
    public void endEntity(String name, Augmentations augs) throws XNIException {
        super.endEntity(name, augs);
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.endDocument(null);
        }
    } 
    protected Dispatcher createContentDispatcher() {
        return new ContentDispatcher();
    } 
    protected boolean scanDoctypeDecl() throws IOException, XNIException {
        if (!fEntityScanner.skipSpaces()) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL",
                             null);
        }
        fDoctypeName = fEntityScanner.scanName();
        if (fDoctypeName == null) {
            reportFatalError("MSG_ROOT_ELEMENT_TYPE_REQUIRED", null);
        }
        if (fEntityScanner.skipSpaces()) {
            scanExternalID(fStrings, false);
            fDoctypeSystemId = fStrings[0];
            fDoctypePublicId = fStrings[1];
            fEntityScanner.skipSpaces();
        }
        fHasExternalDTD = fDoctypeSystemId != null;
        if (!fHasExternalDTD && fExternalSubsetResolver != null) {
            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            fDTDDescription.setRootName(fDoctypeName);
            fExternalSubsetSource = fExternalSubsetResolver.getExternalSubset(fDTDDescription);
            fHasExternalDTD = fExternalSubsetSource != null;
        }
        if (fDocumentHandler != null) {
            if (fExternalSubsetSource == null) {
                fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
            }
            else {
                fDocumentHandler.doctypeDecl(fDoctypeName, fExternalSubsetSource.getPublicId(), fExternalSubsetSource.getSystemId(), null);
            }
        }
        boolean internalSubset = true;
        if (!fEntityScanner.skipChar('[')) {
            internalSubset = false;
            fEntityScanner.skipSpaces();
            if (!fEntityScanner.skipChar('>')) {
                reportFatalError("DoctypedeclUnterminated", new Object[]{fDoctypeName});
            }
            fMarkupDepth--;
        }
        return internalSubset;
    } 
    protected String getScannerStateName(int state) {
        switch (state) {
            case SCANNER_STATE_XML_DECL: return "SCANNER_STATE_XML_DECL";
            case SCANNER_STATE_PROLOG: return "SCANNER_STATE_PROLOG";
            case SCANNER_STATE_TRAILING_MISC: return "SCANNER_STATE_TRAILING_MISC";
            case SCANNER_STATE_DTD_INTERNAL_DECLS: return "SCANNER_STATE_DTD_INTERNAL_DECLS";
            case SCANNER_STATE_DTD_EXTERNAL: return "SCANNER_STATE_DTD_EXTERNAL";
            case SCANNER_STATE_DTD_EXTERNAL_DECLS: return "SCANNER_STATE_DTD_EXTERNAL_DECLS";
        }
        return super.getScannerStateName(state);
    } 
    protected final class XMLDeclDispatcher
        implements Dispatcher {
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {
            setScannerState(SCANNER_STATE_PROLOG);
            setDispatcher(fPrologDispatcher);
            try {
                if (fEntityScanner.skipString("<?xml")) {
                    fMarkupDepth++;
                    if (XMLChar.isName(fEntityScanner.peekChar())) {
                        fStringBuffer.clear();
                        fStringBuffer.append("xml");
                        if (fNamespaces) {
                            while (XMLChar.isNCName(fEntityScanner.peekChar())) {
                                fStringBuffer.append((char)fEntityScanner.scanChar());
                            }
                        }
                        else {
                            while (XMLChar.isName(fEntityScanner.peekChar())) {
                                fStringBuffer.append((char)fEntityScanner.scanChar());
                            }
                        }
                        String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
                        scanPIData(target, fString);
                    }
                    else {
                        scanXMLDeclOrTextDecl(false);
                    }
                }
                fEntityManager.fCurrentEntity.mayReadChunks = true;
                return true;
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
                reportFatalError("PrematureEOF", null);
                return false;
            }
        } 
    } 
    protected final class PrologDispatcher
        implements Dispatcher {
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_PROLOG: {
                            fEntityScanner.skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('&')) {
                                setScannerState(SCANNER_STATE_REFERENCE);
                                again = true;
                            }
                            else {
                                setScannerState(SCANNER_STATE_CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart",
                                                         null);
                                    }
                                    setScannerState(SCANNER_STATE_COMMENT);
                                    again = true;
                                }
                                else if (fEntityScanner.skipString("DOCTYPE")) {
                                    setScannerState(SCANNER_STATE_DOCTYPE);
                                    again = true;
                                }
                                else {
                                    reportFatalError("MarkupNotRecognizedInProlog",
                                                     null);
                                }
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            }
                            else if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInProlog",
                                                 null);
                            }
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            scanComment();
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_DOCTYPE: {
                            if (fDisallowDoctype) {
                                reportFatalError("DoctypeNotAllowed", null);
                            }
                            if (fSeenDoctypeDecl) {
                                reportFatalError("AlreadySeenDoctype", null);
                            }
                            fSeenDoctypeDecl = true;
                            if (scanDoctypeDecl()) {
                                setScannerState(SCANNER_STATE_DTD_INTERNAL_DECLS);
                                setDispatcher(fDTDDispatcher);
                                return true;
                            }
                            if (fDoctypeSystemId != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                                if (((fValidation || fLoadExternalDTD) 
                                    && (fValidationManager == null || !fValidationManager.isCachedDTD()))) {
                                    setScannerState(SCANNER_STATE_DTD_EXTERNAL);
                                    setDispatcher(fDTDDispatcher);
                                    return true;
                                }
                            }
                            else if (fExternalSubsetSource != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                                if (((fValidation || fLoadExternalDTD) 
                                    && (fValidationManager == null || !fValidationManager.isCachedDTD()))) {
                                    fDTDScanner.setInputSource(fExternalSubsetSource);
                                    fExternalSubsetSource = null;
                                    setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                                    setDispatcher(fDTDDispatcher);
                                    return true;
                                }                       	
                            }
                            fDTDScanner.setInputSource(null);
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_CONTENT: {
                            reportFatalError("ContentIllegalInProlog", null);
                            fEntityScanner.scanChar();
                        }
                        case SCANNER_STATE_REFERENCE: {
                            reportFatalError("ReferenceIllegalInProlog", null);
                        }
                    }
                } while (complete || again);
                if (complete) {
                    if (fEntityScanner.scanChar() != '<') {
                        reportFatalError("RootElementRequired", null);
                    }
                    setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                    setDispatcher(fContentDispatcher);
                }
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
                reportFatalError("PrematureEOF", null);
                return false;
            }
            return true;
        } 
    } 
    protected final class DTDDispatcher
        implements Dispatcher {
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {
            fEntityManager.setEntityHandler(null);
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_DTD_INTERNAL_DECLS: {
                            boolean completeDTD = true;
                            boolean readExternalSubset = (fValidation || fLoadExternalDTD) && (fValidationManager == null || !fValidationManager.isCachedDTD());
                            boolean moreToScan = fDTDScanner.scanDTDInternalSubset(completeDTD, fStandalone, fHasExternalDTD && readExternalSubset);
                            if (!moreToScan) {
                                if (!fEntityScanner.skipChar(']')) {
                                    reportFatalError("EXPECTED_SQUARE_BRACKET_TO_CLOSE_INTERNAL_SUBSET",
                                                     null);
                                }
                                fEntityScanner.skipSpaces();
                                if (!fEntityScanner.skipChar('>')) {
                                    reportFatalError("DoctypedeclUnterminated", new Object[]{fDoctypeName});
                                }
                                fMarkupDepth--;
                                if (fDoctypeSystemId != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        setScannerState(SCANNER_STATE_DTD_EXTERNAL);
                                        break;
                                    }
                                }
                                else if (fExternalSubsetSource != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        fDTDScanner.setInputSource(fExternalSubsetSource);
                                        fExternalSubsetSource = null;
                                        setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                                        break;
                                    }
                                }
                                else {
                                    fIsEntityDeclaredVC = fEntityManager.hasPEReferences() && !fStandalone;
                                }
                                setScannerState(SCANNER_STATE_PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                                return true;
                            }
                            break;
                        }
                        case SCANNER_STATE_DTD_EXTERNAL: {
                            fDTDDescription.setValues(fDoctypePublicId, fDoctypeSystemId, null, null);
                            fDTDDescription.setRootName(fDoctypeName);
                            XMLInputSource xmlInputSource =
                                fEntityManager.resolveEntity(fDTDDescription);
                            fDTDScanner.setInputSource(xmlInputSource);
                            setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                            again = true;
                            break;
                        }
                        case SCANNER_STATE_DTD_EXTERNAL_DECLS: {
                            boolean completeDTD = true;
                            boolean moreToScan = fDTDScanner.scanDTDExternalSubset(completeDTD);
                            if (!moreToScan) {
                                setScannerState(SCANNER_STATE_PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                                return true;
                            }
                            break;
                        }
                        default: {
                            throw new XNIException("DTDDispatcher#dispatch: scanner state="+fScannerState+" ("+getScannerStateName(fScannerState)+')');
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
                reportFatalError("PrematureEOF", null);
                return false;
            }
            finally {
                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
            }
            return true;
        } 
    } 
    protected class ContentDispatcher
        extends FragmentContentDispatcher {
        protected boolean scanForDoctypeHook()
            throws IOException, XNIException {
            if (fEntityScanner.skipString("DOCTYPE")) {
                setScannerState(SCANNER_STATE_DOCTYPE);
                return true;
            }
            return false;
        } 
        protected boolean elementDepthIsZeroHook()
            throws IOException, XNIException {
            setScannerState(SCANNER_STATE_TRAILING_MISC);
            setDispatcher(fTrailingMiscDispatcher);
            return true;
        } 
        protected boolean scanRootElementHook()
            throws IOException, XNIException {
            if (fExternalSubsetResolver != null && !fSeenDoctypeDecl 
                && !fDisallowDoctype && (fValidation || fLoadExternalDTD)) {
                scanStartElementName();
                resolveExternalSubsetAndRead();
                if (scanStartElementAfterName()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            else if (scanStartElement()) {
                setScannerState(SCANNER_STATE_TRAILING_MISC);
                setDispatcher(fTrailingMiscDispatcher);
                return true;
            }
            return false;
        } 
        protected void endOfFileHook(EOFException e)
            throws IOException, XNIException {
            reportFatalError("PrematureEOF", null);
        } 
        protected void resolveExternalSubsetAndRead()
            throws IOException, XNIException {
            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            fDTDDescription.setRootName(fElementQName.rawname);
            XMLInputSource src = fExternalSubsetResolver.getExternalSubset(fDTDDescription);
            if (src != null) {
                fDoctypeName = fElementQName.rawname;
                fDoctypePublicId = src.getPublicId();
                fDoctypeSystemId = src.getSystemId();
                if (fDocumentHandler != null) {
                    fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
                }
                try {
                    if (fValidationManager == null || !fValidationManager.isCachedDTD()) {
                        fDTDScanner.setInputSource(src);
                        while (fDTDScanner.scanDTDExternalSubset(true));
                    }
                    else {
                        fDTDScanner.setInputSource(null);
                    }
                }
                finally {
                    fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                }
            }
        } 
    } 
    protected final class TrailingMiscDispatcher
        implements Dispatcher {
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_TRAILING_MISC: {
                            fEntityScanner.skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else {
                                setScannerState(SCANNER_STATE_CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('!')) {
                                setScannerState(SCANNER_STATE_COMMENT);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('/')) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                again = true;
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                            }
                            break;
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            if (!fEntityScanner.skipString("--")) {
                                reportFatalError("InvalidCommentStart", null);
                            }
                            scanComment();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_CONTENT: {
                            int ch = fEntityScanner.peekChar();
                            if (ch == -1) {
                                setScannerState(SCANNER_STATE_TERMINATED);
                                return false;
                            }
                            reportFatalError("ContentIllegalInTrailingMisc",
                                             null);
                            fEntityScanner.scanChar();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_REFERENCE: {
                            reportFatalError("ReferenceIllegalInTrailingMisc",
                                             null);
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_TERMINATED: {
                            return false;
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
                if (fMarkupDepth != 0) {
                    reportFatalError("PrematureEOF", null);
                    return false;
                }
                setScannerState(SCANNER_STATE_TERMINATED);
                return false;
            }
            return true;
        } 
    } 
} 
