package org.apache.batik.util.gui.xmleditor;
import org.apache.batik.xml.XMLUtilities;
public class XMLScanner {
    public static final int TEMP_ERROR_CONTEXT = -2;
    public static final int EOF_CONTEXT = -1;
    public static final int DEFAULT_CONTEXT = 0;
    public static final int COMMENT_CONTEXT = 1;
    public static final int ELEMENT_CONTEXT = 2;
    public static final int CHARACTER_DATA_CONTEXT = 3;
    public static final int ATTRIBUTE_NAME_CONTEXT = 4;
    public static final int ATTRIBUTE_VALUE_CONTEXT = 5;
    public static final int XML_DECLARATION_CONTEXT = 6;
    public static final int DOCTYPE_CONTEXT = 7;
    public static final int ENTITY_CONTEXT = 8;
    public static final int ELEMENT_DECLARATION_CONTEXT = 9;
    public static final int CDATA_CONTEXT = 10;
    public static final int PI_CONTEXT = 11;
    private int position;
    private String string;
    private int current;
    private int scanValue;
    private int startOffset;
    public XMLScanner() {
        reset();
    }
    public void reset() {
        position = 0;
        startOffset = 0;
    }
    public void setString(String string) {
        this.string = string;
    }
    protected int nextChar() {
        try {
            current = (int)string.charAt(position);
            position++;
        } catch (Exception ex) {
            current = -1;
        }
        return current;
    }
    protected int skipSpaces() {
        do {
            nextChar();
        } while (current != -1 && XMLUtilities.isXMLSpace((char)current));
        return current;
    }
    public int getScanValue() {
        return scanValue;
    }
    public int getStartOffset() {
        return startOffset;
    }
    public int scan(int context) {
        nextChar();
        switch(context) {
            case XML_DECLARATION_CONTEXT:
                scanValue = scanXMLDeclaration();
                break;
            case DOCTYPE_CONTEXT:
                scanValue = scanDOCTYPE();
                break;
            case COMMENT_CONTEXT:
                scanValue = scanComment();
                break;
            case ELEMENT_CONTEXT:
                scanValue = scanElement();
                break;
            case ATTRIBUTE_NAME_CONTEXT:
                scanValue = scanAttributeName();
                break;
            case ATTRIBUTE_VALUE_CONTEXT:
                scanValue = scanAttributeValue();
                break;
            case CDATA_CONTEXT:
                scanValue = scanCDATA();
                break;
            default:
                scanValue = scanCharacterData();
                break;
        }
        return position;
    }
    private int scanCharacterData() {
        while (current != -1) {
            if (current == '<') {
                nextChar();
                if (current == '?') {
                    position = position - 2;
                    return XML_DECLARATION_CONTEXT;
                } else if (current == '!') {
                    nextChar();
                    if (current == 'D') {
                        position = position - 3;
                        return DOCTYPE_CONTEXT;
                    } else if (current == '-') {
                        nextChar();
                        if (current == '-') {
                            position = position - 4;
                            return COMMENT_CONTEXT;
                        }
                    } else if (current == '[') {
                        if (nextChar() == 'C') {
                            if (nextChar() == 'D') {
                                if (nextChar() == 'A') {
                                    if (nextChar() == 'T') {
                                        if (nextChar() == 'A') {
                                            if (nextChar() == '[') {
                                                position = position - 9;
                                                return CDATA_CONTEXT;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    position = position - 2;
                    return ELEMENT_CONTEXT;
                }
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return CHARACTER_DATA_CONTEXT;
    }
    private int scanXMLDeclaration() {
        position = position + 2;    
        while(current != -1) {
            if (current == '?') {
                if (nextChar() == '>') {
                    return CHARACTER_DATA_CONTEXT;
                } else {
                    return TEMP_ERROR_CONTEXT;
                }
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return XML_DECLARATION_CONTEXT;
    }
    private int scanDOCTYPE() {
        position = position + 3;    
        boolean end = true;
        while(current != -1) {
            if (current == '[') {
                end = false;
            } else if (current == ']') {
                end = true;
            } else if (current == '>' && end == true) {
                return CHARACTER_DATA_CONTEXT;
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return DOCTYPE_CONTEXT;
    }
    private int scanComment() {
        while(current != -1) {
            if (current == '-') {
                if (nextChar() == '-') {
                    if (nextChar() == '>') {
                        return CHARACTER_DATA_CONTEXT;
                    }
                }
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return COMMENT_CONTEXT;
    }
    private int scanElement() {
        while (current != -1) {
            if (current == '>') {
                return CHARACTER_DATA_CONTEXT;
            } else if (XMLUtilities.isXMLSpace((char)current)) {
                return ATTRIBUTE_NAME_CONTEXT;
            } 
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return ELEMENT_CONTEXT;
    }
    private int scanAttributeName() {
        while (current != -1) {
            if (current == '=') {
                return ATTRIBUTE_VALUE_CONTEXT;
            } else if (current == '/') {
                position--;
                return ELEMENT_CONTEXT;
            } else if (current == '>') {
                position--;
                return ELEMENT_CONTEXT;
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return ATTRIBUTE_NAME_CONTEXT;
    }
    private int scanAttributeValue() {
        int delim = '"';
        while (current != -1) {
            if ((current == '"') || (current == '\'')) {
                delim = current;
                break;
            }
            nextChar();
        }
        nextChar();
        while (current != -1) {
            if (current == delim) {
                return ELEMENT_CONTEXT;
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return ATTRIBUTE_VALUE_CONTEXT;
    }
    private int scanCDATA() {
        while(current != -1) {
            if (current == ']') {
                if (nextChar() == ']') {
                    if (nextChar() == '>') {
                        return CHARACTER_DATA_CONTEXT;
                    }
                }
            }
            nextChar();
        }
        if (current == -1) {
            return EOF_CONTEXT;
        }
        return CDATA_CONTEXT;
    }
}
