package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGCircleElement;
public class SVGOMCircleElement
    extends    SVGGraphicsElement
    implements SVGCircleElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_CX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, TraitInformation.PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_CY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, TraitInformation.PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_R_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, TraitInformation.PERCENTAGE_VIEWPORT_SIZE));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLength cx;
    protected SVGOMAnimatedLength cy;
    protected SVGOMAnimatedLength r;
    protected SVGOMCircleElement() {
    }
    public SVGOMCircleElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        cx = createLiveAnimatedLength
            (null, SVG_CX_ATTRIBUTE, SVG_CIRCLE_CX_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        cy = createLiveAnimatedLength
            (null, SVG_CY_ATTRIBUTE, SVG_CIRCLE_CY_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        r = createLiveAnimatedLength
            (null, SVG_R_ATTRIBUTE, null, SVGOMAnimatedLength.OTHER_LENGTH,
             true);
    }
    public String getLocalName() {
        return SVG_CIRCLE_TAG;
    }
    public SVGAnimatedLength getCx() {
        return cx;
    }
    public SVGAnimatedLength getCy() {
        return cy;
    }
    public SVGAnimatedLength getR() {
        return r;
    }
    protected Node newNode() {
        return new SVGOMCircleElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
