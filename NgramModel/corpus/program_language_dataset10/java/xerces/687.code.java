package org.apache.xerces.xpointer;
import java.util.HashMap;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
final class ElementSchemePointer implements XPointerPart {
    private String fSchemeName;
    private String fSchemeData;
    private String fShortHandPointerName;
    private boolean fIsResolveElement = false;
    private boolean fIsElementFound = false;
    private boolean fWasOnlyEmptyElementFound = false;
    boolean fIsShortHand = false;
    int fFoundDepth = 0;
    private int fChildSequence[];
    private int fCurrentChildPosition = 1;
    private int fCurrentChildDepth = 0;
    private int fCurrentChildSequence[];;
    private boolean fIsFragmentResolved = false;
    private ShortHandPointer fShortHandPointer;
    protected XMLErrorReporter fErrorReporter;
    protected XMLErrorHandler fErrorHandler;
    private SymbolTable fSymbolTable;
    public ElementSchemePointer() {
    }
    public ElementSchemePointer(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    }
    public ElementSchemePointer(SymbolTable symbolTable,
            XMLErrorReporter errorReporter) {
        fSymbolTable = symbolTable;
        fErrorReporter = errorReporter;
    }
    public void parseXPointer(String xpointer) throws XNIException {
        init();
        final Tokens tokens = new Tokens(fSymbolTable);
        Scanner scanner = new Scanner(fSymbolTable) {
            protected void addToken(Tokens tokens, int token)
                    throws XNIException {
                if (token == Tokens.XPTRTOKEN_ELEM_CHILD
                        || token == Tokens.XPTRTOKEN_ELEM_NCNAME) {
                    super.addToken(tokens, token);
                    return;
                }
                reportError("InvalidElementSchemeToken", new Object[] { tokens
                        .getTokenString(token) });
            }
        };
        int length = xpointer.length();
        boolean success = scanner.scanExpr(fSymbolTable, tokens, xpointer, 0,
                length);
        if (!success) {
            reportError("InvalidElementSchemeXPointer",
                    new Object[] { xpointer });
        }    
        int tmpChildSequence[] = new int[tokens.getTokenCount() / 2 + 1];
        int i = 0;
        while (tokens.hasMore()) {
            int token = tokens.nextToken();
            switch (token) {
            case Tokens.XPTRTOKEN_ELEM_NCNAME: {
                token = tokens.nextToken();
                fShortHandPointerName = tokens.getTokenString(token);
                fShortHandPointer = new ShortHandPointer(fSymbolTable);
                fShortHandPointer.setSchemeName(fShortHandPointerName);
                break;
            }
            case Tokens.XPTRTOKEN_ELEM_CHILD: {
                tmpChildSequence[i] = tokens.nextToken();
                i++;
                break;
            }
            default:
                reportError("InvalidElementSchemeXPointer",
                        new Object[] { xpointer });
            }
        }
        fChildSequence = new int[i];
        fCurrentChildSequence = new int[i];
        System.arraycopy(tmpChildSequence, 0, fChildSequence, 0, i);
    }
    public String getSchemeName() {
        return fSchemeName;
    }
    public String getSchemeData() {
        return fSchemeData;
    }
    public void setSchemeName(String schemeName) {
        fSchemeName = schemeName;
    }
    public void setSchemeData(String schemeData) {
        fSchemeData = schemeData;
    }
    public boolean resolveXPointer(QName element, XMLAttributes attributes,
            Augmentations augs, int event) throws XNIException {
        boolean isShortHandPointerResolved = false;
        if (fShortHandPointerName != null) {
            isShortHandPointerResolved = fShortHandPointer.resolveXPointer(
                    element, attributes, augs, event);
            if (isShortHandPointerResolved) {
                fIsResolveElement = true;
                fIsShortHand = true;
            } else {
                fIsResolveElement = false;
            }
        } else {
            fIsResolveElement = true;
        }
        if (fChildSequence.length > 0) {
            fIsFragmentResolved = matchChildSequence(element, event);
        } else if (isShortHandPointerResolved && fChildSequence.length <= 0) {
            fIsFragmentResolved = isShortHandPointerResolved;
        } else {
            fIsFragmentResolved = false;
        }
        return fIsFragmentResolved;
    }
    protected boolean matchChildSequence(QName element, int event)
            throws XNIException {
        if (fCurrentChildDepth >= fCurrentChildSequence.length) {
            int tmpCurrentChildSequence[] = new int[fCurrentChildSequence.length];
            System.arraycopy(fCurrentChildSequence, 0, tmpCurrentChildSequence,
                    0, fCurrentChildSequence.length);
            fCurrentChildSequence = new int[fCurrentChildDepth * 2];
            System.arraycopy(tmpCurrentChildSequence, 0, fCurrentChildSequence,
                    0, tmpCurrentChildSequence.length);
        }
        if (fIsResolveElement) {
            if (event == XPointerPart.EVENT_ELEMENT_START) {
                fCurrentChildSequence[fCurrentChildDepth] = fCurrentChildPosition;
                fCurrentChildDepth++;
                fCurrentChildPosition = 1;
                if ((fCurrentChildDepth <= fFoundDepth) || (fFoundDepth == 0)) {
                    if (checkMatch()) {
                        fIsElementFound = true;
                        fFoundDepth = fCurrentChildDepth;
                    } else {
                        fIsElementFound = false;
                        fFoundDepth = 0;
                    }
                }
            } else if (event == XPointerPart.EVENT_ELEMENT_END) {
                if (fCurrentChildDepth == fFoundDepth) {
                    fIsElementFound = true;
                } else if (((fCurrentChildDepth < fFoundDepth) && (fFoundDepth != 0))
                        || ((fCurrentChildDepth > fFoundDepth) 
                        && (fFoundDepth == 0))) {
                    fIsElementFound = false;
                }
                fCurrentChildSequence[fCurrentChildDepth] = 0;
                fCurrentChildDepth--;
                fCurrentChildPosition = fCurrentChildSequence[fCurrentChildDepth] + 1;
            } else if (event == XPointerPart.EVENT_ELEMENT_EMPTY) {
                fCurrentChildSequence[fCurrentChildDepth] = fCurrentChildPosition;
                fCurrentChildPosition++;
                if (checkMatch()) {
                    if (!fIsElementFound) {
                        fWasOnlyEmptyElementFound = true;
                    } else {
                        fWasOnlyEmptyElementFound = false;
                    }
                    fIsElementFound = true;
                } else {
                    fIsElementFound = false;
                    fWasOnlyEmptyElementFound = false;
                }  
            }
        }
        return fIsElementFound;
    }
    protected boolean checkMatch() {
        if (!fIsShortHand) {
            if (fChildSequence.length <= fCurrentChildDepth + 1) {
                for (int i = 0; i < fChildSequence.length; i++) {
                    if (fChildSequence[i] != fCurrentChildSequence[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            if (fChildSequence.length <= fCurrentChildDepth + 1) {
                for (int i = 0; i < fChildSequence.length; i++) {
                    if (fCurrentChildSequence.length < i + 2) {
                        return false;
                    }
                    if (fChildSequence[i] != fCurrentChildSequence[i + 1]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }
    public boolean isFragmentResolved() throws XNIException {
        return fIsFragmentResolved ;
    }
    public boolean isChildFragmentResolved() {
    	if (fIsShortHand && fShortHandPointer != null && fChildSequence.length <= 0) {
    		return fShortHandPointer.isChildFragmentResolved();
    	} else {
    		return fWasOnlyEmptyElementFound ? !fWasOnlyEmptyElementFound
    				: (fIsFragmentResolved && (fCurrentChildDepth >= fFoundDepth));
    	}
    }
    protected void reportError(String key, Object[] arguments)
            throws XNIException {
    	throw new XNIException((fErrorReporter
    			.getMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN))
				.formatMessage(fErrorReporter.getLocale(), key, arguments));
    }
    protected void initErrorReporter() {
        if (fErrorReporter == null) {
            fErrorReporter = new XMLErrorReporter();
        }
        if (fErrorHandler == null) {
            fErrorHandler = new XPointerErrorHandler();
        }
        fErrorReporter.putMessageFormatter(
                XPointerMessageFormatter.XPOINTER_DOMAIN,
                new XPointerMessageFormatter());
    }
    protected void init() {
        fSchemeName = null;
        fSchemeData = null;
        fShortHandPointerName = null;
        fIsResolveElement = false;
        fIsElementFound = false;
        fWasOnlyEmptyElementFound = false;
        fFoundDepth = 0;
        fCurrentChildPosition = 1;
        fCurrentChildDepth = 0;
        fIsFragmentResolved = false;
        fShortHandPointer = null;
        initErrorReporter();
    }
    private final class Tokens {
        private static final int XPTRTOKEN_ELEM_NCNAME = 0;
        private static final int XPTRTOKEN_ELEM_CHILD = 1;
        private final String[] fgTokenNames = { "XPTRTOKEN_ELEM_NCNAME",
                "XPTRTOKEN_ELEM_CHILD" };
        private static final int INITIAL_TOKEN_COUNT = 1 << 8;
        private int[] fTokens = new int[INITIAL_TOKEN_COUNT];
        private int fTokenCount = 0;
        private int fCurrentTokenIndex;
        private SymbolTable fSymbolTable;
        private HashMap fTokenNames = new HashMap();
        private Tokens(SymbolTable symbolTable) {
            fSymbolTable = symbolTable;
            fTokenNames.put(new Integer(XPTRTOKEN_ELEM_NCNAME),
                    "XPTRTOKEN_ELEM_NCNAME");
            fTokenNames.put(new Integer(XPTRTOKEN_ELEM_CHILD),
                    "XPTRTOKEN_ELEM_CHILD");
        }
        private String getTokenString(int token) {
            return (String) fTokenNames.get(new Integer(token));
        }
        private Integer getToken(int token) {
            return (Integer) fTokenNames.get(new Integer(token));
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
            if (fCurrentTokenIndex == fTokenCount)
                reportError("XPointerElementSchemeProcessingError", null);
            return fTokens[fCurrentTokenIndex++];
        }
        private int peekToken() throws XNIException {
            if (fCurrentTokenIndex == fTokenCount)
                reportError("XPointerElementSchemeProcessingError", null);
            return fTokens[fCurrentTokenIndex];
        }
        private String nextTokenAsString() throws XNIException {
            String s = getTokenString(nextToken());
            if (s == null)
                reportError("XPointerElementSchemeProcessingError", null);
            return s;
        }
        private int getTokenCount() {
            return fTokenCount;
        }
    }
    private class Scanner {
        private static final byte CHARTYPE_INVALID = 0, 
                CHARTYPE_OTHER = 1, 
                CHARTYPE_MINUS = 2, 
                CHARTYPE_PERIOD = 3, 
                CHARTYPE_SLASH = 4, 
                CHARTYPE_DIGIT = 5, 
                CHARTYPE_LETTER = 6, 
                CHARTYPE_UNDERSCORE = 7, 
                CHARTYPE_NONASCII = 8; 
        private final byte[] fASCIICharMap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
                0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 4, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6,
                6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 1, 1, 1,
                7, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
                6, 6, 6, 6, 6, 6, 6, 1, 1, 1, 1, 1 };
        private SymbolTable fSymbolTable;
        private Scanner(SymbolTable symbolTable) {
            fSymbolTable = symbolTable;
        } 
        private boolean scanExpr(SymbolTable symbolTable, Tokens tokens,
                String data, int currentOffset, int endOffset)
                throws XNIException {
            int ch;
            int nameOffset;
            String nameHandle = null;
            while (true) {
                if (currentOffset == endOffset) {
                    break;
                }
                ch = data.charAt(currentOffset);
                byte chartype = (ch >= 0x80) ? CHARTYPE_NONASCII
                        : fASCIICharMap[ch];
                switch (chartype) {
                case CHARTYPE_SLASH:
                    if (++currentOffset == endOffset) {
                        return false;
                    }
                    addToken(tokens, Tokens.XPTRTOKEN_ELEM_CHILD);
                    ch = data.charAt(currentOffset);
                    int child = 0;
                    while (ch >= '0' && ch <= '9') {
                        child = (child * 10) + (ch - '0');
                        if (++currentOffset == endOffset) {
                            break;
                        }
                        ch = data.charAt(currentOffset);
                    }
                    if (child == 0) {
                        reportError("InvalidChildSequenceCharacter",
                                new Object[] { new Character((char) ch) });
                        return false;
                    }
                    tokens.addToken(child);
                    break;
                case CHARTYPE_DIGIT:
                case CHARTYPE_LETTER:
                case CHARTYPE_MINUS:
                case CHARTYPE_NONASCII:
                case CHARTYPE_OTHER:
                case CHARTYPE_PERIOD:
                case CHARTYPE_UNDERSCORE:
                    nameOffset = currentOffset;
                    currentOffset = scanNCName(data, endOffset, currentOffset);
                    if (currentOffset == nameOffset) {
                        reportError("InvalidNCNameInElementSchemeData",
                                new Object[] { data });
                        return false;
                    }
                    if (currentOffset < endOffset) {
                        ch = data.charAt(currentOffset);
                    } else {
                        ch = -1;
                    }
                    nameHandle = symbolTable.addSymbol(data.substring(
                            nameOffset, currentOffset));
                    addToken(tokens, Tokens.XPTRTOKEN_ELEM_NCNAME);
                    tokens.addToken(nameHandle);
                    break;
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
        protected void addToken(Tokens tokens, int token) throws XNIException {
            tokens.addToken(token);
        } 
    } 
}