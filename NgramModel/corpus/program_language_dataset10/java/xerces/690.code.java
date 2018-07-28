package org.apache.xerces.xpointer;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xinclude.XIncludeHandler;
import org.apache.xerces.xinclude.XIncludeNamespaceSupport;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
public final class XPointerHandler extends XIncludeHandler implements
        XPointerProcessor {
    protected ArrayList fXPointerParts = null;
    protected XPointerPart fXPointerPart = null;
    protected boolean fFoundMatchingPtrPart = false;
    protected XMLErrorReporter fXPointerErrorReporter;
    protected XMLErrorHandler fErrorHandler;
    protected SymbolTable fSymbolTable = null;
    private final String ELEMENT_SCHEME_NAME = "element";
    protected boolean fIsXPointerResolved = false;
    protected boolean fFixupBase = false;
    protected boolean fFixupLang = false;
    public XPointerHandler() {
        super();
        fXPointerParts = new ArrayList();
        fSymbolTable = new SymbolTable();
    }
    public XPointerHandler(SymbolTable symbolTable,
            XMLErrorHandler errorHandler, XMLErrorReporter errorReporter) {
        super();
        fXPointerParts = new ArrayList();
        fSymbolTable = symbolTable;
        fErrorHandler = errorHandler;
        fXPointerErrorReporter = errorReporter;
    }
    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    }
    public void parseXPointer(String xpointer) throws XNIException {
        init();
        final Tokens tokens = new Tokens(fSymbolTable);
        Scanner scanner = new Scanner(fSymbolTable) {
            protected void addToken(Tokens tokens, int token)
                    throws XNIException {
                if (token == Tokens.XPTRTOKEN_OPEN_PAREN
                        || token == Tokens.XPTRTOKEN_CLOSE_PAREN
                        || token == Tokens.XPTRTOKEN_SCHEMENAME
                        || token == Tokens.XPTRTOKEN_SCHEMEDATA
                        || token == Tokens.XPTRTOKEN_SHORTHAND) {
                    super.addToken(tokens, token);
                    return;
                }
                reportError("InvalidXPointerToken", new Object[] { tokens
                        .getTokenString(token) });
            }
        };
        int length = xpointer.length();
        boolean success = scanner.scanExpr(fSymbolTable, tokens, xpointer, 0,
                length);
        if (!success)
            reportError("InvalidXPointerExpression", new Object[] { xpointer });
        while (tokens.hasMore()) {
            int token = tokens.nextToken();
            switch (token) {
            case Tokens.XPTRTOKEN_SHORTHAND: {
                token = tokens.nextToken();
                String shortHandPointerName = tokens.getTokenString(token);
                if (shortHandPointerName == null) {
                    reportError("InvalidXPointerExpression",
                            new Object[] { xpointer });
                }
                XPointerPart shortHandPointer = new ShortHandPointer(
                        fSymbolTable);
                shortHandPointer.setSchemeName(shortHandPointerName);
                fXPointerParts.add(shortHandPointer);
                break;
            }
            case Tokens.XPTRTOKEN_SCHEMENAME: {
                token = tokens.nextToken();
                String prefix = tokens.getTokenString(token);
                token = tokens.nextToken();
                String localName = tokens.getTokenString(token);
                String schemeName = prefix + localName;
                int openParenCount = 0;
                int closeParenCount = 0;
                token = tokens.nextToken();
                String openParen = tokens.getTokenString(token);
                if (openParen != "XPTRTOKEN_OPEN_PAREN") {
                    if (token == Tokens.XPTRTOKEN_SHORTHAND) {
                        reportError("MultipleShortHandPointers",
                                new Object[] { xpointer });
                    } else {
                        reportError("InvalidXPointerExpression",
                                new Object[] { xpointer });
                    }
                }
                openParenCount++;
                String schemeData = null;
                while (tokens.hasMore()) {
                    token = tokens.nextToken();
                    schemeData = tokens.getTokenString(token);
                    if (schemeData != "XPTRTOKEN_OPEN_PAREN") {
                        break;
                    }
                    openParenCount++;
                }
                token = tokens.nextToken();
                schemeData = tokens.getTokenString(token);
                token = tokens.nextToken();
                String closeParen = tokens.getTokenString(token);
                if (closeParen != "XPTRTOKEN_CLOSE_PAREN") {
                    reportError("SchemeDataNotFollowedByCloseParenthesis",
                            new Object[] { xpointer });
                }
                closeParenCount++;
                while (tokens.hasMore()) {
                    if (tokens.getTokenString(tokens.peekToken()) != "XPTRTOKEN_OPEN_PAREN") {
                        break;
                    }
                    closeParenCount++;
                }
                if (openParenCount != closeParenCount) {
                    reportError("UnbalancedParenthesisInXPointerExpression",
                            new Object[] { xpointer,
                                    new Integer(openParenCount),
                                    new Integer(closeParenCount) });
                }
                if (schemeName.equals(ELEMENT_SCHEME_NAME)) {
                    XPointerPart elementSchemePointer = new ElementSchemePointer(
                            fSymbolTable, fErrorReporter);
                    elementSchemePointer.setSchemeName(schemeName);
                    elementSchemePointer.setSchemeData(schemeData);
                    try {
                        elementSchemePointer.parseXPointer(schemeData);
                        fXPointerParts.add(elementSchemePointer);
                    } catch (XNIException e) {
                    	throw new XNIException (e);
                    }
                } else {
                    reportWarning("SchemeUnsupported",
                            new Object[] { schemeName });
                }
                break;
            }
            default:
                reportError("InvalidXPointerExpression",
                        new Object[] { xpointer });
            }
        }
    }
    public boolean resolveXPointer(QName element, XMLAttributes attributes,
            Augmentations augs, int event) throws XNIException {
    	boolean resolved = false;
        if (!fFoundMatchingPtrPart) {
            for (int i = 0; i < fXPointerParts.size(); i++) {
                fXPointerPart = (XPointerPart) fXPointerParts.get(i);
                if (fXPointerPart.resolveXPointer(element, attributes, augs,
                        event)) {
                    fFoundMatchingPtrPart = true;
                    resolved = true;
                }
            }
        } else {
            if (fXPointerPart.resolveXPointer(element, attributes, augs, event)) {
            	resolved = true;
            }
        }
        if (!fIsXPointerResolved) {
            fIsXPointerResolved = resolved;
        }
        return resolved;
    }
    public boolean isFragmentResolved() throws XNIException {
        boolean resolved = (fXPointerPart != null) ? fXPointerPart.isFragmentResolved()
                : false;
        if (!fIsXPointerResolved) {
            fIsXPointerResolved = resolved;
        }
        return resolved;
    }
    public boolean isChildFragmentResolved() throws XNIException {
    	boolean resolved = (fXPointerPart != null) ? fXPointerPart
				.isChildFragmentResolved() : false;
		return resolved;
    }
    public boolean isXPointerResolved() throws XNIException {
        return fIsXPointerResolved;
    }
    public XPointerPart getXPointerPart() {
        return fXPointerPart;
    }
    private void reportError(String key, Object[] arguments)
            throws XNIException {
    	throw new XNIException((fErrorReporter
				.getMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN))
				.formatMessage(fErrorReporter.getLocale(), key, arguments));
    }
    private void reportWarning(String key, Object[] arguments)
            throws XNIException {
        fXPointerErrorReporter.reportError(
                XPointerMessageFormatter.XPOINTER_DOMAIN, key, arguments,
                XMLErrorReporter.SEVERITY_WARNING);
    }
    protected void initErrorReporter() {
        if (fXPointerErrorReporter == null) {
            fXPointerErrorReporter = new XMLErrorReporter();
        }
        if (fErrorHandler == null) {
            fErrorHandler = new XPointerErrorHandler();
        }
        fXPointerErrorReporter.putMessageFormatter(
                XPointerMessageFormatter.XPOINTER_DOMAIN,
                new XPointerMessageFormatter());
    }
    protected void init() {
        fXPointerParts.clear();
        fXPointerPart = null;
        fFoundMatchingPtrPart = false;
        fIsXPointerResolved = false;
        initErrorReporter();
    }
    public ArrayList getPointerParts() {
        return fXPointerParts;
    }
    private final class Tokens {
        private static final int XPTRTOKEN_OPEN_PAREN = 0,
                XPTRTOKEN_CLOSE_PAREN = 1, XPTRTOKEN_SHORTHAND = 2,
                XPTRTOKEN_SCHEMENAME = 3, XPTRTOKEN_SCHEMEDATA = 4;
        private final String[] fgTokenNames = { "XPTRTOKEN_OPEN_PAREN",
                "XPTRTOKEN_CLOSE_PAREN", "XPTRTOKEN_SHORTHAND",
                "XPTRTOKEN_SCHEMENAME", "XPTRTOKEN_SCHEMEDATA" };
        private static final int INITIAL_TOKEN_COUNT = 1 << 8;
        private int[] fTokens = new int[INITIAL_TOKEN_COUNT];
        private int fTokenCount = 0;
        private int fCurrentTokenIndex;
        private SymbolTable fSymbolTable;
        private HashMap fTokenNames = new HashMap();
        private Tokens(SymbolTable symbolTable) {
            fSymbolTable = symbolTable;
            fTokenNames.put(new Integer(XPTRTOKEN_OPEN_PAREN),
                    "XPTRTOKEN_OPEN_PAREN");
            fTokenNames.put(new Integer(XPTRTOKEN_CLOSE_PAREN),
                    "XPTRTOKEN_CLOSE_PAREN");
            fTokenNames.put(new Integer(XPTRTOKEN_SHORTHAND),
                    "XPTRTOKEN_SHORTHAND");
            fTokenNames.put(new Integer(XPTRTOKEN_SCHEMENAME),
                    "XPTRTOKEN_SCHEMENAME");
            fTokenNames.put(new Integer(XPTRTOKEN_SCHEMEDATA),
                    "XPTRTOKEN_SCHEMEDATA");
        }
        private String getTokenString(int token) {
            return (String) fTokenNames.get(new Integer(token));
        }
        private void addToken(String tokenStr) {
            Integer tokenInt = (Integer) fTokenNames.get(tokenStr);
            if (tokenInt == null) {
                tokenInt = new Integer(fTokenNames.size());
                fTokenNames.put(tokenInt, tokenStr);
            }
            addToken(tokenInt.intValue());
        }
        private void addToken(int token) {
            try {
                fTokens[fTokenCount] = token;
            } catch (ArrayIndexOutOfBoundsException ex) {
                int[] oldList = fTokens;
                fTokens = new int[fTokenCount << 1];
                System.arraycopy(oldList, 0, fTokens, 0, fTokenCount);
                fTokens[fTokenCount] = token;
            }
            fTokenCount++;
        }
        private void rewind() {
            fCurrentTokenIndex = 0;
        }
        private boolean hasMore() {
            return fCurrentTokenIndex < fTokenCount;
        }
        private int nextToken() throws XNIException {
            if (fCurrentTokenIndex == fTokenCount) {
                reportError("XPointerProcessingError", null);
            }
            return fTokens[fCurrentTokenIndex++];
        }
        private int peekToken() throws XNIException {
            if (fCurrentTokenIndex == fTokenCount) {
                reportError("XPointerProcessingError", null);
            }
            return fTokens[fCurrentTokenIndex];
        }
        private String nextTokenAsString() throws XNIException {
            String tokenStrint = getTokenString(nextToken());
            if (tokenStrint == null) {
                reportError("XPointerProcessingError", null);
            }
            return tokenStrint;
        }
    }
    private class Scanner {
        private static final byte CHARTYPE_INVALID = 0, 
                CHARTYPE_OTHER = 1, 
                CHARTYPE_WHITESPACE = 2, 
                CHARTYPE_CARRET = 3, 
                CHARTYPE_OPEN_PAREN = 4, 
                CHARTYPE_CLOSE_PAREN = 5, 
                CHARTYPE_MINUS = 6, 
                CHARTYPE_PERIOD = 7, 
                CHARTYPE_SLASH = 8, 
                CHARTYPE_DIGIT = 9, 
                CHARTYPE_COLON = 10, 
                CHARTYPE_EQUAL = 11, 
                CHARTYPE_LETTER = 12, 
                CHARTYPE_UNDERSCORE = 13, 
                CHARTYPE_NONASCII = 14; 
        private final byte[] fASCIICharMap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2,
                0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                2, 1, 1, 1, 1, 1, 1, 1, 4, 5, 1, 1, 1, 6, 7, 8, 9, 9, 9, 9, 9,
                9, 9, 9, 9, 9, 10, 1, 1, 11, 1, 1, 1, 12, 12, 12, 12, 12, 12,
                12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
                12, 12, 12, 12, 1, 1, 1, 3, 13, 1, 12, 12, 12, 12, 12, 12, 12,
                12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
                12, 12, 12, 1, 1, 1, 1, 1 };
        private SymbolTable fSymbolTable;
        private Scanner(SymbolTable symbolTable) {
            fSymbolTable = symbolTable;
        } 
        private boolean scanExpr(SymbolTable symbolTable, Tokens tokens,
                String data, int currentOffset, int endOffset)
                throws XNIException {
            int ch;
            int openParen = 0;
            int closeParen = 0;
            int nameOffset, dataOffset;
            boolean isQName = false;
            String name = null;
            String prefix = null;
            String schemeData = null;
            StringBuffer schemeDataBuff = new StringBuffer();
            while (true) {
                if (currentOffset == endOffset) {
                    break;
                }
                ch = data.charAt(currentOffset);
                while (ch == ' ' || ch == 0x0A || ch == 0x09 || ch == 0x0D) {
                    if (++currentOffset == endOffset) {
                        break;
                    }
                    ch = data.charAt(currentOffset);
                }
                if (currentOffset == endOffset) {
                    break;
                }
                byte chartype = (ch >= 0x80) ? CHARTYPE_NONASCII
                        : fASCIICharMap[ch];
                switch (chartype) {
                case CHARTYPE_OPEN_PAREN: 
                    addToken(tokens, Tokens.XPTRTOKEN_OPEN_PAREN);
                    openParen++;
                    ++currentOffset;
                    break;
                case CHARTYPE_CLOSE_PAREN: 
                    addToken(tokens, Tokens.XPTRTOKEN_CLOSE_PAREN);
                    closeParen++;
                    ++currentOffset;
                    break;
                case CHARTYPE_CARRET:
                case CHARTYPE_COLON:
                case CHARTYPE_DIGIT:
                case CHARTYPE_EQUAL:
                case CHARTYPE_LETTER:
                case CHARTYPE_MINUS:
                case CHARTYPE_NONASCII:
                case CHARTYPE_OTHER:
                case CHARTYPE_PERIOD:
                case CHARTYPE_SLASH:
                case CHARTYPE_UNDERSCORE:
                case CHARTYPE_WHITESPACE:
                    if (openParen == 0) {
                        nameOffset = currentOffset;
                        currentOffset = scanNCName(data, endOffset,
                                currentOffset);
                        if (currentOffset == nameOffset) {
                            reportError("InvalidShortHandPointer",
                                    new Object[] { data });
                            return false;
                        }
                        if (currentOffset < endOffset) {
                            ch = data.charAt(currentOffset);
                        } else {
                            ch = -1;
                        }
                        name = symbolTable.addSymbol(data.substring(nameOffset,
                                currentOffset));
                        prefix = XMLSymbols.EMPTY_STRING;
                        if (ch == ':') {
                            if (++currentOffset == endOffset) {
                                return false;
                            }
                            ch = data.charAt(currentOffset);
                            prefix = name;
                            nameOffset = currentOffset;
                            currentOffset = scanNCName(data, endOffset,
                                    currentOffset);
                            if (currentOffset == nameOffset) {
                                return false;
                            }
                            if (currentOffset < endOffset) {
                                ch = data.charAt(currentOffset);
                            } else {
                                ch = -1;
                            }
                            isQName = true;
                            name = symbolTable.addSymbol(data.substring(
                                    nameOffset, currentOffset));
                        }
                        if (currentOffset != endOffset) {
                            addToken(tokens, Tokens.XPTRTOKEN_SCHEMENAME);
                            tokens.addToken(prefix);
                            tokens.addToken(name);
                            isQName = false;
                        } else if (currentOffset == endOffset) {
                            addToken(tokens, Tokens.XPTRTOKEN_SHORTHAND);
                            tokens.addToken(name);
                            isQName = false;
                        }
                        closeParen = 0;
                        break;
                    } else if (openParen > 0 && closeParen == 0 && name != null) {
                        dataOffset = currentOffset;
                        currentOffset = scanData(data, schemeDataBuff,
                                endOffset, currentOffset);
                        if (currentOffset == dataOffset) {
                            reportError("InvalidSchemeDataInXPointer",
                                    new Object[] { data });
                            return false;
                        }
                        if (currentOffset < endOffset) {
                            ch = data.charAt(currentOffset);
                        } else {
                            ch = -1;
                        }
                        schemeData = symbolTable.addSymbol(schemeDataBuff
                                .toString());
                        addToken(tokens, Tokens.XPTRTOKEN_SCHEMEDATA);
                        tokens.addToken(schemeData);
                        openParen = 0;
                        schemeDataBuff.delete(0, schemeDataBuff.length());
                    } else {
                        return false;
                    }
                }
            } 
            return true;
        }
        private int scanNCName(String data, int endOffset, int currentOffset) {
            int ch = data.charAt(currentOffset);
            if (ch >= 0x80) {
                if (!XMLChar.isNameStart(ch)) {
                    return currentOffset;
                }
            } else {
                byte chartype = fASCIICharMap[ch];
                if (chartype != CHARTYPE_LETTER
                        && chartype != CHARTYPE_UNDERSCORE) {
                    return currentOffset;
                }
            }
            while (++currentOffset < endOffset) {
                ch = data.charAt(currentOffset);
                if (ch >= 0x80) {
                    if (!XMLChar.isName(ch)) {
                        break;
                    }
                } else {
                    byte chartype = fASCIICharMap[ch];
                    if (chartype != CHARTYPE_LETTER
                            && chartype != CHARTYPE_DIGIT
                            && chartype != CHARTYPE_PERIOD
                            && chartype != CHARTYPE_MINUS
                            && chartype != CHARTYPE_UNDERSCORE) {
                        break;
                    }
                }
            }
            return currentOffset;
        }
        private int scanData(String data, StringBuffer schemeData,
                int endOffset, int currentOffset) {
            while (true) {
                if (currentOffset == endOffset) {
                    break;
                }
                int ch = data.charAt(currentOffset);
                byte chartype = (ch >= 0x80) ? CHARTYPE_NONASCII
                        : fASCIICharMap[ch];
                if (chartype == CHARTYPE_OPEN_PAREN) {
                    schemeData.append(ch);
                    currentOffset = scanData(data, schemeData, endOffset,
                            ++currentOffset);
                    if (currentOffset == endOffset) {
                        return currentOffset;
                    }
                    ch = data.charAt(currentOffset);
                    chartype = (ch >= 0x80) ? CHARTYPE_NONASCII
                            : fASCIICharMap[ch];
                    if (chartype != CHARTYPE_CLOSE_PAREN) {
                        return endOffset;
                    }
                    schemeData.append((char) ch);
                    ++currentOffset;
                } else if (chartype == CHARTYPE_CLOSE_PAREN) {
                    return currentOffset;
                } else  if (chartype == CHARTYPE_CARRET) {
                    ch = data.charAt(++currentOffset);
                    chartype = (ch >= 0x80) ? CHARTYPE_NONASCII
                            : fASCIICharMap[ch];
                    if (chartype != CHARTYPE_CARRET
                            && chartype != CHARTYPE_OPEN_PAREN
                            && chartype != CHARTYPE_CLOSE_PAREN) {
                        break;
                    }
                    schemeData.append((char) ch);
                    ++currentOffset;
                } else {
                    schemeData.append((char) ch);
                    ++currentOffset;
                }
            }
            return currentOffset;
        }
        protected void addToken(Tokens tokens, int token) throws XNIException {
            tokens.addToken(token);
        } 
    } 
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.comment(text, augs);
    }
    public void processingInstruction(String target, XMLString data,
            Augmentations augs) throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.processingInstruction(target, data, augs);
    }
    public void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        if (!resolveXPointer(element, attributes, augs,
                XPointerPart.EVENT_ELEMENT_START)) {
        	if (fFixupBase) {
                processXMLBaseAttributes(attributes);
        	}
            if (fFixupLang) {
                processXMLLangAttributes(attributes);
            }
            fNamespaceContext.setContextInvalid(); 
            return;
        }
        super.startElement(element, attributes, augs);
    }
    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        if (!resolveXPointer(element, attributes, augs,
                XPointerPart.EVENT_ELEMENT_EMPTY)) {
        	if (fFixupBase) {
                processXMLBaseAttributes(attributes);
        	}    
            if (fFixupLang) {
                processXMLLangAttributes(attributes);
            }
            fNamespaceContext.setContextInvalid(); 
            return;
        }
        super.emptyElement(element, attributes, augs);
    }
    public void characters(XMLString text, Augmentations augs)
            throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.characters(text, augs);
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.ignorableWhitespace(text, augs);
    }
    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        if (!resolveXPointer(element, null, augs,
                XPointerPart.EVENT_ELEMENT_END)) {
            return;
        }
        super.endElement(element, augs);
    }
    public void startCDATA(Augmentations augs) throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.startCDATA(augs);
    }
    public void endCDATA(Augmentations augs) throws XNIException {
        if (!isChildFragmentResolved()) {
            return;
        }
        super.endCDATA(augs);
    }
    public void setProperty(String propertyId, Object value)
            throws XMLConfigurationException {
        if (propertyId == Constants.XERCES_PROPERTY_PREFIX
                + Constants.ERROR_REPORTER_PROPERTY) {
            if (value != null) {
                fXPointerErrorReporter = (XMLErrorReporter) value;
            } else {
                fXPointerErrorReporter = null;
            }
        }
        if (propertyId == Constants.XERCES_PROPERTY_PREFIX
                + Constants.ERROR_HANDLER_PROPERTY) {
            if (value != null) {
                fErrorHandler = (XMLErrorHandler) value;
            } else {
                fErrorHandler = null;
            }
        }
        if (propertyId == Constants.XERCES_FEATURE_PREFIX
                + Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE) {
            if (value != null) {
            	fFixupLang = ((Boolean)value).booleanValue();
            } else {
            	fFixupLang = false;
            }
        }
        if (propertyId == Constants.XERCES_FEATURE_PREFIX
                + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE) {
            if (value != null) {
            	fFixupBase = ((Boolean)value).booleanValue();
            } else {
            	fFixupBase = false;
            }
        }
        if (propertyId == Constants.XERCES_PROPERTY_PREFIX
                + Constants.NAMESPACE_CONTEXT_PROPERTY) {
            fNamespaceContext = (XIncludeNamespaceSupport) value;
        }
        super.setProperty(propertyId, value);
    }
}