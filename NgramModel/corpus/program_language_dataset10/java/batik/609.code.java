package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGLinearGradientElement;
public class SVGOMLinearGradientElement
    extends    SVGOMGradientElement
    implements SVGLinearGradientElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMGradientElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_WIDTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_HEIGHT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLength x1;
    protected SVGOMAnimatedLength y1;
    protected SVGOMAnimatedLength x2;
    protected SVGOMAnimatedLength y2;
    protected SVGOMLinearGradientElement() {
    }
    public SVGOMLinearGradientElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x1 = createLiveAnimatedLength
            (null, SVG_X1_ATTRIBUTE, SVG_LINEAR_GRADIENT_X1_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        y1 = createLiveAnimatedLength
            (null, SVG_Y1_ATTRIBUTE, SVG_LINEAR_GRADIENT_Y1_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        x2 = createLiveAnimatedLength
            (null, SVG_X2_ATTRIBUTE, SVG_LINEAR_GRADIENT_X2_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        y2 = createLiveAnimatedLength
            (null, SVG_Y2_ATTRIBUTE, SVG_LINEAR_GRADIENT_Y2_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
    }
    public String getLocalName() {
        return SVG_LINEAR_GRADIENT_TAG;
    }
    public SVGAnimatedLength getX1() {
        return x1;
    }
    public SVGAnimatedLength getY1() {
        return y1;
    }
    public SVGAnimatedLength getX2() {
        return x2;
    }
    public SVGAnimatedLength getY2() {
        return y2;
    }
    protected Node newNode() {
        return new SVGOMLinearGradientElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
