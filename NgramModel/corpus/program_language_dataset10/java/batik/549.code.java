package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableRectValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.DefaultNumberListHandler;
import org.apache.batik.parser.NumberListParser;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGRect;
public class SVGOMAnimatedRect
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedRect {
    protected BaseSVGRect baseVal;
    protected AnimSVGRect animVal;
    protected boolean changing;
    protected String defaultValue;
    public SVGOMAnimatedRect(AbstractElement elt, String ns, String ln,
                             String def) {
        super(elt, ns, ln);
        defaultValue = def;
    }
    public SVGRect getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGRect();
        }
        return baseVal;
    }
    public SVGRect getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGRect();
        }
        return animVal;
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatableRectValue animRect = (AnimatableRectValue) val;
            if (animVal == null) {
                animVal = new AnimSVGRect();
            }
            animVal.setAnimatedValue(animRect.getX(), animRect.getY(),
                                     animRect.getWidth(), animRect.getHeight());
        }
        fireAnimatedAttributeListeners();
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        SVGRect r = getBaseVal();
        return new AnimatableRectValue
            (target, r.getX(), r.getY(), r.getWidth(), r.getHeight());
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
    protected class BaseSVGRect extends SVGOMRect {
        protected boolean valid;
        public void invalidate() {
            valid = false;
        }
        protected void reset() {
            try {
                changing = true;
                element.setAttributeNS
                    (namespaceURI, localName,
                     Float.toString(x) + ' ' + y + ' ' + w + ' ' + h);
            } finally {
                changing = false;
            }
        }
        protected void revalidate() {
            if (valid) {
                return;
            }
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            final String s = attr == null ? defaultValue : attr.getValue();
            final float[] numbers = new float[4];
            NumberListParser p = new NumberListParser();
            p.setNumberListHandler(new DefaultNumberListHandler() {
                protected int count;
                public void endNumberList() {
                    if (count != 4) {
                        throw new LiveAttributeException
                            (element, localName,
                             LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                             s);
                    }
                }
                public void numberValue(float v) throws ParseException {
                    if (count < 4) {
                        numbers[count] = v;
                    }
                    if (v < 0 && (count == 2 || count == 3)) {
                        throw new LiveAttributeException
                            (element, localName,
                             LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                             s);
                    }
                    count++;
                }
            });
            p.parse(s);
            x = numbers[0];
            y = numbers[1];
            w = numbers[2];
            h = numbers[3];
            valid = true;
        }
        public float getX() {
            revalidate();
            return x;
        }
        public void setX(float x) throws DOMException {
            this.x = x;
            reset();
        }
        public float getY() {
            revalidate();
            return y;
        }
        public void setY(float y) throws DOMException {
            this.y = y;
            reset();
        }
        public float getWidth() {
            revalidate();
            return w;
        }
        public void setWidth(float width) throws DOMException {
            this.w = width;
            reset();
        }
        public float getHeight() {
            revalidate();
            return h;
        }
        public void setHeight(float height) throws DOMException {
            this.h = height;
            reset();
        }
    }
    protected class AnimSVGRect extends SVGOMRect {
        public float getX() {
            if (hasAnimVal) {
                return super.getX();
            }
            return getBaseVal().getX();
        }
        public float getY() {
            if (hasAnimVal) {
                return super.getY();
            }
            return getBaseVal().getY();
        }
        public float getWidth() {
            if (hasAnimVal) {
                return super.getWidth();
            }
            return getBaseVal().getWidth();
        }
        public float getHeight() {
            if (hasAnimVal) {
                return super.getHeight();
            }
            return getBaseVal().getHeight();
        }
        public void setX(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void setY(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void setWidth(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        public void setHeight(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.length",
                 null);
        }
        protected void setAnimatedValue(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
