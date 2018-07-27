package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGMaskElement;
public class SVGOMMaskElement
    extends    SVGGraphicsElement
    implements SVGMaskElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_WIDTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_HEIGHT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_MASK_UNITS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_MASK_CONTENT_UNITS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        xmlTraitInformation = t;
    }
    protected static final String[] UNITS_VALUES = {
        "",
        SVG_USER_SPACE_ON_USE_VALUE,
        SVG_OBJECT_BOUNDING_BOX_VALUE
    };
    protected SVGOMAnimatedLength x;
    protected SVGOMAnimatedLength y;
    protected SVGOMAnimatedLength width;
    protected SVGOMAnimatedLength height;
    protected SVGOMAnimatedEnumeration maskUnits;
    protected SVGOMAnimatedEnumeration maskContentUnits;
    protected SVGOMMaskElement() {
    }
    public SVGOMMaskElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x = createLiveAnimatedLength
            (null, SVG_X_ATTRIBUTE, SVG_MASK_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        y = createLiveAnimatedLength
            (null, SVG_Y_ATTRIBUTE, SVG_MASK_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        width =
            createLiveAnimatedLength
                (null, SVG_WIDTH_ATTRIBUTE, SVG_MASK_WIDTH_DEFAULT_VALUE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
        height =
            createLiveAnimatedLength
                (null, SVG_HEIGHT_ATTRIBUTE, SVG_MASK_WIDTH_DEFAULT_VALUE,
                 SVGOMAnimatedLength.VERTICAL_LENGTH, true);
        maskUnits =
            createLiveAnimatedEnumeration
                (null, SVG_MASK_UNITS_ATTRIBUTE, UNITS_VALUES, (short) 2);
        maskContentUnits =
            createLiveAnimatedEnumeration
                (null, SVG_MASK_CONTENT_UNITS_ATTRIBUTE, UNITS_VALUES,
                 (short) 1);
    }
    public String getLocalName() {
        return SVG_MASK_TAG;
    }
    public SVGAnimatedEnumeration getMaskUnits() {
        return maskUnits;
    }
    public SVGAnimatedEnumeration getMaskContentUnits() {
        return maskContentUnits;
    }
    public SVGAnimatedLength getX() {
        return x;
    }
    public SVGAnimatedLength getY() {
        return y;
    }
    public SVGAnimatedLength getWidth() {
        return width;
    }
    public SVGAnimatedLength getHeight() {
        return height;
    }
    protected Node newNode() {
        return new SVGOMMaskElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
