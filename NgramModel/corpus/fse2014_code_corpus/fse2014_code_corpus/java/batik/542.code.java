package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatableLengthListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedLengthList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGLengthList;
public class SVGOMAnimatedLengthList
    extends AbstractSVGAnimatedValue
    implements SVGAnimatedLengthList {
    protected BaseSVGLengthList baseVal;
    protected AnimSVGLengthList animVal;
    protected boolean changing;
    protected String defaultValue;
    protected boolean emptyAllowed;
    protected short direction;
    public SVGOMAnimatedLengthList(AbstractElement elt,
                                   String ns,
                                   String ln,
                                   String defaultValue,
                                   boolean emptyAllowed,
                                   short direction) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
        this.emptyAllowed = emptyAllowed;
        this.direction = direction;
    }
    public SVGLengthList getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGLengthList();
        }
        return baseVal;
    }
    public SVGLengthList getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGLengthList();
        }
        return animVal;
    }
    public void check() {
        if (!hasAnimVal) {
            if (baseVal == null) {
                baseVal = new BaseSVGLengthList();
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
        SVGLengthList ll = getBaseVal();
        int n = ll.getNumberOfItems();
        short[] types = new short[n];
        float[] values = new float[n];
        for (int i = 0; i < n; i++) {
            SVGLength l = ll.getItem(i);
            types[i] = l.getUnitType();
            values[i] = l.getValueInSpecifiedUnits();
        }
        return new AnimatableLengthListValue
            (target, types, values,
             target.getPercentageInterpretation
                 (getNamespaceURI(), getLocalName(), false));
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatableLengthListValue animLengths =
                (AnimatableLengthListValue) val;
            if (animVal == null) {
                animVal = new AnimSVGLengthList();
            }
            animVal.setAnimatedValue(animLengths.getLengthTypes(),
                                     animLengths.getLengthValues());
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
    public class BaseSVGLengthList extends AbstractSVGLengthList {
        protected boolean missing;
        protected boolean malformed;
        public BaseSVGLengthList() {
            super(SVGOMAnimatedLengthList.this.direction);
        }
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        protected Element getElement() {
            return element;
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
            boolean isEmpty = s != null && s.length() == 0;
            if (s == null || isEmpty && !emptyAllowed) {
                missing = true;
                return;
            }
            if (isEmpty) {
                itemList = new ArrayList(1);
            } else {
                try {
                    ListBuilder builder = new ListBuilder();
                    doParse(s, builder);
                    if (builder.getList() != null) {
                        clear(itemList);
                    }
                    itemList = builder.getList();
                } catch (ParseException e) {
                    itemList = new ArrayList(1);
                    valid = true;
                    malformed = true;
                }
            }
        }
    }
    protected class AnimSVGLengthList extends AbstractSVGLengthList {
        public AnimSVGLengthList() {
            super(SVGOMAnimatedLengthList.this.direction);
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
        protected Element getElement() {
            return element;
        }
        public int getNumberOfItems() {
            if (hasAnimVal) {
                return super.getNumberOfItems();
            }
            return getBaseVal().getNumberOfItems();
        }
        public SVGLength getItem(int index) throws DOMException {
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
                 "readonly.length.list", null);
        }
        public SVGLength initialize(SVGLength newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }
        public SVGLength insertItemBefore(SVGLength newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }
        public SVGLength replaceItem(SVGLength newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }
        public SVGLength removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }
        public SVGLength appendItem(SVGLength newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }
        protected void setAnimatedValue(short[] types, float[] values) {
            int size = itemList.size();
            int i = 0;
            while (i < size && i < types.length) {
                SVGLengthItem l = (SVGLengthItem) itemList.get(i);
                l.unitType  = types[i];
                l.value     = values[i];
                l.direction = this.direction;
                i++;
            }
            while (i < types.length) {
                appendItemImpl(new SVGLengthItem(types[i], values[i],
                                                 this.direction));
                i++;
            }
            while (size > types.length) {
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
