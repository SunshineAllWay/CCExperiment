package org.apache.xerces.impl;
import java.io.IOException;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
public abstract class XMLScanner 
    implements XMLComponent {
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    protected static final String NAMESPACES = 
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
	protected static final String PARSER_SETTINGS = 
				Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    protected static final String SYMBOL_TABLE = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    protected static final boolean DEBUG_ATTR_NORMALIZATION = false;
    protected boolean fValidation = false;
    protected boolean fNamespaces;
    protected boolean fNotifyCharRefs = false;
	protected boolean fParserSettings = true;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLEntityScanner fEntityScanner;
    protected int fEntityDepth;
    protected String fCharRefLiteral = null;
    protected boolean fScanningAttribute;
    protected boolean fReportEntity;
    protected final static String fVersionSymbol = "version".intern();
    protected final static String fEncodingSymbol = "encoding".intern();
    protected final static String fStandaloneSymbol = "standalone".intern();
    protected final static String fAmpSymbol = "amp".intern();
    protected final static String fLtSymbol = "lt".intern();
    protected final static String fGtSymbol = "gt".intern();
    protected final static String fQuotSymbol = "quot".intern();
    protected final static String fAposSymbol = "apos".intern();
    private final XMLString fString = new XMLString();
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();
    protected final XMLResourceIdentifierImpl fResourceIdentifier = new XMLResourceIdentifierImpl();
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {
		try {
			fParserSettings = componentManager.getFeature(PARSER_SETTINGS);
		} catch (XMLConfigurationException e) {
			fParserSettings = true;
		}
		if (!fParserSettings) {
			init();
			return;
		}
        fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        fEntityManager = (XMLEntityManager)componentManager.getProperty(ENTITY_MANAGER);
        try {
            fValidation = componentManager.getFeature(VALIDATION);
        }
        catch (XMLConfigurationException e) {
            fValidation = false;
        }
        try {
            fNamespaces = componentManager.getFeature(NAMESPACES);
        }
        catch (XMLConfigurationException e) {
            fNamespaces = true;
        }
        try {
            fNotifyCharRefs = componentManager.getFeature(NOTIFY_CHAR_REFS);
        }
        catch (XMLConfigurationException e) {
            fNotifyCharRefs = false;
        }
        init();
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
        	final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SYMBOL_TABLE_PROPERTY.length() && 
                propertyId.endsWith(Constants.SYMBOL_TABLE_PROPERTY)) {
                fSymbolTable = (SymbolTable)value;
            }
            else if (suffixLength == Constants.ERROR_REPORTER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
                fErrorReporter = (XMLErrorReporter)value;
            }
            else if (suffixLength == Constants.ENTITY_MANAGER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (XMLEntityManager)value;
            }
        }
    } 
    public void setFeature(String featureId, boolean value)
        throws XMLConfigurationException {
        if (VALIDATION.equals(featureId)) {
            fValidation = value;
        } else if (NOTIFY_CHAR_REFS.equals(featureId)) {
            fNotifyCharRefs = value;
        }
    }
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
        if (VALIDATION.equals(featureId)) {
            return fValidation;
        } else if (NOTIFY_CHAR_REFS.equals(featureId)) {
            return fNotifyCharRefs;
        }
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
    }
    protected void reset() {
        init();
        fValidation = true;
        fNotifyCharRefs = false;
    }
    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl,
                                         String[] pseudoAttributeValues) 
        throws IOException, XNIException {
        String version = null;
        String encoding = null;
        String standalone = null;
        final int STATE_VERSION = 0;
        final int STATE_ENCODING = 1;
        final int STATE_STANDALONE = 2;
        final int STATE_DONE = 3;
        int state = STATE_VERSION;
        boolean dataFoundForTarget = false;
        boolean sawSpace = fEntityScanner.skipDeclSpaces();
        XMLEntityManager.ScannedEntity currEnt = fEntityManager.getCurrentEntity();
        boolean currLiteral = currEnt.literal;
        currEnt.literal = false;
        while (fEntityScanner.peekChar() != '?') {
            dataFoundForTarget = true;
            String name = scanPseudoAttribute(scanningTextDecl, fString);
            switch (state) {
                case STATE_VERSION: {
                    if (name == fVersionSymbol) {
                        if (!sawSpace) {
                            reportFatalError(scanningTextDecl
                                       ? "SpaceRequiredBeforeVersionInTextDecl"
                                       : "SpaceRequiredBeforeVersionInXMLDecl",
                                             null);
                        }
                        version = fString.toString();
                        state = STATE_ENCODING;
                        if (!versionSupported(version)) {
                            reportFatalError(getVersionNotSupportedKey(), 
                                             new Object[]{version});
                        }
                    }
                    else if (name == fEncodingSymbol) {
                        if (!scanningTextDecl) {
                            reportFatalError("VersionInfoRequired", null);
                        }
                        if (!sawSpace) {
                            reportFatalError(scanningTextDecl
                                      ? "SpaceRequiredBeforeEncodingInTextDecl"
                                      : "SpaceRequiredBeforeEncodingInXMLDecl",
                                             null);
                        }
                        encoding = fString.toString();
                        state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                    }
                    else {
                        if (scanningTextDecl) {
                            reportFatalError("EncodingDeclRequired", null);
                        }
                        else {
                            reportFatalError("VersionInfoRequired", null);
                        }
                    }
                    break;
                }
                case STATE_ENCODING: {
                    if (name == fEncodingSymbol) {
                        if (!sawSpace) {
                            reportFatalError(scanningTextDecl
                                      ? "SpaceRequiredBeforeEncodingInTextDecl"
                                      : "SpaceRequiredBeforeEncodingInXMLDecl",
                                             null);
                        }
                        encoding = fString.toString();
                        state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                    }
                    else if (!scanningTextDecl && name == fStandaloneSymbol) {
                        if (!sawSpace) {
                            reportFatalError("SpaceRequiredBeforeStandalone",
                                             null);
                        }
                        standalone = fString.toString();
                        state = STATE_DONE;
                        if (!standalone.equals("yes") && !standalone.equals("no")) {
                            reportFatalError("SDDeclInvalid", new Object[] {standalone});
                        }
                    }
                    else {
                        reportFatalError("EncodingDeclRequired", null);
                    }
                    break;
                }
                case STATE_STANDALONE: {
                    if (name == fStandaloneSymbol) {
                        if (!sawSpace) {
                            reportFatalError("SpaceRequiredBeforeStandalone",
                                             null);
                        }
                        standalone = fString.toString();
                        state = STATE_DONE;
                        if (!standalone.equals("yes") && !standalone.equals("no")) {
                            reportFatalError("SDDeclInvalid", new Object[] {standalone});
                        }
                    }
                    else {
                        reportFatalError("EncodingDeclRequired", null);
                    }
                    break;
                }
                default: {
                    reportFatalError("NoMorePseudoAttributes", null);
                }
            }
            sawSpace = fEntityScanner.skipDeclSpaces();
        }
        if(currLiteral) 
            currEnt.literal = true;
        if (scanningTextDecl && state != STATE_DONE) {
            reportFatalError("MorePseudoAttributes", null);
        }
        if (scanningTextDecl) {
            if (!dataFoundForTarget && encoding == null) {
                reportFatalError("EncodingDeclRequired", null);
            }
        }
        else {
            if (!dataFoundForTarget && version == null) {
                reportFatalError("VersionInfoRequired", null);
            }
        }
        if (!fEntityScanner.skipChar('?')) {
            reportFatalError("XMLDeclUnterminated", null);
        }
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("XMLDeclUnterminated", null);
        }
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;
    } 
    public String scanPseudoAttribute(boolean scanningTextDecl, 
                                      XMLString value) 
        throws IOException, XNIException {
        String name = fEntityScanner.scanName();
        XMLEntityManager.print(fEntityManager.getCurrentEntity());
        if (name == null) {
            reportFatalError("PseudoAttrNameExpected", null);
        }
        fEntityScanner.skipDeclSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError(scanningTextDecl ? "EqRequiredInTextDecl"
                             : "EqRequiredInXMLDecl", new Object[]{name});
        }
        fEntityScanner.skipDeclSpaces();
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
            reportFatalError(scanningTextDecl ? "QuoteRequiredInTextDecl"
                             : "QuoteRequiredInXMLDecl" , new Object[]{name});
        }
        fEntityScanner.scanChar();
        int c = fEntityScanner.scanLiteral(quote, value);
        if (c != quote) {
            fStringBuffer2.clear();
            do {
                fStringBuffer2.append(value);
                if (c != -1) {
                    if (c == '&' || c == '%' || c == '<' || c == ']') {
                        fStringBuffer2.append((char)fEntityScanner.scanChar());
                    }
                    else if (XMLChar.isHighSurrogate(c)) {
                        scanSurrogates(fStringBuffer2);
                    }
                    else if (isInvalidLiteral(c)) {
                        String key = scanningTextDecl
                            ? "InvalidCharInTextDecl" : "InvalidCharInXMLDecl";
                        reportFatalError(key,
                                       new Object[] {Integer.toString(c, 16)});
                        fEntityScanner.scanChar();
                    }
                }
                c = fEntityScanner.scanLiteral(quote, value);
            } while (c != quote);
            fStringBuffer2.append(value);
            value.setValues(fStringBuffer2);
        }
        if (!fEntityScanner.skipChar(quote)) {
            reportFatalError(scanningTextDecl ? "CloseQuoteMissingInTextDecl"
                             : "CloseQuoteMissingInXMLDecl",
                             new Object[]{name});
        }
        return name;
    } 
    protected void scanPI() throws IOException, XNIException {
        fReportEntity = false;
        String target = null;
        if(fNamespaces) {
            target = fEntityScanner.scanNCName();
        } else {
            target = fEntityScanner.scanName();
        }
        if (target == null) {
            reportFatalError("PITargetRequired", null);
        }
        scanPIData(target, fString);
        fReportEntity = true;
    } 
    protected void scanPIData(String target, XMLString data) 
        throws IOException, XNIException {
        if (target.length() == 3) {
            char c0 = Character.toLowerCase(target.charAt(0));
            char c1 = Character.toLowerCase(target.charAt(1));
            char c2 = Character.toLowerCase(target.charAt(2));
            if (c0 == 'x' && c1 == 'm' && c2 == 'l') {
                reportFatalError("ReservedPITarget", null);
            }
        }
        if (!fEntityScanner.skipSpaces()) {
            if (fEntityScanner.skipString("?>")) {
                data.clear();
                return;
            }
            else {
                if(fNamespaces && fEntityScanner.peekChar() == ':') { 
                    fEntityScanner.scanChar();
                    XMLStringBuffer colonName = new XMLStringBuffer(target);
                    colonName.append(':');
                    String str = fEntityScanner.scanName();
                    if (str != null)
                        colonName.append(str);
                    reportFatalError("ColonNotLegalWithNS", new Object[] {colonName.toString()});
                    fEntityScanner.skipSpaces();
                } else {
                    reportFatalError("SpaceRequiredInPI", null);
                }
            }
        }
        fStringBuffer.clear();
        if (fEntityScanner.scanData("?>", fStringBuffer)) {
            do {
                int c = fEntityScanner.peekChar();
                if (c != -1) {
                    if (XMLChar.isHighSurrogate(c)) {
                        scanSurrogates(fStringBuffer);
                    }
                    else if (isInvalidLiteral(c)) {
                        reportFatalError("InvalidCharInPI",
                                         new Object[]{Integer.toHexString(c)});
                        fEntityScanner.scanChar();
                    }
                }
            } while (fEntityScanner.scanData("?>", fStringBuffer));
        }
        data.setValues(fStringBuffer);
    } 
    protected void scanComment(XMLStringBuffer text)
        throws IOException, XNIException {
        text.clear();
        while (fEntityScanner.scanData("--", text)) {
            int c = fEntityScanner.peekChar();
            if (c != -1) {
                if (XMLChar.isHighSurrogate(c)) {
                    scanSurrogates(text);
                }
                else if (isInvalidLiteral(c)) {
                    reportFatalError("InvalidCharInComment",
                                     new Object[] { Integer.toHexString(c) }); 
                    fEntityScanner.scanChar();
                }
            } 
        }
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("DashDashInComment", null);
        }
    } 
    protected boolean scanAttributeValue(XMLString value, 
                                      XMLString nonNormalizedValue,
                                      String atName,
                                      boolean checkEntities,String eleName)
        throws IOException, XNIException
    {
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
			reportFatalError("OpenQuoteExpected", new Object[]{eleName,atName});
        }
        fEntityScanner.scanChar();
        int entityDepth = fEntityDepth;
        int c = fEntityScanner.scanLiteral(quote, value);
        if (DEBUG_ATTR_NORMALIZATION) {
            System.out.println("** scanLiteral -> \""
                               + value.toString() + "\"");
        }
        int fromIndex = 0;
        if (c == quote && (fromIndex = isUnchangedByNormalization(value)) == -1) {
            nonNormalizedValue.setValues(value);
            int cquote = fEntityScanner.scanChar();
            if (cquote != quote) {
                reportFatalError("CloseQuoteExpected", new Object[]{eleName,atName});
            }
            return true;
        }
        fStringBuffer2.clear();
        fStringBuffer2.append(value);
        normalizeWhitespace(value, fromIndex);
        if (DEBUG_ATTR_NORMALIZATION) {
            System.out.println("** normalizeWhitespace -> \""
                               + value.toString() + "\"");
        }
        if (c != quote) {
            fScanningAttribute = true;
            fStringBuffer.clear();
            do {
                fStringBuffer.append(value);
                if (DEBUG_ATTR_NORMALIZATION) {
                    System.out.println("** value2: \""
                                       + fStringBuffer.toString() + "\"");
                }
                if (c == '&') {
                    fEntityScanner.skipChar('&');
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append('&');
                    }
                    if (fEntityScanner.skipChar('#')) {
                        if (entityDepth == fEntityDepth) {
                            fStringBuffer2.append('#');
                        }
                        int ch = scanCharReferenceValue(fStringBuffer, fStringBuffer2);
                        if (ch != -1) {
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** value3: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                    }
                    else {
                        String entityName = fEntityScanner.scanName();
                        if (entityName == null) {
                            reportFatalError("NameRequiredInReference", null);
                        }
                        else if (entityDepth == fEntityDepth) {
                            fStringBuffer2.append(entityName);
                        }
                        if (!fEntityScanner.skipChar(';')) {
                            reportFatalError("SemicolonRequiredInReference",
                                             new Object []{entityName});
                        }
                        else if (entityDepth == fEntityDepth) {
                            fStringBuffer2.append(';');
                        }
                        if (entityName == fAmpSymbol) {
                            fStringBuffer.append('&');
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** value5: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                        else if (entityName == fAposSymbol) {
                            fStringBuffer.append('\'');
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** value7: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                        else if (entityName == fLtSymbol) {
                            fStringBuffer.append('<');
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** value9: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                        else if (entityName == fGtSymbol) {
                            fStringBuffer.append('>');
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** valueB: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                        else if (entityName == fQuotSymbol) {
                            fStringBuffer.append('"');
                            if (DEBUG_ATTR_NORMALIZATION) {
                                System.out.println("** valueD: \""
                                                   + fStringBuffer.toString()
                                                   + "\"");
                            }
                        }
                        else {
                            if (fEntityManager.isExternalEntity(entityName)) {
                                reportFatalError("ReferenceToExternalEntity",
                                                 new Object[] { entityName });
                            }
                            else {
                                if (!fEntityManager.isDeclaredEntity(entityName)) {
                                    if (checkEntities) {
                                        if (fValidation) {
                                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                                                       "EntityNotDeclared",
                                                                       new Object[]{entityName},
                                                                       XMLErrorReporter.SEVERITY_ERROR);
                                        }
                                    }
                                    else {
                                        reportFatalError("EntityNotDeclared",
                                                         new Object[]{entityName});
                                    }
                                }
                                fEntityManager.startEntity(entityName, true);
                            }
                        }
                    }
                }
                else if (c == '<') {
                    reportFatalError("LessthanInAttValue",
									 new Object[] { eleName, atName });
                    fEntityScanner.scanChar();
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char)c);
                    }
                }
                else if (c == '%' || c == ']') {
                    fEntityScanner.scanChar();
                    fStringBuffer.append((char)c);
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char)c);
                    }
                    if (DEBUG_ATTR_NORMALIZATION) {
                        System.out.println("** valueF: \""
                                           + fStringBuffer.toString() + "\"");
                    }
                }
                else if (c == '\n' || c == '\r') {
                    fEntityScanner.scanChar();
                    fStringBuffer.append(' ');
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append('\n');
                    }
                }
                else if (c != -1 && XMLChar.isHighSurrogate(c)) {
                    fStringBuffer3.clear();
                    if (scanSurrogates(fStringBuffer3)) {
                        fStringBuffer.append(fStringBuffer3);
                        if (entityDepth == fEntityDepth) {
                            fStringBuffer2.append(fStringBuffer3);
                        }
                        if (DEBUG_ATTR_NORMALIZATION) {
                            System.out.println("** valueI: \""
                                               + fStringBuffer.toString()
                                               + "\"");
                        }
                    }
                }
                else if (c != -1 && isInvalidLiteral(c)) {
                    reportFatalError("InvalidCharInAttValue",
					new Object[] {eleName, atName, Integer.toString(c, 16)});
                    fEntityScanner.scanChar();
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char)c);
                    }
                }
                c = fEntityScanner.scanLiteral(quote, value);
                if (entityDepth == fEntityDepth) {
                    fStringBuffer2.append(value);
                }
                normalizeWhitespace(value);
            } while (c != quote || entityDepth != fEntityDepth);
            fStringBuffer.append(value);
            if (DEBUG_ATTR_NORMALIZATION) {
                System.out.println("** valueN: \""
                                   + fStringBuffer.toString() + "\"");
            }
            value.setValues(fStringBuffer);
            fScanningAttribute = false;
        }
        nonNormalizedValue.setValues(fStringBuffer2);
        int cquote = fEntityScanner.scanChar();
        if (cquote != quote) {
			reportFatalError("CloseQuoteExpected", new Object[]{eleName,atName});
        }
        return nonNormalizedValue.equals(value.ch, value.offset, value.length);
    } 
    protected void scanExternalID(String[] identifiers,
                                  boolean optionalSystemId)
        throws IOException, XNIException {
        String systemId = null;
        String publicId = null;
        if (fEntityScanner.skipString("PUBLIC")) {
            if (!fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterPUBLIC", null);
            }
            scanPubidLiteral(fString);
            publicId = fString.toString();
            if (!fEntityScanner.skipSpaces() && !optionalSystemId) {
                reportFatalError("SpaceRequiredBetweenPublicAndSystem", null);
            }
        }
        if (publicId != null || fEntityScanner.skipString("SYSTEM")) {
            if (publicId == null && !fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterSYSTEM", null);
            }
            int quote = fEntityScanner.peekChar();
            if (quote != '\'' && quote != '"') {
                if (publicId != null && optionalSystemId) {
                    identifiers[0] = null;
                    identifiers[1] = publicId;
                    return;
                }
                reportFatalError("QuoteRequiredInSystemID", null);
            }
            fEntityScanner.scanChar();
            XMLString ident = fString;
            if (fEntityScanner.scanLiteral(quote, ident) != quote) {
                fStringBuffer.clear();
                do {
                    fStringBuffer.append(ident);
                    int c = fEntityScanner.peekChar();
                    if (XMLChar.isMarkup(c) || c == ']') {
                        fStringBuffer.append((char)fEntityScanner.scanChar());
                    }
                    else if (XMLChar.isHighSurrogate(c)) {
                        scanSurrogates(fStringBuffer);
                    }
                    else if (isInvalidLiteral(c)) {
                        reportFatalError("InvalidCharInSystemID",
                                new Object[] { Integer.toHexString(c) }); 
                        fEntityScanner.scanChar();
                    }
                } while (fEntityScanner.scanLiteral(quote, ident) != quote);
                fStringBuffer.append(ident);
                ident = fStringBuffer;
            }
            systemId = ident.toString();
            if (!fEntityScanner.skipChar(quote)) {
                reportFatalError("SystemIDUnterminated", null);
            }
        }
        identifiers[0] = systemId;
        identifiers[1] = publicId;
    }
    protected boolean scanPubidLiteral(XMLString literal)
        throws IOException, XNIException
    {
        int quote = fEntityScanner.scanChar();
        if (quote != '\'' && quote != '"') {
            reportFatalError("QuoteRequiredInPublicID", null);
            return false;
        }
        fStringBuffer.clear();
        boolean skipSpace = true;
        boolean dataok = true;
        while (true) {
            int c = fEntityScanner.scanChar();
            if (c == ' ' || c == '\n' || c == '\r') {
                if (!skipSpace) {
                    fStringBuffer.append(' ');
                    skipSpace = true;
                }
            }
            else if (c == quote) {
                if (skipSpace) {
                    fStringBuffer.length--;
                }
                literal.setValues(fStringBuffer);
                break;
            }
            else if (XMLChar.isPubid(c)) {
                fStringBuffer.append((char)c);
                skipSpace = false;
            }
            else if (c == -1) {
                reportFatalError("PublicIDUnterminated", null);
                return false;
            }
            else {
                dataok = false;
                reportFatalError("InvalidCharInPublicID",
                                 new Object[]{Integer.toHexString(c)});
            }
        }
        return dataok;
   }
    protected void normalizeWhitespace(XMLString value) {
        int end = value.offset + value.length;
        for (int i = value.offset; i < end; ++i) {
            int c = value.ch[i];
            if (c < 0x20) {
                value.ch[i] = ' ';
            }
        }
    }
    protected void normalizeWhitespace(XMLString value, int fromIndex) {
        int end = value.offset + value.length;
        for (int i = value.offset + fromIndex; i < end; ++i) {
            int c = value.ch[i];
            if (c < 0x20) {
                value.ch[i] = ' ';
            }
        }
    }
    protected int isUnchangedByNormalization(XMLString value) {
        int end = value.offset + value.length;
        for (int i = value.offset; i < end; ++i) {
            int c = value.ch[i];
            if (c < 0x20) {
                return i - value.offset;
            }
        }
        return -1;
    }
    public void startEntity(String name, 
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {
        fEntityDepth++;
        fEntityScanner = fEntityManager.getEntityScanner();
    } 
    public void endEntity(String name, Augmentations augs) throws XNIException {
        fEntityDepth--;
    } 
    protected int scanCharReferenceValue(XMLStringBuffer buf, XMLStringBuffer buf2) 
        throws IOException, XNIException {
        boolean hex = false;
        if (fEntityScanner.skipChar('x')) {
            if (buf2 != null) { buf2.append('x'); }
            hex = true;
            fStringBuffer3.clear();
            boolean digit = true;
            int c = fEntityScanner.peekChar();
            digit = (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'f') ||
                    (c >= 'A' && c <= 'F');
            if (digit) {
                if (buf2 != null) { buf2.append((char)c); }
                fEntityScanner.scanChar();
                fStringBuffer3.append((char)c);
                do {
                    c = fEntityScanner.peekChar();
                    digit = (c >= '0' && c <= '9') ||
                            (c >= 'a' && c <= 'f') ||
                            (c >= 'A' && c <= 'F');
                    if (digit) {
                        if (buf2 != null) { buf2.append((char)c); }
                        fEntityScanner.scanChar();
                        fStringBuffer3.append((char)c);
                    }
                } while (digit);
            }
            else {
                reportFatalError("HexdigitRequiredInCharRef", null);
            }
        }
        else {
            fStringBuffer3.clear();
            boolean digit = true;
            int c = fEntityScanner.peekChar();
            digit = c >= '0' && c <= '9';
            if (digit) {
                if (buf2 != null) { buf2.append((char)c); }
                fEntityScanner.scanChar();
                fStringBuffer3.append((char)c);
                do {
                    c = fEntityScanner.peekChar();
                    digit = c >= '0' && c <= '9';
                    if (digit) {
                        if (buf2 != null) { buf2.append((char)c); }
                        fEntityScanner.scanChar();
                        fStringBuffer3.append((char)c);
                    }
                } while (digit);
            }
            else {
                reportFatalError("DigitRequiredInCharRef", null);
            }
        }
        if (!fEntityScanner.skipChar(';')) {
            reportFatalError("SemicolonRequiredInCharRef", null);
        }
        if (buf2 != null) { buf2.append(';'); }
        int value = -1;
        try {
            value = Integer.parseInt(fStringBuffer3.toString(),
                                     hex ? 16 : 10);
            if (isInvalid(value)) {
            	StringBuffer errorBuf = new StringBuffer(fStringBuffer3.length + 1);
                if (hex) errorBuf.append('x');
                errorBuf.append(fStringBuffer3.ch, fStringBuffer3.offset, fStringBuffer3.length);
                reportFatalError("InvalidCharRef",
                                 new Object[]{errorBuf.toString()});
            }
        }
        catch (NumberFormatException e) {
            StringBuffer errorBuf = new StringBuffer(fStringBuffer3.length + 1);
            if (hex) errorBuf.append('x');
            errorBuf.append(fStringBuffer3.ch, fStringBuffer3.offset, fStringBuffer3.length);
            reportFatalError("InvalidCharRef",
                             new Object[]{errorBuf.toString()});
        }
        if (!XMLChar.isSupplemental(value)) {
            buf.append((char) value);
        }
        else {
            buf.append(XMLChar.highSurrogate(value));
            buf.append(XMLChar.lowSurrogate(value));
        }
        if (fNotifyCharRefs && value != -1) {
            String literal = "#" + (hex ? "x" : "") + fStringBuffer3.toString();
            if (!fScanningAttribute) {
                fCharRefLiteral = literal;
            }
        }
        return value;
    }
    protected boolean isInvalid(int value) {
        return (XMLChar.isInvalid(value)); 
    } 
    protected boolean isInvalidLiteral(int value) {
        return (XMLChar.isInvalid(value)); 
    } 
    protected boolean isValidNameChar(int value) {
        return (XMLChar.isName(value)); 
    } 
    protected boolean isValidNameStartChar(int value) {
        return (XMLChar.isNameStart(value)); 
    } 
    protected boolean isValidNCName(int value) {
        return (XMLChar.isNCName(value));
    } 
    protected boolean isValidNameStartHighSurrogate(int value) {
        return false; 
    } 
    protected boolean versionSupported(String version ) {
        return version.equals("1.0");
    } 
    protected String getVersionNotSupportedKey () {
        return "VersionNotSupported";
    } 
    protected boolean scanSurrogates(XMLStringBuffer buf)
        throws IOException, XNIException {
        int high = fEntityScanner.scanChar();
        int low = fEntityScanner.peekChar();
        if (!XMLChar.isLowSurrogate(low)) {
            reportFatalError("InvalidCharInContent",
                             new Object[] {Integer.toString(high, 16)});
            return false;
        }
        fEntityScanner.scanChar();
        int c = XMLChar.supplemental((char)high, (char)low);
        if (isInvalid(c)) {
            reportFatalError("InvalidCharInContent",
                             new Object[]{Integer.toString(c, 16)}); 
            return false;
        }
        buf.append((char)high);
        buf.append((char)low);
        return true;
    } 
    protected void reportFatalError(String msgId, Object[] args)
        throws XNIException {
        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                   msgId, args,
                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
    }
    private void init() { 
        fEntityScanner = null;       
        fEntityDepth = 0;
        fReportEntity = true;
        fResourceIdentifier.clear();
    } 
} 
