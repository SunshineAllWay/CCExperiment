package org.apache.batik.dom.util;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public final class XMLSupport implements XMLConstants {
    private XMLSupport() {
    }
    public static String getXMLLang(Element elt) {
        Attr attr = elt.getAttributeNodeNS(XML_NAMESPACE_URI, "lang");
        if (attr != null) {
            return attr.getNodeValue();
        }
        for (Node n = elt.getParentNode(); n != null; n = n.getParentNode()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                attr = ((Element)n).getAttributeNodeNS(XML_NAMESPACE_URI,
                                                       "lang");
                if (attr != null) {
                    return attr.getNodeValue();
                }
            }
        }
        return "en";
    }
    public static String getXMLSpace(Element elt) {
        Attr attr = elt.getAttributeNodeNS(XML_NAMESPACE_URI, "space");
        if (attr != null) {
            return attr.getNodeValue();
        }
        for (Node n = elt.getParentNode(); n != null; n = n.getParentNode()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                attr = ((Element)n).getAttributeNodeNS(XML_NAMESPACE_URI,
                                                       "space");
                if (attr != null) {
                    return attr.getNodeValue();
                }
            }
        }
        return "default";
    }
    public static String defaultXMLSpace(String data) {
        int nChars = data.length();
        StringBuffer result = new StringBuffer( nChars );
        boolean space = false;
        for (int i = 0; i < nChars; i++) {
            char c = data.charAt(i);
            switch (c) {
            case 10:               
            case 13:
                space = false;
                break;
            case ' ':              
            case '\t':
                if (!space) {
                    result.append(' ');
                    space = true;
                }
                break;
            default:
                result.append(c);
                space = false;
            }
        }
        return result.toString().trim();
    }
    public static String preserveXMLSpace(String data) {
        int nChars = data.length();
        StringBuffer result = new StringBuffer( nChars );
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            switch (c) {
            case 10:               
            case 13:
            case '\t':
                result.append(' ');
                break;
            default:
                result.append(c);
            }
        }
        return result.toString();
    }
}
