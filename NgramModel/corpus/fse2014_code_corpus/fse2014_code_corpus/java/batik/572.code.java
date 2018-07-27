package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFECompositeElement;
public class SVGOMFECompositeElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFECompositeElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_IN2_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_OPERATOR_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_K1_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_K2_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_K3_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_K4_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected static final String[] OPERATOR_VALUES = {
        "",
        SVG_OVER_VALUE,
        SVG_IN_VALUE,
        SVG_OUT_VALUE,
        SVG_ATOP_VALUE,
        SVG_XOR_VALUE,
        SVG_ARITHMETIC_VALUE
    };
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedString in2;
    protected SVGOMAnimatedEnumeration operator;
    protected SVGOMAnimatedNumber k1;
    protected SVGOMAnimatedNumber k2;
    protected SVGOMAnimatedNumber k3;
    protected SVGOMAnimatedNumber k4;
    protected SVGOMFECompositeElement() {
    }
    public SVGOMFECompositeElement(String prefix, AbstractDocument owner) {
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
        operator =
            createLiveAnimatedEnumeration
                (null, SVG_OPERATOR_ATTRIBUTE, OPERATOR_VALUES, (short) 1);
        k1 = createLiveAnimatedNumber(null, SVG_K1_ATTRIBUTE, 0f);
        k2 = createLiveAnimatedNumber(null, SVG_K2_ATTRIBUTE, 0f);
        k3 = createLiveAnimatedNumber(null, SVG_K3_ATTRIBUTE, 0f);
        k4 = createLiveAnimatedNumber(null, SVG_K4_ATTRIBUTE, 0f);
    }
    public String getLocalName() {
        return SVG_FE_COMPOSITE_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedString getIn2() {
        return in2;
    }
    public SVGAnimatedEnumeration getOperator() {
        return operator;
    }
    public SVGAnimatedNumber getK1() {
        return k1;
    }
    public SVGAnimatedNumber getK2() {
        return k2;
    }
    public SVGAnimatedNumber getK3() {
        return k3;
    }
    public SVGAnimatedNumber getK4() {
        return k4;
    }
    protected Node newNode() {
        return new SVGOMFECompositeElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
