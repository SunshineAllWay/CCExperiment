package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGColorProfileElement;
public class SVGOMColorProfileElement
    extends    SVGOMURIReferenceElement
    implements SVGColorProfileElement {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(5);
        attributeInitializer.addAttribute(null, null,
                                          SVG_RENDERING_INTENT_ATTRIBUTE,
                                          SVG_AUTO_VALUE);
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
    protected SVGOMColorProfileElement() {
    }
    public SVGOMColorProfileElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_COLOR_PROFILE_TAG;
    }
    public String getLocal() {
        return getAttributeNS(null, SVG_LOCAL_ATTRIBUTE);
    }
    public void setLocal(String local) throws DOMException {
        setAttributeNS(null, SVG_LOCAL_ATTRIBUTE, local);
    }
    public String getName() {
        return getAttributeNS(null, SVG_NAME_ATTRIBUTE);
    }
    public void setName(String name) throws DOMException {
        setAttributeNS(null, SVG_NAME_ATTRIBUTE, name);
    }
    public short getRenderingIntent() {
        Attr attr = getAttributeNodeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE);
        if (attr == null) {
            return RENDERING_INTENT_AUTO;
        }
        String val = attr.getValue();
        switch (val.length()) {
        case 4:
            if (val.equals(SVG_AUTO_VALUE)) {
                return RENDERING_INTENT_AUTO;
            }
            break;
        case 10:
            if (val.equals(SVG_PERCEPTUAL_VALUE)) {
                return RENDERING_INTENT_PERCEPTUAL;
            }
            if (val.equals(SVG_SATURATE_VALUE)) {
                return RENDERING_INTENT_SATURATION;
            }
            break;
        case 21:
            if (val.equals(SVG_ABSOLUTE_COLORIMETRIC_VALUE)) {
                return RENDERING_INTENT_ABSOLUTE_COLORIMETRIC;
            }
            if (val.equals(SVG_RELATIVE_COLORIMETRIC_VALUE)) {
                return RENDERING_INTENT_RELATIVE_COLORIMETRIC;
            }
        }
        return RENDERING_INTENT_UNKNOWN;
    }
    public void setRenderingIntent(short renderingIntent) throws DOMException {
        switch (renderingIntent) {
        case RENDERING_INTENT_AUTO:
            setAttributeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE,
                           SVG_AUTO_VALUE);
            break;
        case RENDERING_INTENT_PERCEPTUAL:
            setAttributeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE,
                           SVG_PERCEPTUAL_VALUE);
            break;
        case RENDERING_INTENT_RELATIVE_COLORIMETRIC:
            setAttributeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE,
                           SVG_RELATIVE_COLORIMETRIC_VALUE);
            break;
        case RENDERING_INTENT_SATURATION:
            setAttributeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE,
                           SVG_SATURATE_VALUE);
            break;
        case RENDERING_INTENT_ABSOLUTE_COLORIMETRIC:
            setAttributeNS(null, SVG_RENDERING_INTENT_ATTRIBUTE,
                           SVG_ABSOLUTE_COLORIMETRIC_VALUE);
        }
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMColorProfileElement();
    }
}
