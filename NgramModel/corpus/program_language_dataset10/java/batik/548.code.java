package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatablePreserveAspectRatioValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
public class SVGOMAnimatedPreserveAspectRatio
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedPreserveAspectRatio {
    protected BaseSVGPARValue baseVal;
    protected AnimSVGPARValue animVal;
    protected boolean changing;
    public SVGOMAnimatedPreserveAspectRatio(AbstractElement elt) {
        super(elt, null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
    }
    public SVGPreserveAspectRatio getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGPARValue();
        }
        return baseVal;
    }
    public SVGPreserveAspectRatio getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGPARValue();
        }
        return animVal;
    }
    public void check() {
        if (!hasAnimVal) {
            if (baseVal == null) {
                baseVal = new BaseSVGPARValue();
            }
            if (baseVal.malformed) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                     baseVal.getValueAsString());
            }
        }
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        SVGPreserveAspectRatio par = getBaseVal();
        return new AnimatablePreserveAspectRatioValue(target, par.getAlign(),
                                                      par.getMeetOrSlice());
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            if (animVal == null) {
                animVal = new AnimSVGPARValue();
            }
            AnimatablePreserveAspectRatioValue animPAR =
                (AnimatablePreserveAspectRatioValue) val;
            animVal.setAnimatedValue(animPAR.getAlign(),
                                     animPAR.getMeetOrSlice());
        }
        fireAnimatedAttributeListeners();
    }
    public void attrAdded(Attr node, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrRemoved(Attr node, String oldv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public class BaseSVGPARValue extends AbstractSVGPreserveAspectRatio {
        protected boolean malformed;
        public BaseSVGPARValue() {
            invalidate();
        }
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected void setAttributeValue(String value) throws DOMException {
            try {
                changing = true;
                element.setAttributeNS
                    (null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                     value);
                malformed = false;
            } finally {
                changing = false;
            }
        }
        protected void invalidate() {
            String s = element.getAttributeNS
                (null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
            setValueAsString(s);
        }
    }
    public class AnimSVGPARValue extends AbstractSVGPreserveAspectRatio {
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected void setAttributeValue(String value) throws DOMException {
        }
        public short getAlign() {
            if (hasAnimVal) {
                return super.getAlign();
            }
            return getBaseVal().getAlign();
        }
        public short getMeetOrSlice() {
            if (hasAnimVal) {
                return super.getMeetOrSlice();
            }
            return getBaseVal().getMeetOrSlice();
        }
        public void setAlign(short align) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.preserve.aspect.ratio", null);
        }
        public void setMeetOrSlice(short meetOrSlice) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.preserve.aspect.ratio", null);
        }
        protected void setAnimatedValue(short align, short meetOrSlice) {
            this.align = align;
            this.meetOrSlice = meetOrSlice;
        }
    }
}
