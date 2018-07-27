package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;
public abstract class SVGDescriptiveElement extends SVGStylableElement {
    protected SVGDescriptiveElement() {
    }
    protected SVGDescriptiveElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getXMLlang() {
        return XMLSupport.getXMLLang(this);
    }
    public void setXMLlang(String lang) {
        setAttributeNS(XML_NAMESPACE_URI, XML_LANG_QNAME, lang);
    }
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }
    public void setXMLspace(String space) {
        setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, space);
    }
}
