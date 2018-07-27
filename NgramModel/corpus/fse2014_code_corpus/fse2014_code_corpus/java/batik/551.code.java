package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.anim.values.AnimatableTransformListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;
public class SVGOMAnimatedTransformList
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedTransformList {
    protected BaseSVGTransformList baseVal;
    protected AnimSVGTransformList animVal;
    protected boolean changing;
    protected String defaultValue;
    public SVGOMAnimatedTransformList(AbstractElement elt,
                                      String ns,
                                      String ln,
                                      String defaultValue) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
    }
    public SVGTransformList getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGTransformList();
        }
        return baseVal;
    }
    public SVGTransformList getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGTransformList();
        }
        return animVal;
    }
    public void check() {
        if (!hasAnimVal) {
            if (baseVal == null) {
                baseVal = new BaseSVGTransformList();
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
        SVGTransformList tl = getBaseVal();
        int n = tl.getNumberOfItems();
        List v = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            v.add(tl.getItem(i));
        }
        return new AnimatableTransformListValue(target, v);
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatableTransformListValue aval =
                (AnimatableTransformListValue) val;
            if (animVal == null) {
                animVal = new AnimSVGTransformList();
            }
            animVal.setAnimatedValue(aval.getTransforms());
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
    public class BaseSVGTransformList extends AbstractSVGTransformList {
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
    protected class AnimSVGTransformList extends AbstractSVGTransformList {
        public AnimSVGTransformList() {
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
            return getBaseVal().getNumberOfItems();
        }
        public SVGTransform getItem(int index) throws DOMException {
            if (hasAnimVal) {
                return super.getItem(index);
            }
            return getBaseVal().getItem(index);
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
                 "readonly.transform.list", null);
        }
        public SVGTransform initialize(SVGTransform newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        public SVGTransform insertItemBefore(SVGTransform newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        public SVGTransform replaceItem(SVGTransform newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        public SVGTransform removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        public SVGTransform appendItem(SVGTransform newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        public SVGTransform consolidate() {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }
        protected void setAnimatedValue(Iterator it) {
            int size = itemList.size();
            int i = 0;
            while (i < size && it.hasNext()) {
                SVGTransformItem t = (SVGTransformItem) itemList.get(i);
                t.assign((SVGTransform) it.next());
                i++;
            }
            while (it.hasNext()) {
                appendItemImpl(new SVGTransformItem((SVGTransform) it.next()));
                i++;
            }
            while (size > i) {
                removeItemImpl(--size);
            }
        }
        protected void setAnimatedValue(SVGTransform transform) {
            int size = itemList.size();
            while (size > 1) {
                removeItemImpl(--size);
            }
            if (size == 0) {
                appendItemImpl(new SVGTransformItem(transform));
            } else {
                SVGTransformItem t = (SVGTransformItem) itemList.get(0);
                t.assign(transform);
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
