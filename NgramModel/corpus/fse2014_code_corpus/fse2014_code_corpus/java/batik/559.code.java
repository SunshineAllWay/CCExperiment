package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGComponentTransferFunctionElement;
public abstract class SVGOMComponentTransferFunctionElement
    extends    SVGOMElement
    implements SVGComponentTransferFunctionElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
        t.put(null, SVG_TYPE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_TABLE_VALUES_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_LIST));
        t.put(null, SVG_SLOPE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_INTERCEPT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_AMPLITUDE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_EXPONENT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_OFFSET_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected static final String[] TYPE_VALUES = {
        "",
        SVG_IDENTITY_VALUE,
        SVG_TABLE_VALUE,
        SVG_DISCRETE_VALUE,
        SVG_LINEAR_VALUE,
        SVG_GAMMA_VALUE
    };
    protected SVGOMAnimatedEnumeration type;
    protected SVGOMAnimatedNumberList tableValues;
    protected SVGOMAnimatedNumber slope;
    protected SVGOMAnimatedNumber intercept;
    protected SVGOMAnimatedNumber amplitude;
    protected SVGOMAnimatedNumber exponent;
    protected SVGOMAnimatedNumber offset;
    protected SVGOMComponentTransferFunctionElement() {
    }
    protected SVGOMComponentTransferFunctionElement(String prefix,
                                                    AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        type =
            createLiveAnimatedEnumeration
                (null, SVG_TYPE_ATTRIBUTE, TYPE_VALUES, (short) 1);
        tableValues =
            createLiveAnimatedNumberList
                (null, SVG_TABLE_VALUES_ATTRIBUTE,
                 SVG_COMPONENT_TRANSFER_FUNCTION_TABLE_VALUES_DEFAULT_VALUE,
                 false);
        slope = createLiveAnimatedNumber(null, SVG_SLOPE_ATTRIBUTE, 1f);
        intercept = createLiveAnimatedNumber(null, SVG_INTERCEPT_ATTRIBUTE, 0f);
        amplitude = createLiveAnimatedNumber(null, SVG_AMPLITUDE_ATTRIBUTE, 1f);
        exponent = createLiveAnimatedNumber(null, SVG_EXPONENT_ATTRIBUTE, 1f);
        offset = createLiveAnimatedNumber(null, SVG_EXPONENT_ATTRIBUTE, 0f);
    }
    public SVGAnimatedEnumeration getType() {
        return type;
    }
    public SVGAnimatedNumberList getTableValues() {
        throw new UnsupportedOperationException
            ("SVGComponentTransferFunctionElement.getTableValues is not implemented");
    }
    public SVGAnimatedNumber getSlope() {
        return slope;
    }
    public SVGAnimatedNumber getIntercept() {
        return intercept;
    }
    public SVGAnimatedNumber getAmplitude() {
        return amplitude;
    }
    public SVGAnimatedNumber getExponent() {
        return exponent;
    }
    public SVGAnimatedNumber getOffset() {
        return offset;
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
