package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableNumberValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedNumber;
public class SVGOMAnimatedNumber
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedNumber {
    protected float defaultValue;
    protected boolean allowPercentage;
    protected boolean valid;
    protected float baseVal;
    protected float animVal;
    protected boolean changing;
    public SVGOMAnimatedNumber(AbstractElement elt,
                               String ns,
                               String ln,
                               float  val) {
        this(elt, ns, ln, val, false);
    }
    public SVGOMAnimatedNumber(AbstractElement elt,
                               String  ns,
                               String  ln,
                               float   val,
                               boolean allowPercentage) {
        super(elt, ns, ln);
        defaultValue = val;
        this.allowPercentage = allowPercentage;
    }
    public float getBaseVal() {
        if (!valid) {
            update();
        }
        return baseVal;
    }
    protected void update() {
        Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
        if (attr == null) {
            baseVal = defaultValue;
        } else {
            String v = attr.getValue();
            int len = v.length();
            if (allowPercentage && len > 1 && v.charAt(len - 1) == '%') {
                baseVal = .01f * Float.parseFloat(v.substring(0, len - 1));
            } else {
                baseVal = Float.parseFloat(v);
            }
        }
        valid = true;
    }
    public void setBaseVal(float baseVal) throws DOMException {
        try {
            this.baseVal = baseVal;
            valid = true;
            changing = true;
            element.setAttributeNS(namespaceURI, localName,
                                   String.valueOf(baseVal));
        } finally {
            changing = false;
        }
    }
    public float getAnimVal() {
        if (hasAnimVal) {
            return animVal;
        }
        if (!valid) {
            update();
        }
        return baseVal;
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        return new AnimatableNumberValue(target, getBaseVal());
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            this.animVal = ((AnimatableNumberValue) val).getValue();
        }
        fireAnimatedAttributeListeners();
    }
    public void attrAdded(Attr node, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrRemoved(Attr node, String oldv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
}
