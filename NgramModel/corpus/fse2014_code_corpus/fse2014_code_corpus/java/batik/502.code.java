package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
public abstract class AbstractSVGList {
    protected boolean valid;
    protected List itemList;
    protected abstract String getItemSeparator();
    protected abstract SVGItem createSVGItem(Object newItem);
    protected abstract void doParse(String value, ListHandler builder)
        throws ParseException;
    protected abstract void checkItemType(Object newItem)
        throws SVGException;
    protected abstract String getValueAsString();
    protected abstract void setAttributeValue(String value);
    protected abstract DOMException createDOMException(short type, String key,
                                                       Object[] args);
    public int getNumberOfItems() {
        revalidate();
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }
    public void clear() throws DOMException {
        revalidate();
        if (itemList != null) {
            clear(itemList);
            resetAttribute();
        }
    }
    protected SVGItem initializeImpl(Object newItem)
        throws DOMException, SVGException {
        checkItemType(newItem);
        if (itemList == null) {
            itemList = new ArrayList(1);
        } else {
            clear(itemList);
        }
        SVGItem item = removeIfNeeded(newItem);
        itemList.add(item);
        item.setParent(this);
        resetAttribute();
        return item;
    }
    protected SVGItem getItemImpl(int index) throws DOMException {
        revalidate();
        if (index < 0 || itemList == null || index >= itemList.size()) {
            throw createDOMException
                (DOMException.INDEX_SIZE_ERR, "index.out.of.bounds",
                 new Object[] { new Integer(index) } );
        }
        return (SVGItem)itemList.get(index);
    }
    protected SVGItem insertItemBeforeImpl(Object newItem, int index)
        throws DOMException, SVGException {
        checkItemType(newItem);
        revalidate();
        if (index < 0) {
            throw createDOMException
                (DOMException.INDEX_SIZE_ERR, "index.out.of.bounds",
                 new Object[] { new Integer(index) } );
        }
        if (index > itemList.size()) {
            index = itemList.size();
        }
        SVGItem item = removeIfNeeded(newItem);
        itemList.add(index, item);
        item.setParent(this);
        resetAttribute();
        return item;
    }
    protected SVGItem replaceItemImpl(Object newItem, int index)
        throws DOMException, SVGException {
        checkItemType(newItem);
        revalidate();
        if (index < 0 || index >= itemList.size()) {
            throw createDOMException
                (DOMException.INDEX_SIZE_ERR, "index.out.of.bounds",
                 new Object[] { new Integer(index) } );
        }
        SVGItem item = removeIfNeeded(newItem);
        itemList.set(index, item);
        item.setParent(this);
        resetAttribute();
        return item;
    }
    protected SVGItem removeItemImpl(int index) throws DOMException {
        revalidate();
        if (index < 0 || index >= itemList.size()) {
            throw createDOMException
                (DOMException.INDEX_SIZE_ERR, "index.out.of.bounds",
                 new Object[] { new Integer(index) } );
        }
        SVGItem item = (SVGItem)itemList.remove(index);
        item.setParent(null);
        resetAttribute();
        return item;
    }
    protected SVGItem appendItemImpl(Object newItem)
        throws DOMException, SVGException {
        checkItemType(newItem);
        revalidate();
        SVGItem item = removeIfNeeded(newItem);
        itemList.add(item);
        item.setParent(this);
        if (itemList.size() <= 1) {
            resetAttribute();
        } else {
            resetAttribute(item);
        }
        return item;
    }
    protected SVGItem removeIfNeeded(Object newItem) {
        SVGItem item;
        if (newItem instanceof SVGItem) {
            item = (SVGItem)newItem;
            if (item.getParent() != null) {
                item.getParent().removeItem(item);
            }
        } else {
            item = createSVGItem(newItem);
        }
        return item;
    }
    protected void revalidate() {
        if (valid) {
            return;
        }
        try {
            ListBuilder builder = new ListBuilder();
            doParse(getValueAsString(), builder);
            List parsedList = builder.getList();
            if (parsedList != null) {
                clear(itemList);
            }
            itemList = parsedList;
        } catch (ParseException e) {
            itemList = null;
        }
        valid = true;
    }
    protected void setValueAsString(List value) throws DOMException {
        String finalValue = null;
        Iterator it = value.iterator();
        if (it.hasNext()) {
            SVGItem item = (SVGItem) it.next();
            StringBuffer buf = new StringBuffer( value.size() * 8 );
            buf.append(  item.getValueAsString() );
            while (it.hasNext()) {
                item = (SVGItem) it.next();
                buf.append(getItemSeparator());
                buf.append(item.getValueAsString());
            }
            finalValue = buf.toString();
        }
        setAttributeValue(finalValue);
        valid = true;
    }
    public void itemChanged() {
        resetAttribute();
    }
    protected void resetAttribute() {
        setValueAsString(itemList);
    }
    protected void resetAttribute(SVGItem item) {
        String newValue = getValueAsString() + getItemSeparator() + item.getValueAsString();
        setAttributeValue( newValue );
        valid = true;
    }
    public void invalidate() {
        valid = false;
    }
    protected void removeItem(SVGItem item) {
        if (itemList.contains(item)) {
            itemList.remove(item);
            item.setParent(null);
            resetAttribute();
        }
    }
    protected void clear(List list) {
        if (list == null) {
            return;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SVGItem item = (SVGItem)it.next();
            item.setParent(null);
        }
        list.clear();
    }
    protected class ListBuilder implements ListHandler {
        protected List list;
        public List getList() {
            return list;
        }
        public void startList(){
            list = new ArrayList();
        }
        public void item(SVGItem item) {
            item.setParent(AbstractSVGList.this);
            list.add(item);
        }
        public void endList() {
        }
    }
}
