package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGEllipseElement;
public class SVGOMEllipseElement
    extends    SVGGraphicsElement
    implements SVGEllipseElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_CX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_CY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_RX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_RY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLength cx;
    protected SVGOMAnimatedLength cy;
    protected SVGOMAnimatedLength rx;
    protected SVGOMAnimatedLength ry;
    protected SVGOMEllipseElement() {
    }
    public SVGOMEllipseElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        cx = createLiveAnimatedLength
            (null, SVG_CX_ATTRIBUTE, SVG_ELLIPSE_CX_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        cy = createLiveAnimatedLength
            (null, SVG_CY_ATTRIBUTE, SVG_ELLIPSE_CY_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        rx = createLiveAnimatedLength
            (null, SVG_RX_ATTRIBUTE, null,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
        ry = createLiveAnimatedLength
            (null, SVG_RY_ATTRIBUTE, null, SVGOMAnimatedLength.VERTICAL_LENGTH,
             true);
    }
    public String getLocalName() {
        return SVG_ELLIPSE_TAG;
    }
    public SVGAnimatedLength getCx() {
        return cx;
    }
    public SVGAnimatedLength getCy() {
        return cy;
    }
    public SVGAnimatedLength getRx() {
        return rx;
    }
    public SVGAnimatedLength getRy() {
        return ry;
   }
    protected Node newNode() {
        return new SVGOMEllipseElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
