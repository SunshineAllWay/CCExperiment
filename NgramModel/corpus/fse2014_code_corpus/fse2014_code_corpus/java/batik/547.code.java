package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatablePointListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedPoints;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;
public class SVGOMAnimatedPoints
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedPoints {
    protected BaseSVGPointList baseVal;
    protected AnimSVGPointList animVal;
    protected boolean changing;
    protected String defaultValue;
    public SVGOMAnimatedPoints(AbstractElement elt,
                               String ns,
                               String ln,
                               String defaultValue) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
    }
    public SVGPointList getPoints() {
        if (baseVal == null) {
            baseVal = new BaseSVGPointList();
        }
        return baseVal;
    }
    public SVGPointList getAnimatedPoints() {
        if (animVal == null) {
            animVal = new AnimSVGPointList();
        }
        return animVal;
    }
    public void check() {
        if (!hasAnimVal) {
            if (baseVal == null) {
                baseVal = new BaseSVGPointList();
            }
            baseVal.revalidate();
            if (baseVal.missing) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MISSING, null);
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
        SVGPointList pl = getPoints();
        int n = pl.getNumberOfItems();
        float[] points = new float[n * 2];
        for (int i = 0; i < n; i++) {
            SVGPoint p = pl.getItem(i);
            points[i * 2] = p.getX();
            points[i * 2 + 1] = p.getY();
        }
        return new AnimatablePointListValue(target, points);
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatablePointListValue animPointList =
                (AnimatablePointListValue) val;
            if (animVal == null) {
                animVal = new AnimSVGPointList();
            }
            animVal.setAnimatedValue(animPointList.getNumbers());
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
    protected class BaseSVGPointList extends AbstractSVGPointList {
        protected boolean missing;
        protected boolean malformed;
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        protected String getValueAsString() {
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            if (attr == null) {
                return defaultValue;
            }
            return attr.getValue();
        }
        protected void setAttributeValue(String value) {
            try {
                changing = true;
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }
        protected void resetAttribute() {
            super.resetAttribute();
            missing = false;
            malformed = false;
        }
        protected void resetAttribute(SVGItem item) {
            super.resetAttribute(item);
            missing = false;
            malformed = false;
        }
        protected void revalidate() {
            if (valid) {
                return;
            }
            valid = true;
            missing = false;
            malformed = false;
            String s = getValueAsString();
            if (s == null) {
                missing = true;
                return;
            }
            try {
                ListBuilder builder = new ListBuilder();
                doParse(s, builder);
                if (builder.getList() != null) {
                    clear(itemList);
                }
                itemList = builder.getList();
            } catch (ParseException e) {
                itemList = new ArrayList(1);
                malformed = true;
            }
        }
    }
    protected class AnimSVGPointList extends AbstractSVGPointList {
        public AnimSVGPointList() {
            itemList = new ArrayList(1);
        }
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        public int getNumberOfItems() {
            if (hasAnimVal) {
                return super.getNumberOfItems();
            }
            return getPoints().getNumberOfItems();
        }
        public SVGPoint getItem(int index) throws DOMException {
            if (hasAnimVal) {
                return super.getItem(index);
            }
            return getPoints().getItem(index);
        }
        protected String getValueAsString() {
            if (itemList.size() == 0) {
                return "";
            }
            StringBuffer sb = new StringBuffer( itemList.size() * 8 );
            Iterator i = itemList.iterator();
            if (i.hasNext()) {
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            while (i.hasNext()) {
                sb.append(getItemSeparator());
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            return sb.toString();
        }
        protected void setAttributeValue(String value) {
        }
        public void clear() throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        public SVGPoint initialize(SVGPoint newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        public SVGPoint insertItemBefore(SVGPoint newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        public SVGPoint replaceItem(SVGPoint newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        public SVGPoint removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        public SVGPoint appendItem(SVGPoint newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.point.list", null);
        }
        protected void setAnimatedValue(float[] pts) {
            int size = itemList.size();
            int i = 0;
            while (i < size && i < pts.length / 2) {
                SVGPointItem p = (SVGPointItem) itemList.get(i);
                p.x = pts[i * 2];
                p.y = pts[i * 2 + 1];
                i++;
            }
            while (i < pts.length / 2) {
                appendItemImpl(new SVGPointItem(pts[i * 2], pts[i * 2 + 1]));
                i++;
            }
            while (size > pts.length / 2) {
                removeItemImpl(--size);
            }
        }
        protected void resetAttribute() {
        }
        protected void resetAttribute(SVGItem item) {
        }
        protected void revalidate() {
            valid = true;
        }
    }
}
