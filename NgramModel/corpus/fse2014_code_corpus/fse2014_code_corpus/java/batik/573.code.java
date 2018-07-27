package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedInteger;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEConvolveMatrixElement;
public class SVGOMFEConvolveMatrixElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEConvolveMatrixElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_ORDER_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OPTIONAL_NUMBER));
        t.put(null, SVG_KERNEL_UNIT_LENGTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OPTIONAL_NUMBER));
        t.put(null, SVG_KERNEL_MATRIX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_LIST));
        t.put(null, SVG_DIVISOR_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_BIAS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_TARGET_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_INTEGER));
        t.put(null, SVG_TARGET_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_INTEGER));
        t.put(null, SVG_EDGE_MODE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_PRESERVE_ALPHA_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        xmlTraitInformation = t;
    }
    protected static final String[] EDGE_MODE_VALUES = {
        "",
        SVG_DUPLICATE_VALUE,
        SVG_WRAP_VALUE,
        SVG_NONE_VALUE
    };
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedEnumeration edgeMode;
    protected SVGOMAnimatedNumber bias;
    protected SVGOMAnimatedBoolean preserveAlpha;
    protected SVGOMFEConvolveMatrixElement() {
    }
    public SVGOMFEConvolveMatrixElement(String prefix,
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
        edgeMode =
            createLiveAnimatedEnumeration
                (null, SVG_EDGE_MODE_ATTRIBUTE, EDGE_MODE_VALUES, (short) 1);
        bias = createLiveAnimatedNumber(null, SVG_BIAS_ATTRIBUTE, 0f);
        preserveAlpha =
            createLiveAnimatedBoolean
                (null, SVG_PRESERVE_ALPHA_ATTRIBUTE, false);
    }
    public String getLocalName() {
        return SVG_FE_CONVOLVE_MATRIX_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedEnumeration getEdgeMode() {
        return edgeMode;
    }
    public SVGAnimatedNumberList getKernelMatrix() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getKernelMatrix is not implemented"); 
    }
    public SVGAnimatedInteger getOrderX() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getOrderX is not implemented"); 
    }
    public SVGAnimatedInteger getOrderY() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getOrderY is not implemented"); 
    }
    public SVGAnimatedInteger getTargetX() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getTargetX is not implemented"); 
    }
    public SVGAnimatedInteger getTargetY() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getTargetY is not implemented"); 
    }
    public SVGAnimatedNumber getDivisor() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getDivisor is not implemented"); 
    }
    public SVGAnimatedNumber getBias() {
        return bias;
    }
    public SVGAnimatedNumber getKernelUnitLengthX() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getKernelUnitLengthX is not implemented"); 
    }
    public SVGAnimatedNumber getKernelUnitLengthY() {
        throw new UnsupportedOperationException
            ("SVGFEConvolveMatrixElement.getKernelUnitLengthY is not implemented"); 
    }
    public SVGAnimatedBoolean getPreserveAlpha() {
        return preserveAlpha;
    }
    protected Node newNode() {
        return new SVGOMFEConvolveMatrixElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
