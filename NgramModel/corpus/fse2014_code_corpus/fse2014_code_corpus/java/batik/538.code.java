package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableBooleanValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedBoolean;
public class SVGOMAnimatedBoolean
        extends    AbstractSVGAnimatedValue
        implements SVGAnimatedBoolean {
    protected boolean defaultValue;
    protected boolean valid;
    protected boolean baseVal;
    protected boolean animVal;
    protected boolean changing;
    public SVGOMAnimatedBoolean(AbstractElement elt,
                                String ns,
                                String ln,
                                boolean val) {
        super(elt, ns, ln);
        defaultValue = val;
    }
    public boolean getBaseVal() {
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
            baseVal = attr.getValue().equals("true");
        }
        valid = true;
    }
    public void setBaseVal(boolean baseVal) throws DOMException {
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
    public boolean getAnimVal() {
        if (hasAnimVal) {
            return animVal;
        }
        if (!valid) {
            update();
        }
        return baseVal;
    }
    public void setAnimatedValue(boolean animVal) {
        hasAnimVal = true;
        this.animVal = animVal;
        fireAnimatedAttributeListeners();
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            this.animVal = ((AnimatableBooleanValue) val).getValue();
        }
        fireAnimatedAttributeListeners();
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        return new AnimatableBooleanValue(target, getBaseVal());
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
