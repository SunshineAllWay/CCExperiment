package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatableNumberListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGNumberList;
public class SVGOMAnimatedNumberList
    extends AbstractSVGAnimatedValue
    implements SVGAnimatedNumberList {
    protected BaseSVGNumberList baseVal;
    protected AnimSVGNumberList animVal;
    protected boolean changing;
    protected String defaultValue;
    protected boolean emptyAllowed;
    public SVGOMAnimatedNumberList(AbstractElement elt,
                                   String ns,
                                   String ln,
                                   String defaultValue,
                                   boolean emptyAllowed) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
        this.emptyAllowed = emptyAllowed;
    }
    public SVGNumberList getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGNumberList();
        }
        return baseVal;
    }
    public SVGNumberList getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGNumberList();
        }
        return animVal;
    }
    public void check() {
        if (!hasAnimVal) {
            if (baseVal == null) {
                baseVal = new BaseSVGNumberList();
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
        SVGNumberList nl = getBaseVal();
        int n = nl.getNumberOfItems();
        float[] numbers = new float[n];
        for (int i = 0; i < n; i++) {
            numbers[i] = nl.getItem(n).getValue();
        }
        return new AnimatableNumberListValue(target, numbers);
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatableNumberListValue animNumList =
                (AnimatableNumberListValue) val;
            if (animVal == null) {
                animVal = new AnimSVGNumberList();
            }
            animVal.setAnimatedValue(animNumList.getNumbers());
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
    public class BaseSVGNumberList extends AbstractSVGNumberList {
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
    protected class AnimSVGNumberList extends AbstractSVGNumberList {
        public AnimSVGNumberList() {
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
        public SVGNumber getItem(int index) throws DOMException {
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
                 "readonly.number.list", null);
        }
        public SVGNumber initialize(SVGNumber newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.number.list", null);
        }
        public SVGNumber insertItemBefore(SVGNumber newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.number.list", null);
        }
        public SVGNumber replaceItem(SVGNumber newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.number.list", null);
        }
        public SVGNumber removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.number.list", null);
        }
        public SVGNumber appendItem(SVGNumber newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.number.list", null);
        }
        protected void setAnimatedValue(float[] values) {
            int size = itemList.size();
            int i = 0;
            while (i < size && i < values.length) {
                SVGNumberItem n = (SVGNumberItem) itemList.get(i);
                n.value = values[i];
                i++;
            }
            while (i < values.length) {
                appendItemImpl(new SVGNumberItem(values[i]));
                i++;
            }
            while (size > values.length) {
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
