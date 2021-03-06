package org.apache.batik.dom.svg;
import org.apache.batik.parser.LengthListHandler;
import org.apache.batik.parser.LengthListParser;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGLengthList;
public abstract class AbstractSVGLengthList
    extends AbstractSVGList
    implements SVGLengthList {
    protected short direction;
    public static final String SVG_LENGTH_LIST_SEPARATOR
        = " ";
    protected String getItemSeparator() {
        return SVG_LENGTH_LIST_SEPARATOR;
    }
    protected abstract SVGException createSVGException(short type,
                                                       String key,
                                                       Object[] args);
    protected abstract Element getElement();
    protected AbstractSVGLengthList(short direction) {
        this.direction = direction;
    }
    public SVGLength initialize(SVGLength newItem)
            throws DOMException, SVGException {
        return (SVGLength) initializeImpl(newItem);
    }
    public SVGLength getItem(int index) throws DOMException {
        return (SVGLength) getItemImpl(index);
    }
    public SVGLength insertItemBefore(SVGLength newItem, int index)
            throws DOMException, SVGException {
        return (SVGLength) insertItemBeforeImpl(newItem, index);
    }
    public SVGLength replaceItem(SVGLength newItem, int index)
            throws DOMException, SVGException {
        return (SVGLength) replaceItemImpl(newItem,index);
    }
    public SVGLength removeItem(int index) throws DOMException {
        return (SVGLength) removeItemImpl(index);
    }
    public SVGLength appendItem(SVGLength newItem)
            throws DOMException, SVGException {
        return (SVGLength) appendItemImpl(newItem);
    }
    protected SVGItem createSVGItem(Object newItem) {
        SVGLength l = (SVGLength) newItem;
        return new SVGLengthItem(l.getUnitType(), l.getValueInSpecifiedUnits(),
                                 direction);
    }
    protected void doParse(String value, ListHandler handler)
        throws ParseException{
        LengthListParser lengthListParser = new LengthListParser();
        LengthListBuilder builder = new LengthListBuilder(handler);
        lengthListParser.setLengthListHandler(builder);
        lengthListParser.parse(value);
    }
    protected void checkItemType(Object newItem) throws SVGException {
        if (!(newItem instanceof SVGLength)) {
            createSVGException(SVGException.SVG_WRONG_TYPE_ERR,
                               "expected.length", null);
        }
    }
    protected class SVGLengthItem extends AbstractSVGLength implements SVGItem {
        public SVGLengthItem(short type, float value, short direction) {
            super(direction);
            this.unitType = type;
            this.value = value;
        }
        protected SVGOMElement getAssociatedElement() {
            return (SVGOMElement) AbstractSVGLengthList.this.getElement();
        }
        protected AbstractSVGList parentList;
        public void setParent(AbstractSVGList list) {
            parentList = list;
        }
        public AbstractSVGList getParent() {
            return parentList;
        }
        protected void reset() {
            if (parentList != null) {
                parentList.itemChanged();
            }
        }
    }
    protected class LengthListBuilder implements LengthListHandler {
        protected ListHandler listHandler;
        protected float currentValue;
        protected short currentType;
        public LengthListBuilder(ListHandler listHandler) {
            this.listHandler = listHandler;
        }
        public void startLengthList() throws ParseException {
            listHandler.startList();
        }
        public void startLength() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_NUMBER;
            currentValue = 0.0f;
        }
        public void lengthValue(float v) throws ParseException {
            currentValue = v;
        }
        public void em() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_EMS;
        }
        public void ex() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_EXS;
        }
        public void in() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_IN;
        }
        public void cm() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_CM;
        }
        public void mm() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_MM;
        }
        public void pc() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_PC;
        }
        public void pt() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_EMS;
        }
        public void px() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_PX;
        }
        public void percentage() throws ParseException {
            currentType = SVGLength.SVG_LENGTHTYPE_PERCENTAGE;
        }
        public void endLength() throws ParseException {
            listHandler.item
                (new SVGLengthItem(currentType,currentValue,direction));
        }
        public void endLengthList() throws ParseException {
            listHandler.endList();
        }
    }
}
