package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.svg.SVGAnimatedLengthList;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGTextPositioningElement;
public abstract class SVGOMTextPositioningElement
    extends    SVGOMTextContentElement
    implements SVGTextPositioningElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMTextContentElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH_LIST, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH_LIST, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_DX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH_LIST, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_DY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH_LIST, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_ROTATE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_LIST));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLengthList x;
    protected SVGOMAnimatedLengthList y;
    protected SVGOMAnimatedLengthList dx;
    protected SVGOMAnimatedLengthList dy;
    protected SVGOMAnimatedNumberList rotate;
    protected SVGOMTextPositioningElement() {
    }
    protected SVGOMTextPositioningElement(String prefix,
                                          AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x = createLiveAnimatedLengthList
            (null, SVG_X_ATTRIBUTE, getDefaultXValue(), true,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH);
        y = createLiveAnimatedLengthList
            (null, SVG_Y_ATTRIBUTE, getDefaultYValue(), true,
             SVGOMAnimatedLength.VERTICAL_LENGTH);
        dx = createLiveAnimatedLengthList
            (null, SVG_DX_ATTRIBUTE, "", true,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH);
        dy = createLiveAnimatedLengthList
            (null, SVG_DY_ATTRIBUTE, "", true,
             SVGOMAnimatedLength.VERTICAL_LENGTH);
        rotate =
            createLiveAnimatedNumberList(null, SVG_ROTATE_ATTRIBUTE, "", true);
    }
    public SVGAnimatedLengthList getX() {
        return x;
    }
    public SVGAnimatedLengthList getY() {
        return y;
    }
    public SVGAnimatedLengthList getDx() {
        return dx;
    }
    public SVGAnimatedLengthList getDy() {
        return dy;
    }
    public SVGAnimatedNumberList getRotate() {
        return rotate;
    }
    protected String getDefaultXValue() {
        return "";
    }
    protected String getDefaultYValue() {
        return "";
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
