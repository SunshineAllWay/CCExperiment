package org.apache.tools.ant.util;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
public class DOMElementWriter {
    private static final int HEX = 16;
    private static final String[] WS_ENTITIES = new String['\r' - '\t' + 1];
    static {
        for (int i = '\t'; i < '\r' + 1; i++) {
            WS_ENTITIES[i - '\t'] = "&#x" + Integer.toHexString(i) + ";";
        }
    }
    private static final String NS = "ns";
    private boolean xmlDeclaration = true;
    private XmlNamespacePolicy namespacePolicy = XmlNamespacePolicy.IGNORE;
    private HashMap nsPrefixMap = new HashMap();
    private int nextPrefix = 0;
    private HashMap nsURIByElement = new HashMap();
    public static class XmlNamespacePolicy {
        private boolean qualifyElements;
        private boolean qualifyAttributes;
        public static final XmlNamespacePolicy IGNORE =
            new XmlNamespacePolicy(false, false);
        public static final XmlNamespacePolicy ONLY_QUALIFY_ELEMENTS =
            new XmlNamespacePolicy(true, false);
        public static final XmlNamespacePolicy QUALIFY_ALL =
            new XmlNamespacePolicy(true, true);
        public XmlNamespacePolicy(boolean qualifyElements,
                                  boolean qualifyAttributes) {
            this.qualifyElements = qualifyElements;
            this.qualifyAttributes = qualifyAttributes;
        }
    }
    public DOMElementWriter() {
    }
    public DOMElementWriter(boolean xmlDeclaration) {
        this(xmlDeclaration, XmlNamespacePolicy.IGNORE);
    }
    public DOMElementWriter(boolean xmlDeclaration,
                            XmlNamespacePolicy namespacePolicy) {
        this.xmlDeclaration = xmlDeclaration;
        this.namespacePolicy = namespacePolicy;
    }
    private static String lSep = System.getProperty("line.separator");
    protected String[] knownEntities = {"gt", "amp", "lt", "apos", "quot"};
    public void write(Element root, OutputStream out) throws IOException {
        Writer wri = new OutputStreamWriter(out, "UTF8");
        writeXMLDeclaration(wri);
        write(root, wri, 0, "  ");
        wri.flush();
    }
    public void writeXMLDeclaration(Writer wri) throws IOException {
        if (xmlDeclaration) {
            wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        }
    }
    public void write(Element element, Writer out, int indent,
                      String indentWith)
        throws IOException {
        NodeList children = element.getChildNodes();
        boolean hasChildren = (children.getLength() > 0);
        boolean hasChildElements = false;
        openElement(element, out, indent, indentWith, hasChildren);
        if (hasChildren) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    hasChildElements = true;
                    if (i == 0) {
                        out.write(lSep);
                    }
                    write((Element) child, out, indent + 1, indentWith);
                    break;
                case Node.TEXT_NODE:
                    out.write(encode(child.getNodeValue()));
                    break;
                case Node.COMMENT_NODE:
                    out.write("<!--");
                    out.write(encode(child.getNodeValue()));
                    out.write("-->");
                    break;
                case Node.CDATA_SECTION_NODE:
                    out.write("<![CDATA[");
                    out.write(encodedata(((Text) child).getData()));
                    out.write("]]>");
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    out.write('&');
                    out.write(child.getNodeName());
                    out.write(';');
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    out.write("<?");
                    out.write(child.getNodeName());
                    String data = child.getNodeValue();
                    if (data != null && data.length() > 0) {
                        out.write(' ');
                        out.write(data);
                    }
                    out.write("?>");
                    break;
                default:
                }
            }
            closeElement(element, out, indent, indentWith, hasChildElements);
        }
    }
    public void openElement(Element element, Writer out, int indent,
                            String indentWith)
        throws IOException {
        openElement(element, out, indent, indentWith, true);
    }
    public void openElement(Element element, Writer out, int indent,
                            String indentWith, boolean hasChildren)
        throws IOException {
        for (int i = 0; i < indent; i++) {
            out.write(indentWith);
        }
        out.write("<");
        if (namespacePolicy.qualifyElements) {
            String uri = getNamespaceURI(element);
            String prefix = (String) nsPrefixMap.get(uri);
            if (prefix == null) {
                if (nsPrefixMap.isEmpty()) {
                    prefix = "";
                } else {
                    prefix = NS + (nextPrefix++);
                }
                nsPrefixMap.put(uri, prefix);
                addNSDefinition(element, uri);
            }
            if (!"".equals(prefix)) {
                out.write(prefix);
                out.write(":");
            }
        }
        out.write(element.getTagName());
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            out.write(" ");
            if (namespacePolicy.qualifyAttributes) {
                String uri = getNamespaceURI(attr);
                String prefix = (String) nsPrefixMap.get(uri);
                if (prefix == null) {
                    prefix = NS + (nextPrefix++);
                    nsPrefixMap.put(uri, prefix);
                    addNSDefinition(element, uri);
                }
                out.write(prefix);
                out.write(":");
            }
            out.write(attr.getName());
            out.write("=\"");
            out.write(encodeAttributeValue(attr.getValue()));
            out.write("\"");
        }
        ArrayList al = (ArrayList) nsURIByElement.get(element);
        if (al != null) {
            Iterator iter = al.iterator();
            while (iter.hasNext()) {
                String uri = (String) iter.next();
                String prefix = (String) nsPrefixMap.get(uri);
                out.write(" xmlns");
                if (!"".equals(prefix)) {
                    out.write(":");
                    out.write(prefix);
                }
                out.write("=\"");
                out.write(uri);
                out.write("\"");
            }
        }
        if (hasChildren) {
            out.write(">");
        } else {
            removeNSDefinitions(element);
            out.write(" />");
            out.write(lSep);
            out.flush();
        }
    }
    public void closeElement(Element element, Writer out, int indent,
                             String indentWith, boolean hasChildren)
        throws IOException {
        if (hasChildren) {
            for (int i = 0; i < indent; i++) {
                out.write(indentWith);
            }
        }
        out.write("</");
        if (namespacePolicy.qualifyElements) {
            String uri = getNamespaceURI(element);
            String prefix = (String) nsPrefixMap.get(uri);
            if (prefix != null && !"".equals(prefix)) {
                out.write(prefix);
                out.write(":");
            }
            removeNSDefinitions(element);
        }
        out.write(element.getTagName());
        out.write(">");
        out.write(lSep);
        out.flush();
    }
    public String encode(String value) {
        return encode(value, false);
    }
    public String encodeAttributeValue(String value) {
        return encode(value, true);
    }
    private String encode(final String value, final boolean encodeWhitespace) {
        final int len = value.length();
        final StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            final char c = value.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            case '\"':
                sb.append("&quot;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            case '\r':
            case '\n':
            case '\t':
                if (encodeWhitespace) {
                    sb.append(WS_ENTITIES[c - '\t']);
                } else {
                    sb.append(c);
                }
                break;
            default:
                if (isLegalCharacter(c)) {
                    sb.append(c);
                }
                break;
            }
        }
        return sb.substring(0);
    }
    public String encodedata(final String value) {
        final int len = value.length();
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; ++i) {
            final char c = value.charAt(i);
            if (isLegalCharacter(c)) {
                sb.append(c);
            }
        }
        return StringUtils.replace(sb.substring(0), "]]>", "]]]]><![CDATA[>");
    }
    public boolean isReference(String ent) {
        if (!(ent.charAt(0) == '&') || !ent.endsWith(";")) {
            return false;
        }
        if (ent.charAt(1) == '#') {
            if (ent.charAt(2) == 'x') {
                try {
                    Integer.parseInt(ent.substring(3, ent.length() - 1), HEX);
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            } else {
                try {
                    Integer.parseInt(ent.substring(2, ent.length() - 1));
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        String name = ent.substring(1, ent.length() - 1);
        for (int i = 0; i < knownEntities.length; i++) {
            if (name.equals(knownEntities[i])) {
                return true;
            }
        }
        return false;
    }
    public boolean isLegalCharacter(final char c) {
        if (c == 0x9 || c == 0xA || c == 0xD) {
            return true;
        } else if (c < 0x20) {
            return false;
        } else if (c <= 0xD7FF) {
            return true;
        } else if (c < 0xE000) {
            return false;
        } else if (c <= 0xFFFD) {
            return true;
        }
        return false;
    }
    private void removeNSDefinitions(Element element) {
        ArrayList al = (ArrayList) nsURIByElement.get(element);
        if (al != null) {
            Iterator iter = al.iterator();
            while (iter.hasNext()) {
                nsPrefixMap.remove(iter.next());
            }
            nsURIByElement.remove(element);
        }
    }
    private void addNSDefinition(Element element, String uri) {
        ArrayList al = (ArrayList) nsURIByElement.get(element);
        if (al == null) {
            al = new ArrayList();
            nsURIByElement.put(element, al);
        }
        al.add(uri);
    }
    private static String getNamespaceURI(Node n) {
        String uri = n.getNamespaceURI();
        if (uri == null) {
            uri = "";
        }
        return uri;
    }
}
