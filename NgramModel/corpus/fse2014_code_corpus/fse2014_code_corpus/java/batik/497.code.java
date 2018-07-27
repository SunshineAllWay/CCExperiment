package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableLengthValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.UnitProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGLength;
public abstract class AbstractSVGAnimatedLength
    extends    AbstractSVGAnimatedValue
    implements SVGAnimatedLength,
               LiveAttributeValue {
    public static final short HORIZONTAL_LENGTH =
        UnitProcessor.HORIZONTAL_LENGTH;
    public static final short VERTICAL_LENGTH =
        UnitProcessor.VERTICAL_LENGTH;
    public static final short OTHER_LENGTH =
        UnitProcessor.OTHER_LENGTH;
    protected short direction;
    protected BaseSVGLength baseVal;
    protected AnimSVGLength animVal;
    protected boolean changing;
    protected boolean nonNegative;
    public AbstractSVGAnimatedLength(AbstractElement elt,
                                     String ns,
                                     String ln,
                                     short dir,
                                     boolean nonneg) {
        super(elt, ns, ln);
        direction = dir;
        nonNegative = nonneg;
    }
    protected abstract String getDefaultValue();
    public SVGLength getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGLength(direction);
        }
        return baseVal;
    }
    public SVGLength getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGLength(direction);
        }
        return animVal;
    }
    public float getCheckedValue() {
        if (hasAnimVal) {
            if (animVal == null) {
                animVal = new AnimSVGLength(direction);
            }
            if (nonNegative && animVal.value < 0) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_NEGATIVE,
                     animVal.getValueAsString());
            }
            return animVal.getValue();
        } else {
            if (baseVal == null) {
                baseVal = new BaseSVGLength(direction);
            }
            baseVal.revalidate();
            if (baseVal.missing) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MISSING, null);
            } else if (baseVal.unitType ==
                        SVGLength.SVG_LENGTHTYPE_UNKNOWN) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                     baseVal.getValueAsString());
            }
            if (nonNegative && baseVal.value < 0) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_NEGATIVE,
                     baseVal.getValueAsString());
            }
            return baseVal.getValue();
        }
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatableLengthValue animLength = (AnimatableLengthValue) val;
            if (animVal == null) {
                animVal = new AnimSVGLength(direction);
            }
            animVal.setAnimatedValue(animLength.getLengthType(),
                                     animLength.getLengthValue());
        }
        fireAnimatedAttributeListeners();
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        SVGLength base = getBaseVal();
        return new AnimatableLengthValue
            (target, base.getUnitType(), base.getValueInSpecifiedUnits(),
             target.getPercentageInterpretation
                 (getNamespaceURI(), getLocalName(), false));
    }
    public void attrAdded(Attr node, String newv) {
        attrChanged();
    }
    public void attrModified(Attr node, String oldv, String newv) {
        attrChanged();
    }
    public void attrRemoved(Attr node, String oldv) {
        attrChanged();
    }
    protected void attrChanged() {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    protected class BaseSVGLength extends AbstractSVGLength {
        protected boolean valid;
        protected boolean missing;
        public BaseSVGLength(short direction) {
            super(direction);
        }
        public void invalidate() {
            valid = false;
        }
        protected void reset() {
            try {
                changing = true;
                valid = true;
                String value = getValueAsString();
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }
        protected void revalidate() {
            if (valid) {
                return;
            }
            missing = false;
            valid = true;
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            String s;
            if (attr == null) {
                s = getDefaultValue();
                if (s == null) {
                    missing = true;
                    return;
                }
            } else {
                s = attr.getValue();
            }
            parse(s);
        }
        protected SVGOMElement getAssociatedElement() {
            return (SVGOMElement)element;
        }
    }
    protected class AnimSVGLength extends AbstractSVGLength {
        public AnimSVGLength(short direction) {
            super(direction);
        }
        public short getUnitType() {
            if (hasAnimVal) {
                return super.getUnitType();
            }
            return getBaseVal().getUnitType();
        }
        public float getValue() {
            if (hasAnimVal) {
                return super.getValue();
            }
            return getBaseVal().getValue();
        }
        public float getValueInSpecifiedUnits() {
            if (hasAnimVal) {
                return super.getValueInSpecifiedUnits();
            }
            return getBaseVal().getValueInSpecifiedUnits();
        }
        public String getValueAsString() {
            if (hasAnimVal) {
                return super.getValueAsString();
            }
            return getBaseVal().getValueAsString();
        }
        public void setValue(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void setValueInSpecifiedUnits(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void setValueAsString(String value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void newValueSpecifiedUnits(short unit, float value) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void convertToSpecifiedUnits(short unit) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        protected SVGOMElement getAssociatedElement() {
            return (SVGOMElement) element;
        }
        protected void setAnimatedValue(int type, float val) {
            super.newValueSpecifiedUnits((short) type, val);
        }
    }
}
