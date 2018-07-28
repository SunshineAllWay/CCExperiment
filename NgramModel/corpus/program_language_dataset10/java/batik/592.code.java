package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedInteger;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFETurbulenceElement;
public class SVGOMFETurbulenceElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFETurbulenceElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_BASE_FREQUENCY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OPTIONAL_NUMBER));
        t.put(null, SVG_NUM_OCTAVES_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_INTEGER));
        t.put(null, SVG_SEED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_STITCH_TILES_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_TYPE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        xmlTraitInformation = t;
    }
    protected static final String[] STITCH_TILES_VALUES = {
        "",
        SVG_STITCH_VALUE,
        SVG_NO_STITCH_VALUE
    };
    protected static final String[] TYPE_VALUES = {
        "",
        SVG_FRACTAL_NOISE_VALUE,
        SVG_TURBULENCE_VALUE
    };
    protected SVGOMAnimatedInteger numOctaves;
    protected SVGOMAnimatedNumber seed;
    protected SVGOMAnimatedEnumeration stitchTiles;
    protected SVGOMAnimatedEnumeration type;
    protected SVGOMFETurbulenceElement() {
    }
    public SVGOMFETurbulenceElement(String prefix,
                                    AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        numOctaves =
            createLiveAnimatedInteger(null, SVG_NUM_OCTAVES_ATTRIBUTE, 1);
        seed = createLiveAnimatedNumber(null, SVG_SEED_ATTRIBUTE, 0f);
        stitchTiles =
            createLiveAnimatedEnumeration
                (null, SVG_STITCH_TILES_ATTRIBUTE, STITCH_TILES_VALUES,
                 (short) 2);
        type =
            createLiveAnimatedEnumeration
                (null, SVG_TYPE_ATTRIBUTE, TYPE_VALUES, (short) 2);
    }
    public String getLocalName() {
        return SVG_FE_TURBULENCE_TAG;
    }
    public SVGAnimatedNumber getBaseFrequencyX() {
        throw new UnsupportedOperationException
            ("SVGFETurbulenceElement.getBaseFrequencyX is not implemented"); 
    }
    public SVGAnimatedNumber getBaseFrequencyY() {
        throw new UnsupportedOperationException
            ("SVGFETurbulenceElement.getBaseFrequencyY is not implemented"); 
    }
    public SVGAnimatedInteger getNumOctaves() {
        return numOctaves;
    }
    public SVGAnimatedNumber getSeed() {
        return seed;
    }
    public SVGAnimatedEnumeration getStitchTiles() {
        return stitchTiles;
    }
    public SVGAnimatedEnumeration getType() {
        return type;
    }
    protected Node newNode() {
        return new SVGOMFETurbulenceElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
