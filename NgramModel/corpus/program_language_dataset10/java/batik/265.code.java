package org.apache.batik.bridge.svg12;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.apache.batik.parser.AbstractScanner;
import org.apache.batik.parser.ParseException;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XPathSubsetContentSelector extends AbstractContentSelector {
    protected static final int SELECTOR_INVALID = -1;
    protected static final int SELECTOR_ANY = 0;
    protected static final int SELECTOR_QNAME = 1;
    protected static final int SELECTOR_ID = 2;
    protected int selectorType;
    protected String prefix;
    protected String localName;
    protected int index;
    protected SelectedNodes selectedContent;
    public XPathSubsetContentSelector(ContentManager cm,
                                      XBLOMContentElement content,
                                      Element bound,
                                      String selector) {
        super(cm, content, bound);
        parseSelector(selector);
    }
    protected void parseSelector(String selector) {
        selectorType = SELECTOR_INVALID;
        Scanner scanner = new Scanner(selector);
        int token = scanner.next();
        if (token == Scanner.NAME) {
            String name1 = scanner.getStringValue();
            token = scanner.next();
            if (token == Scanner.EOF) {
                selectorType = SELECTOR_QNAME;
                prefix = null;
                localName = name1;
                index = 0;
                return;
            } else if (token == Scanner.COLON) {
                token = scanner.next();
                if (token == Scanner.NAME) {
                    String name2 = scanner.getStringValue();
                    token = scanner.next();
                    if (token == Scanner.EOF) {
                        selectorType = SELECTOR_QNAME;
                        prefix = name1;
                        localName = name2;
                        index = 0;
                        return;
                    } else if (token == Scanner.LEFT_SQUARE_BRACKET) {
                        token = scanner.next();
                        if (token == Scanner.NUMBER) {
                            int number = Integer.parseInt(scanner.getStringValue());
                            token = scanner.next();
                            if (token == Scanner.RIGHT_SQUARE_BRACKET) {
                                token = scanner.next();
                                if (token == Scanner.EOF) {
                                    selectorType = SELECTOR_QNAME;
                                    prefix = name1;
                                    localName = name2;
                                    index = number;
                                    return;
                                }
                            }
                        }
                    }
                } else if (token == Scanner.LEFT_SQUARE_BRACKET) {
                    token = scanner.next();
                    if (token == Scanner.NUMBER) {
                        int number = Integer.parseInt(scanner.getStringValue());
                        token = scanner.next();
                        if (token == Scanner.RIGHT_SQUARE_BRACKET) {
                            token = scanner.next();
                            if (token == Scanner.EOF) {
                                selectorType = SELECTOR_QNAME;
                                prefix = null;
                                localName = name1;
                                index = number;
                                return;
                            }
                        }
                    }
                } else if (token == Scanner.LEFT_PARENTHESIS) {
                    if (name1.equals("id")) {
                        token = scanner.next();
                        if (token == Scanner.STRING) {
                            String id = scanner.getStringValue();
                            token = scanner.next();
                            if (token == Scanner.RIGHT_PARENTHESIS) {
                                token = scanner.next();
                                if (token == Scanner.EOF) {
                                    selectorType = SELECTOR_ID;
                                    localName = id;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else if (token == Scanner.ASTERISK) {
            token = scanner.next();
            if (token == Scanner.EOF) {
                selectorType = SELECTOR_ANY;
                return;
            } else if (token == Scanner.LEFT_SQUARE_BRACKET) {
                token = scanner.next();
                if (token == Scanner.NUMBER) {
                    int number = Integer.parseInt(scanner.getStringValue());
                    token = scanner.next();
                    if (token == Scanner.RIGHT_SQUARE_BRACKET) {
                        token = scanner.next();
                        if (token == Scanner.EOF) {
                            selectorType = SELECTOR_ANY;
                            index = number;
                            return;
                        }
                    }
                }
            }
        }
    }
    public NodeList getSelectedContent() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
        }
        return selectedContent;
    }
    boolean update() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
            return true;
        }
        return selectedContent.update();
    }
    protected class SelectedNodes implements NodeList {
        protected ArrayList nodes = new ArrayList(10);
        public SelectedNodes() {
            update();
        }
        protected boolean update() {
            ArrayList oldNodes = (ArrayList) nodes.clone();
            nodes.clear();
            int nth = 0;
            for (Node n = boundElement.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element) n;
                boolean matched = selectorType == SELECTOR_ANY;
                switch (selectorType) {
                    case SELECTOR_ID:
                        matched = e.getAttributeNS(null, "id").equals(localName);
                        break;
                    case SELECTOR_QNAME:
                        if (prefix == null) {
                            matched = e.getNamespaceURI() == null;
                        } else {
                            String ns = contentElement.lookupNamespaceURI(prefix);
                            if (ns == null) {
                            } else {
                                matched = e.getNamespaceURI().equals(ns);
                            }
                        }
                        matched = matched && localName.equals(e.getLocalName());
                        break;
                }
                if (selectorType == SELECTOR_ANY
                        || selectorType == SELECTOR_QNAME) {
                    matched = matched && (index == 0 || ++nth == index);
                }
                if (matched && !isSelected(n)) {
                    nodes.add(e);
                }
            }
            int nodesSize = nodes.size();
            if (oldNodes.size() != nodesSize) {
                return true;
            }
            for (int i = 0; i < nodesSize; i++) {
                if (oldNodes.get(i) != nodes.get(i)) {
                    return true;
                }
            }
            return false;
        }
        public Node item(int index) {
            if (index < 0 || index >= nodes.size()) {
                return null;
            }
            return (Node) nodes.get(index);
        }
        public int getLength() {
            return nodes.size();
        }
    }
    protected static class Scanner extends AbstractScanner {
        public static final int EOF = 0;
        public static final int NAME = 1;
        public static final int COLON = 2;
        public static final int LEFT_SQUARE_BRACKET = 3;
        public static final int RIGHT_SQUARE_BRACKET = 4;
        public static final int LEFT_PARENTHESIS = 5;
        public static final int RIGHT_PARENTHESIS = 6;
        public static final int STRING = 7;
        public static final int NUMBER = 8;
        public static final int ASTERISK = 9;
        public Scanner(String s) {
            super(s);
        }
        protected int endGap() {
            return (current == -1) ? 0 : 1;
        }
        protected void nextToken() throws ParseException {
            try {
                switch (current) {
                    case -1:
                        type = EOF;
                        return;
                    case ':':
                        nextChar();
                        type = COLON;
                        return;
                    case '[':
                        nextChar();
                        type = LEFT_SQUARE_BRACKET;
                        return;
                    case ']':
                        nextChar();
                        type = RIGHT_SQUARE_BRACKET;
                        return;
                    case '(':
                        nextChar();
                        type = LEFT_PARENTHESIS;
                        return;
                    case ')':
                        nextChar();
                        type = RIGHT_PARENTHESIS;
                        return;
                    case '*':
                        nextChar();
                        type = ASTERISK;
                        return;
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                    case '\f':
                        do {
                            nextChar();
                        } while (XMLUtilities.isXMLSpace((char) current));
                        nextToken();
                        return;
                    case '\'':
                        type = string1();
                        return;
                    case '"':
                        type = string2();
                        return;
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        type = number();
                        return;
                    default:
                        if (XMLUtilities.isXMLNameFirstCharacter((char) current)) {
                            do {
                                nextChar();
                            } while (current != -1
                                     && current != ':'
                                     && XMLUtilities.isXMLNameCharacter((char) current));
                            type = NAME;
                            return;
                        }
                        nextChar();
                        throw new ParseException("identifier.character",
                                                 reader.getLine(),
                                                 reader.getColumn());
                }
            } catch (IOException e) {
                throw new ParseException(e);
            }
        }
        protected int string1() throws IOException {
            start = position;
            loop: for (;;) {
                switch (nextChar()) {
                case -1:
                    throw new ParseException("eof",
                                             reader.getLine(),
                                             reader.getColumn());
                case '\'':
                    break loop;
                }
            }
            nextChar();
            return STRING;
        }
        protected int string2() throws IOException {
            start = position;
            loop: for (;;) {
                switch (nextChar()) {
                case -1:
                    throw new ParseException("eof",
                                             reader.getLine(),
                                             reader.getColumn());
                case '"':
                    break loop;
                }
            }
            nextChar();
            return STRING;
        }
        protected int number() throws IOException {
            loop: for (;;) {
                switch (nextChar()) {
                case '.':
                    switch (nextChar()) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        return dotNumber();
                    }
                    throw new ParseException("character",
                                             reader.getLine(),
                                             reader.getColumn());
                default:
                    break loop;
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                }
            }
            return NUMBER;
        }        
        protected int dotNumber() throws IOException {
            loop: for (;;) {
                switch (nextChar()) {
                default:
                    break loop;
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                }
            }
            return NUMBER;
        }
    }
}
