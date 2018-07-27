package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimateTransformElement;
public class SVGOMAnimateTransformElement
    extends    SVGOMAnimationElement
    implements SVGAnimateTransformElement {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(1);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_TYPE_ATTRIBUTE,
                                          SVG_TRANSLATE_VALUE);
    }
    protected SVGOMAnimateTransformElement() {
    }
    public SVGOMAnimateTransformElement(String prefix,
                                        AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ANIMATE_TRANSFORM_TAG;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMAnimateTransformElement();
    }
}
