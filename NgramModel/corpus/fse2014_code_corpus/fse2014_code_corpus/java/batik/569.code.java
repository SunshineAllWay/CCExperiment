package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEBlendElement;
public class SVGOMFEBlendElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEBlendElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_SURFACE_SCALE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_DIFFUSE_CONSTANT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_KERNEL_UNIT_LENGTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OPTIONAL_NUMBER));
        xmlTraitInformation = t;
    }
    protected static final String[] MODE_VALUES = {
        "",
        SVG_NORMAL_VALUE,
        SVG_MULTIPLY_VALUE,
        SVG_SCREEN_VALUE,
        SVG_DARKEN_VALUE,
        SVG_LIGHTEN_VALUE
    };
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedString in2;
    protected SVGOMAnimatedEnumeration mode;
    protected SVGOMFEBlendElement() {
    }
    public SVGOMFEBlendElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        in = createLiveAnimatedString(null, SVG_IN_ATTRIBUTE);
        in2 = createLiveAnimatedString(null, SVG_IN2_ATTRIBUTE);
        mode =
            createLiveAnimatedEnumeration
                (null, SVG_MODE_ATTRIBUTE, MODE_VALUES, (short) 1);
    }
    public String getLocalName() {
        return SVG_FE_BLEND_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedString getIn2() {
        return in2;
    }
    public SVGAnimatedEnumeration getMode() {
        return mode;
    }
    protected Node newNode() {
        return new SVGOMFEBlendElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
