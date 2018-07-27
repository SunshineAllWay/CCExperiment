package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedString;
public class SVGOMAnimatedString extends AbstractSVGAnimatedValue
                                 implements SVGAnimatedString {
    protected String animVal;
    public SVGOMAnimatedString(AbstractElement elt,
                               String ns,
                               String ln) {
        super(elt, ns, ln);
    }
    public String getBaseVal() {
        return element.getAttributeNS(namespaceURI, localName);
    }
    public void setBaseVal(String baseVal) throws DOMException {
        element.setAttributeNS(namespaceURI, localName, baseVal);
    }
    public String getAnimVal() {
        if (hasAnimVal) {
            return animVal;
        }
        return element.getAttributeNS(namespaceURI, localName);
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        return new AnimatableStringValue(target, getBaseVal());
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            this.animVal = ((AnimatableStringValue) val).getString();
        }
        fireAnimatedAttributeListeners();
    }
    public void attrAdded(Attr node, String newv) {
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrModified(Attr node, String oldv, String newv) {
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrRemoved(Attr node, String oldv) {
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
}
