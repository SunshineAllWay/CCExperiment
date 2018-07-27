package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEDisplacementMapElement;
public class SVGOMFEDisplacementMapElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEDisplacementMapElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_IN2_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_SCALE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_X_CHANNEL_SELECTOR_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_Y_CHANNEL_SELECTOR_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        xmlTraitInformation = t;
    }
    protected static final String[] CHANNEL_SELECTOR_VALUES = {
        "",
        SVG_R_VALUE,
        SVG_G_VALUE,
        SVG_B_VALUE,
        SVG_A_VALUE
    };
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedString in2;
    protected SVGOMAnimatedNumber scale;
    protected SVGOMAnimatedEnumeration xChannelSelector;
    protected SVGOMAnimatedEnumeration yChannelSelector;
    protected SVGOMFEDisplacementMapElement() {
    }
    public SVGOMFEDisplacementMapElement(String prefix,
                                         AbstractDocument owner) {
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
        scale = createLiveAnimatedNumber(null, SVG_SCALE_ATTRIBUTE, 0f);
        xChannelSelector =
            createLiveAnimatedEnumeration
                (null, SVG_X_CHANNEL_SELECTOR_ATTRIBUTE,
                 CHANNEL_SELECTOR_VALUES, (short) 4);
        yChannelSelector =
            createLiveAnimatedEnumeration
                (null, SVG_Y_CHANNEL_SELECTOR_ATTRIBUTE,
                 CHANNEL_SELECTOR_VALUES, (short) 4);
    }
    public String getLocalName() {
        return SVG_FE_DISPLACEMENT_MAP_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedString getIn2() {
        return in2;
    }
    public SVGAnimatedNumber getScale() {
        return scale;
    }
    public SVGAnimatedEnumeration getXChannelSelector() {
        return xChannelSelector;
    }
    public SVGAnimatedEnumeration getYChannelSelector() {
        return yChannelSelector;
    }
    protected Node newNode() {
        return new SVGOMFEDisplacementMapElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
