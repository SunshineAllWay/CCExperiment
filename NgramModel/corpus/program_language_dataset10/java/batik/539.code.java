package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
public class SVGOMAnimatedEnumeration extends AbstractSVGAnimatedValue
                                      implements SVGAnimatedEnumeration {
    protected String[] values;
    protected short defaultValue;
    protected boolean valid;
    protected short baseVal;
    protected short animVal;
    protected boolean changing;
    public SVGOMAnimatedEnumeration(AbstractElement elt,
                                    String ns,
                                    String ln,
                                    String[] val,
                                    short def) {
        super(elt, ns, ln);
        values = val;
        defaultValue = def;
    }
    public short getBaseVal() {
        if (!valid) {
            update();
        }
        return baseVal;
    }
    public String getBaseValAsString() {
        if (!valid) {
            update();
        }
        return values[baseVal];
    }
    protected void update() {
        String val = element.getAttributeNS(namespaceURI, localName);
        if (val.length() == 0) {
            baseVal = defaultValue;
        } else {
            baseVal = getEnumerationNumber(val);
        }
        valid = true;
    }
    protected short getEnumerationNumber(String s) {
        for (short i = 0; i < values.length; i++) {
            if (s.equals(values[i])) {
                return i;
            }
        }
        return 0;
    }
    public void setBaseVal(short baseVal) throws DOMException {
        if (baseVal >= 0 && baseVal < values.length) {
            try {
                this.baseVal = baseVal;
                valid = true;
                changing = true;
                element.setAttributeNS(namespaceURI, localName,
                                       values[baseVal]);
            } finally {
                changing = false;
            }
        }
    }
    public short getAnimVal() {
        if (hasAnimVal) {
            return animVal;
        }
        if (!valid) {
            update();
        }
        return baseVal;
    }
    public short getCheckedVal() {
        if (hasAnimVal) {
            return animVal;
        }
        if (!valid) {
            update();
        }
        if (baseVal == 0) {
            throw new LiveAttributeException
                (element, localName,
                 LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                 getBaseValAsString());
        }
        return baseVal;
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        return new AnimatableStringValue(target, getBaseValAsString());
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
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            this.animVal =
                getEnumerationNumber(((AnimatableStringValue) val).getString());
            fireAnimatedAttributeListeners();
        }
        fireAnimatedAttributeListeners();
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
