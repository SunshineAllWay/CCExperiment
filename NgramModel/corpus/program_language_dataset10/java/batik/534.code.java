package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAltGlyphElement;
public class SVGOMAltGlyphElement
    extends    SVGURIReferenceTextPositioningElement
    implements SVGAltGlyphElement {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(4);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null, "xmlns:xlink",
                                          XLinkSupport.XLINK_NAMESPACE_URI);
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "type", "simple");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "show", "other");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "actuate", "onLoad");
    }
    protected SVGOMAltGlyphElement() {
    }
    public SVGOMAltGlyphElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ALT_GLYPH_TAG;
    }
    public String getGlyphRef() {
        return getAttributeNS(null, SVG_GLYPH_REF_ATTRIBUTE);
    }
    public void setGlyphRef(String glyphRef) throws DOMException {
        setAttributeNS(null, SVG_GLYPH_REF_ATTRIBUTE, glyphRef);
    }
    public String getFormat() {
        return getAttributeNS(null, SVG_FORMAT_ATTRIBUTE);
    }
    public void setFormat(String format) throws DOMException {
        setAttributeNS(null, SVG_FORMAT_ATTRIBUTE, format);
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMAltGlyphElement();
    }
}
