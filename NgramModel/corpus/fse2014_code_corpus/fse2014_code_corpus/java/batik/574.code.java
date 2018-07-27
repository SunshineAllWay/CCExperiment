package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEDiffuseLightingElement;
public class SVGOMFEDiffuseLightingElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEDiffuseLightingElement {
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
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedNumber surfaceScale;
    protected SVGOMAnimatedNumber diffuseConstant;
    protected SVGOMFEDiffuseLightingElement() {
    }
    public SVGOMFEDiffuseLightingElement(String prefix,
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
        surfaceScale =
            createLiveAnimatedNumber(null, SVG_SURFACE_SCALE_ATTRIBUTE, 1f);
        diffuseConstant =
            createLiveAnimatedNumber(null, SVG_DIFFUSE_CONSTANT_ATTRIBUTE, 1f);
    }
    public String getLocalName() {
        return SVG_FE_DIFFUSE_LIGHTING_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedNumber getSurfaceScale() {
        return surfaceScale;
    }
    public SVGAnimatedNumber getDiffuseConstant() {
        return diffuseConstant;
    }
    public SVGAnimatedNumber getKernelUnitLengthX() {
        throw new UnsupportedOperationException
            ("SVGFEDiffuseLightingElement.getKernelUnitLengthX is not implemented"); 
    }
    public SVGAnimatedNumber getKernelUnitLengthY() {
        throw new UnsupportedOperationException
            ("SVGFEDiffuseLightingElement.getKernelUnitLengthY is not implemented"); 
    }
    protected Node newNode() {
        return new SVGOMFEDiffuseLightingElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
