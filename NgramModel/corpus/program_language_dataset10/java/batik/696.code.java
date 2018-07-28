package org.apache.batik.dom.util;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class DOMUtilities extends XMLUtilities implements XMLConstants {
    protected DOMUtilities() {
    }
    private static class NSMap {
        private String prefix;
        private String ns;
        private NSMap next;
        private int nextPrefixNumber;
        public static NSMap create() {
            return new NSMap().declare(XMLConstants.XML_PREFIX,
                                       XMLConstants.XML_NAMESPACE_URI)
                              .declare(XMLConstants.XMLNS_PREFIX,
                                       XMLConstants.XMLNS_NAMESPACE_URI);
        }
        private NSMap() {
        }
        public NSMap declare(String prefix, String ns) {
            NSMap m = new NSMap();
            m.prefix = prefix;
            m.ns = ns;
            m.next = this;
            m.nextPrefixNumber = this.nextPrefixNumber;
            return m;
        }
        public String getNewPrefix() {
            String prefix;
            do {
                prefix = "a" + nextPrefixNumber++;
            } while (getNamespace(prefix) != null);
            return prefix;
        }
        public String getNamespace(String prefix) {
            for (NSMap m = this; m.next != null; m = m.next) {
                if (m.prefix.equals(prefix)) {
                    return m.ns;
                }
            }
            return null;
        }
        public String getPrefixForElement(String ns) {
            for (NSMap m = this; m.next != null; m = m.next) {
                if (ns.equals(m.ns)) {
                    return m.prefix;
                }
            }
            return null;
        }
        public String getPrefixForAttr(String ns) {
            for (NSMap m = this; m.next != null; m = m.next) {
                if (ns.equals(m.ns) && !m.prefix.equals("")) {
                    return m.prefix;
                }
            }
            return null;
        }
    }
    public static void writeDocument(Document doc, Writer w) throws IOException {
        AbstractDocument d = (AbstractDocument) doc;
        if (doc.getDocumentElement() == null) {
            throw new IOException("No document element");
        }
        NSMap m = NSMap.create();
        for (Node n = doc.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            writeNode(n, w, m, "1.1".equals(d.getXmlVersion()));
        }
    }
    protected static void writeNode(Node n, Writer w, NSMap m, boolean isXML11)
            throws IOException {
        switch (n.getNodeType()) {
        case Node.ELEMENT_NODE: {
            if (n.hasAttributes()) {
                NamedNodeMap attr = n.getAttributes();
                int len = attr.getLength();
                for (int i = 0; i < len; i++) {
                    Attr a = (Attr)attr.item(i);
                    String name = a.getNodeName();
                    if (name.startsWith("xmlns")) {
                        if (name.length() == 5) {
                            m = m.declare("", a.getNodeValue());
                        } else {
                            String prefix = name.substring(6);
                            m = m.declare(prefix, a.getNodeValue());
                        }
                    }
                }
            }
            w.write('<');
            String ns = n.getNamespaceURI();
            String tagName;
            if (ns == null) {
                tagName = n.getNodeName();
                w.write(tagName);
                if (!"".equals(m.getNamespace(""))) {
                    w.write(" xmlns=\"\"");
                    m = m.declare("", "");
                }
            } else {
                String prefix = n.getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
                if (ns.equals(m.getNamespace(prefix))) {
                    tagName = n.getNodeName();
                    w.write(tagName);
                } else {
                    prefix = m.getPrefixForElement(ns);
                    if (prefix == null) {
                        prefix = m.getNewPrefix();
                        tagName = prefix + ':' + n.getLocalName();
                        w.write(tagName + " xmlns:" + prefix + "=\""
                                 + contentToString(ns, isXML11) + '"');
                        m = m.declare(prefix, ns);
                    } else {
                        if (prefix.equals("")) {
                            tagName = n.getLocalName();
                        } else {
                            tagName = prefix + ':' + n.getLocalName();
                        }
                        w.write(tagName);
                    }
                }
            }
            if (n.hasAttributes()) {
                NamedNodeMap attr = n.getAttributes();
                int len = attr.getLength();
                for (int i = 0; i < len; i++) {
                    Attr a = (Attr)attr.item(i);
                    String name = a.getNodeName();
                    String prefix = a.getPrefix();
                    String ans = a.getNamespaceURI();
                    if (ans != null &&
                            !("xmlns".equals(prefix) || name.equals("xmlns"))) {
                        if (prefix != null
                                && !ans.equals(m.getNamespace(prefix))
                                || prefix == null) {
                            prefix = m.getPrefixForAttr(ans);
                            if (prefix == null) {
                                prefix = m.getNewPrefix();
                                m = m.declare(prefix, ans);
                                w.write(" xmlns:" + prefix + "=\""
                                         + contentToString(ans, isXML11) + '"');
                            }
                            name = prefix + ':' + a.getLocalName();
                        }
                    }
                    w.write(' ' + name + "=\""
                             + contentToString(a.getNodeValue(), isXML11)
                             + '"');
                }
            }
            Node c = n.getFirstChild();
            if (c != null) {
                w.write('>');
                do {
                    writeNode(c, w, m, isXML11);
                    c = c.getNextSibling();
                } while (c != null);
                w.write("</" + tagName + '>');
            } else {
                w.write("/>");
            }
            break;
        }
        case Node.TEXT_NODE:
            w.write(contentToString(n.getNodeValue(), isXML11));
            break;
        case Node.CDATA_SECTION_NODE: {
            String data = n.getNodeValue();
            if (data.indexOf("]]>") != -1) {
                throw new IOException("Unserializable CDATA section node");
            }
            w.write("<![CDATA["
                     + assertValidCharacters(data, isXML11)
                     + "]]>");
            break;
        }
        case Node.ENTITY_REFERENCE_NODE:
            w.write('&' + n.getNodeName() + ';');
            break;
        case Node.PROCESSING_INSTRUCTION_NODE: {
            String target = n.getNodeName();
            String data = n.getNodeValue();
            if (target.equalsIgnoreCase("xml")
                    || target.indexOf(':') != -1
                    || data.indexOf("?>") != -1) {
                throw new
                    IOException("Unserializable processing instruction node");
            }
            w.write("<?" + target + ' ' + data + "?>");
            break;
        }
        case Node.COMMENT_NODE: {
            w.write("<!--");
            String data = n.getNodeValue();
            int len = data.length();
            if (len != 0 && data.charAt(len - 1) == '-'
                    || data.indexOf("--") != -1) {
                throw new IOException("Unserializable comment node");
            }
            w.write(data);
            w.write("-->");
            break;
        }
        case Node.DOCUMENT_TYPE_NODE: {
            DocumentType dt = (DocumentType)n;
            w.write("<!DOCTYPE "
                     + n.getOwnerDocument().getDocumentElement().getNodeName());
            String pubID = dt.getPublicId();
            if (pubID != null) {
                char q = getUsableQuote(pubID);
                if (q == 0) {
                    throw new IOException("Unserializable DOCTYPE node");
                }
                w.write(" PUBLIC " + q + pubID + q);
            }
            String sysID = dt.getSystemId();
            if (sysID != null) {
                char q = getUsableQuote(sysID);
                if (q == 0) {
                    throw new IOException("Unserializable DOCTYPE node");
                }
                if (pubID == null) {
                    w.write(" SYSTEM");
                }
                w.write(" " + q + sysID + q);
            }
            String subset = dt.getInternalSubset();
            if (subset != null) {
                w.write('[' + subset + ']');
            }
            w.write('>');
            break;
        }
        default:
            throw new IOException("Unknown DOM node type " + n.getNodeType());
        }
    }
    public static void writeNode(Node n, Writer w) throws IOException {
        if (n.getNodeType() == Node.DOCUMENT_NODE) {
            writeDocument((Document) n, w);
        } else {
            AbstractDocument d = (AbstractDocument) n.getOwnerDocument();
            writeNode(n, w, NSMap.create(),
                      d == null ? false : "1.1".equals(d.getXmlVersion()));
        }
    }
    private static char getUsableQuote(String s) {
        char ret = 0;
        int i = s.length() - 1;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c == '"') {
                if (ret == 0) {
                    ret = '\'';
                } else {
                    return 0;
                }
            } else if (c == '\'') {
                if (ret == 0) {
                    ret = '"';
                } else {
                    return 0;
                }
            }
            i--;
        }
        return ret == 0 ? '"' : ret;
    }
    public static String getXML(Node n) {
        Writer writer = new StringWriter();
        try {
            DOMUtilities.writeNode(n, writer);
            writer.close();
        } catch (IOException ex) {
            return "";
        }
        return writer.toString();
    }
    protected static String assertValidCharacters(String s, boolean isXML11)
            throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (!isXML11 && !isXMLCharacter(c)
                    || isXML11 && !isXML11Character(c)) {
                throw new IOException("Invalid character");
            }
        }
        return s;
    }
    public static String contentToString(String s, boolean isXML11)
            throws IOException {
        StringBuffer result = new StringBuffer(s.length());
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (!isXML11 && !isXMLCharacter(c)
                    || isXML11 && !isXML11Character(c)) {
                throw new IOException("Invalid character");
            }
            switch (c) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            case '\'':
                result.append("&apos;");
                break;
            default:
                result.append(c);
            }
        }
        return result.toString();
    }
    public static int getChildIndex(Node child, Node parent) {
        if (child == null || child.getParentNode() != parent
                || child.getParentNode() == null) {
            return -1;
        }
        return getChildIndex(child);
    }
    public static int getChildIndex(Node child) {
        NodeList children = child.getParentNode().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i);
            if (currentChild == child) {
                return i;
            }
        }
        return -1;
    }
    public static boolean isAnyNodeAncestorOf(ArrayList ancestorNodes, Node node) {
        int n = ancestorNodes.size();
        for (int i = 0; i < n; i++) {
            Node ancestor = (Node) ancestorNodes.get(i);
            if (isAncestorOf(ancestor, node)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isAncestorOf(Node node, Node descendant) {
        if (node == null || descendant == null) {
            return false;
        }
        for (Node currentNode = descendant.getParentNode(); currentNode != null; currentNode = currentNode
                .getParentNode()) {
            if (currentNode == node) {
                return true;
            }
        }
        return false;
    }
    public static boolean isParentOf(Node node, Node parentNode) {
        if (node == null || parentNode == null
                || node.getParentNode() != parentNode) {
            return false;
        }
        return true;
    }
    public static boolean canAppend(Node node, Node parentNode) {
        if (node == null || parentNode == null || node == parentNode
                || isAncestorOf(node, parentNode)) {
            return false;
        }
        return true;
    }
    public static boolean canAppendAny(ArrayList children, Node parentNode) {
        if (!canHaveChildren(parentNode)) {
            return false;
        }
        int n = children.size();
        for (int i = 0; i < n; i++) {
            Node child = (Node) children.get(i);
            if (canAppend(child, parentNode)) {
                return true;
            }
        }
        return false;
    }
    public static boolean canHaveChildren(Node parentNode) {
        if (parentNode == null) {
            return false;
        }
        switch (parentNode.getNodeType()) {
            case Node.DOCUMENT_NODE:
            case Node.TEXT_NODE:
            case Node.COMMENT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                return false;
            default:
                return true;
        }
    }
    public static Node parseXML(String text, Document doc, String uri,
            Map prefixes, String wrapperElementName,
            SAXDocumentFactory documentFactory) {
        String wrapperElementPrefix = "";
        String wrapperElementSuffix = "";
        if (wrapperElementName != null) {
            wrapperElementPrefix = "<" + wrapperElementName;
            if (prefixes != null) {
                wrapperElementPrefix += " ";
                Iterator iter = prefixes.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry e = (Map.Entry) iter.next();
                    String currentKey = (String) e.getKey();
                    String currentValue = (String) e.getValue();
                    wrapperElementPrefix += currentKey + "=\"" + currentValue
                            + "\" ";
                }
            }
            wrapperElementPrefix += ">";
            wrapperElementSuffix += "</" + wrapperElementName + '>';
        }
        if (wrapperElementPrefix.trim().length() == 0
                && wrapperElementSuffix.trim().length() == 0) {
            try {
                Document d = documentFactory.createDocument(uri,
                        new StringReader(text));
                if (doc == null) {
                    return d;
                }
                Node result = doc.createDocumentFragment();
                result
                        .appendChild(doc.importNode(d.getDocumentElement(),
                                true));
                return result;
            } catch (Exception ex) {
            }
        }
        StringBuffer sb = new StringBuffer(wrapperElementPrefix.length()
                + text.length() + wrapperElementSuffix.length());
        sb.append(wrapperElementPrefix);
        sb.append(text);
        sb.append(wrapperElementSuffix);
        String newText = sb.toString();
        try {
            Document d = documentFactory.createDocument(uri, new StringReader(
                    newText));
            if (doc == null) {
                return d;
            }
            for (Node node = d.getDocumentElement().getFirstChild(); node != null;
                    node = node.getNextSibling()) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    node = doc.importNode(node, true);
                    Node result = doc.createDocumentFragment();
                    result.appendChild(node);
                    return result;
                }
            }
        } catch (Exception exc) {
        }
        return null;
    }
    public static Document deepCloneDocument(Document doc, DOMImplementation impl) {
        Element root = doc.getDocumentElement();
        Document result = impl.createDocument(root.getNamespaceURI(),
                                              root.getNodeName(),
                                              null);
        Element rroot = result.getDocumentElement();
        boolean before = true;
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n == root) {
                before = false;
                if (root.hasAttributes()) {
                    NamedNodeMap attr = root.getAttributes();
                    int len = attr.getLength();
                    for (int i = 0; i < len; i++) {
                        rroot.setAttributeNode((Attr)result.importNode(attr.item(i),
                                                                       true));
                    }
                }
                for (Node c = root.getFirstChild();
                     c != null;
                     c = c.getNextSibling()) {
                    rroot.appendChild(result.importNode(c, true));
                }
            } else {
                if (n.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
                    if (before) {
                        result.insertBefore(result.importNode(n, true), rroot);
                    } else {
                        result.appendChild(result.importNode(n, true));
                    }
                }
            }
        }
        return result;
    }
    public static boolean isValidName(String s) {
        int len = s.length();
        if (len == 0) {
            return false;
        }
        char c = s.charAt(0);
        int d = c / 32;
        int m = c % 32;
        if ((NAME_FIRST_CHARACTER[d] & (1 << m)) == 0) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            c = s.charAt(i);
            d = c / 32;
            m = c % 32;
            if ((NAME_CHARACTER[d] & (1 << m)) == 0) {
                return false;
            }
        }
        return true;
    }
    public static boolean isValidName11(String s) {
        int len = s.length();
        if (len == 0) {
            return false;
        }
        char c = s.charAt(0);
        int d = c / 32;
        int m = c % 32;
        if ((NAME11_FIRST_CHARACTER[d] & (1 << m)) == 0) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            c = s.charAt(i);
            d = c / 32;
            m = c % 32;
            if ((NAME11_CHARACTER[d] & (1 << m)) == 0) {
                return false;
            }
        }
        return true;
    }
    public static boolean isValidPrefix(String s) {
        return s.indexOf(':') == -1;
    }
    public static String getPrefix(String s) {
        int i = s.indexOf(':');
        return (i == -1 || i == s.length()-1)
            ? null
            : s.substring(0, i);
    }
    public static String getLocalName(String s) {
        int i = s.indexOf(':');
        return (i == -1 || i == s.length()-1)
            ? s
            : s.substring(i + 1);
    }
    public static void parseStyleSheetPIData(String data, HashTable table) {
        char c;
        int i = 0;
        while (i < data.length()) {
            c = data.charAt(i);
            if (!XMLUtilities.isXMLSpace(c)) {
                break;
            }
            i++;
        }
        while (i < data.length()) {
            c = data.charAt(i);
            int d = c / 32;
            int m = c % 32;
            if ((NAME_FIRST_CHARACTER[d] & (1 << m)) == 0) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
                                       "Wrong name initial:  " + c);
            }
            StringBuffer ident = new StringBuffer();
            ident.append(c);
            while (++i < data.length()) {
                c = data.charAt(i);
                d = c / 32;
                m = c % 32;
                if ((NAME_CHARACTER[d] & (1 << m)) == 0) {
                    break;
                }
                ident.append(c);
            }
            if (i >= data.length()) {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Wrong xml-stylesheet data: " + data);
            }
            while (i < data.length()) {
                c = data.charAt(i);
                if (!XMLUtilities.isXMLSpace(c)) {
                    break;
                }
                i++;
            }
            if (i >= data.length()) {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Wrong xml-stylesheet data: " + data);
            }
            if (data.charAt(i) != '=') {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Wrong xml-stylesheet data: " + data);
            }
            i++;
            while (i < data.length()) {
                c = data.charAt(i);
                if (!XMLUtilities.isXMLSpace(c)) {
                    break;
                }
                i++;
            }
            if (i >= data.length()) {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Wrong xml-stylesheet data: " + data);
            }
            c = data.charAt(i);
            i++;
            StringBuffer value = new StringBuffer();
            if (c == '\'') {
                while (i < data.length()) {
                    c = data.charAt(i);
                    if (c == '\'') {
                        break;
                    }
                    value.append(c);
                    i++;
                }
                if (i >= data.length()) {
                    throw new DOMException(DOMException.SYNTAX_ERR,
                                           "Wrong xml-stylesheet data: " +
                                           data);
                }
            } else if (c == '"') {
                while (i < data.length()) {
                    c = data.charAt(i);
                    if (c == '"') {
                        break;
                    }
                    value.append(c);
                    i++;
                }
                if (i >= data.length()) {
                    throw new DOMException(DOMException.SYNTAX_ERR,
                                           "Wrong xml-stylesheet data: " +
                                           data);
                }
            } else {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Wrong xml-stylesheet data: " + data);
            }
            table.put(ident.toString().intern(), value.toString());
            i++;
            while (i < data.length()) {
                c = data.charAt(i);
                if (!XMLUtilities.isXMLSpace(c)) {
                    break;
                }
                i++;
            }
        }
    }
    protected static final String[] LOCK_STRINGS = {
        "",
        "CapsLock",
        "NumLock",
        "NumLock CapsLock",
        "Scroll",
        "Scroll CapsLock",
        "Scroll NumLock",
        "Scroll NumLock CapsLock",
        "KanaMode",
        "KanaMode CapsLock",
        "KanaMode NumLock",
        "KanaMode NumLock CapsLock",
        "KanaMode Scroll",
        "KanaMode Scroll CapsLock",
        "KanaMode Scroll NumLock",
        "KanaMode Scroll NumLock CapsLock"
    };
    protected static final String[] MODIFIER_STRINGS = {
        "",
        "Shift",
        "Control",
        "Control Shift",
        "Meta",
        "Meta Shift",
        "Control Meta",
        "Control Meta Shift",
        "Alt",
        "Alt Shift",
        "Alt Control",
        "Alt Control Shift",
        "Alt Meta",
        "Alt Meta Shift",
        "Alt Control Meta",
        "Alt Control Meta Shift",
        "AltGraph",
        "AltGraph Shift",
        "AltGraph Control",
        "AltGraph Control Shift",
        "AltGraph Meta",
        "AltGraph Meta Shift",
        "AltGraph Control Meta",
        "AltGraph Control Meta Shift",
        "Alt AltGraph",
        "Alt AltGraph Shift",
        "Alt AltGraph Control",
        "Alt AltGraph Control Shift",
        "Alt AltGraph Meta",
        "Alt AltGraph Meta Shift",
        "Alt AltGraph Control Meta",
        "Alt AltGraph Control Meta Shift"
    };
    public static String getModifiersList(int lockState, int modifiersEx) {
        if ((modifiersEx & (1 << 13)) != 0) {
            modifiersEx = 0x10 | ((modifiersEx >> 6) & 0x0f);
        } else {
            modifiersEx = (modifiersEx >> 6) & 0x0f;
        }
        String s = LOCK_STRINGS[lockState & 0x0f];
        if (s.length() != 0) {
            return s + ' ' + MODIFIER_STRINGS[modifiersEx];
        }
        return MODIFIER_STRINGS[modifiersEx];
    }
    public static boolean isAttributeSpecifiedNS(Element e,
                                                 String namespaceURI,
                                                 String localName) {
        Attr a = e.getAttributeNodeNS(namespaceURI, localName);
        return a != null && a.getSpecified();
    }
}
