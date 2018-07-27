package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEColorMatrixElement;
public class SVGOMFEColorMatrixElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEColorMatrixElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_TYPE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_VALUES_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_LIST));
        xmlTraitInformation = t;
    }
    protected static final String[] TYPE_VALUES = {
        "",
        SVG_MATRIX_VALUE,
        SVG_SATURATE_VALUE,
        SVG_HUE_ROTATE_VALUE,
        SVG_LUMINANCE_TO_ALPHA_VALUE
    };
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedEnumeration type;
    protected SVGOMFEColorMatrixElement() {
    }
    public SVGOMFEColorMatrixElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        in = createLiveAnimatedString(null, SVG_IN_ATTRIBUTE);
        type =
            createLiveAnimatedEnumeration
                (null, SVG_TYPE_ATTRIBUTE, TYPE_VALUES, (short) 1);
    }
    public String getLocalName() {
        return SVG_FE_COLOR_MATRIX_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedEnumeration getType() {
        return type;
    }
    public SVGAnimatedNumberList getValues() {
        throw new UnsupportedOperationException
            ("SVGFEColorMatrixElement.getValues is not implemented"); 
    }
    protected Node newNode() {
        return new SVGOMFEColorMatrixElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
