package org.apache.batik.dom.svg;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PointsHandler;
import org.apache.batik.parser.PointsParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;
public abstract class AbstractSVGPointList
    extends AbstractSVGList
    implements SVGPointList {
    public static final String SVG_POINT_LIST_SEPARATOR
        = " ";
    protected String getItemSeparator() {
        return SVG_POINT_LIST_SEPARATOR;
    }
    protected abstract SVGException createSVGException(short type,
                                                       String key,
                                                       Object[] args);
    public SVGPoint initialize(SVGPoint newItem)
            throws DOMException, SVGException {
        return (SVGPoint) initializeImpl(newItem);
    }
    public SVGPoint getItem(int index) throws DOMException {
        return (SVGPoint) getItemImpl(index);
    }
    public SVGPoint insertItemBefore(SVGPoint newItem, int index)
            throws DOMException, SVGException {
        return (SVGPoint) insertItemBeforeImpl(newItem,index);
    }
    public SVGPoint replaceItem(SVGPoint newItem, int index)
            throws DOMException, SVGException {
        return (SVGPoint) replaceItemImpl(newItem,index);
    }
    public SVGPoint removeItem(int index) throws DOMException {
        return (SVGPoint) removeItemImpl(index);
    }
    public SVGPoint appendItem(SVGPoint newItem)
            throws DOMException, SVGException {
        return (SVGPoint) appendItemImpl(newItem);
    }
    protected SVGItem createSVGItem(Object newItem) {
        SVGPoint point = (SVGPoint) newItem;
        return new SVGPointItem(point.getX(), point.getY());
    }
    protected void doParse(String value, ListHandler handler)
            throws ParseException {
        PointsParser pointsParser = new PointsParser();
        PointsListBuilder builder = new PointsListBuilder(handler);
        pointsParser.setPointsHandler(builder);
        pointsParser.parse(value);
    }
    protected void checkItemType(Object newItem) throws SVGException {
        if (!(newItem instanceof SVGPoint)) {
            createSVGException(SVGException.SVG_WRONG_TYPE_ERR,
                               "expected.point", null);
        }
    }
    protected class SVGPointItem extends AbstractSVGItem implements SVGPoint {
        protected float x;
        protected float y;
        public SVGPointItem(float x, float y) {
            this.x = x;
            this.y = y;
        }
        protected String getStringValue() {
            return Float.toString( x )
                    + ','
                    + Float.toString( y );
        }
        public float getX() {
            return x;
        }
        public float getY() {
            return y;
        }
        public void setX(float x) {
            this.x = x;
            resetAttribute();
        }
        public void setY(float y) {
            this.y = y;
            resetAttribute();
        }
        public SVGPoint matrixTransform(SVGMatrix matrix) {
            return SVGOMPoint.matrixTransform(this, matrix);
        }
    }
    protected class PointsListBuilder implements PointsHandler {
        protected ListHandler listHandler;
        public PointsListBuilder(ListHandler listHandler) {
            this.listHandler = listHandler;
        }
        public void startPoints() throws ParseException {
            listHandler.startList();
        }
        public void point(float x, float y) throws ParseException {
            listHandler.item(new SVGPointItem(x, y));
        }
        public void endPoints() throws ParseException {
            listHandler.endList();
        }
    }
}
