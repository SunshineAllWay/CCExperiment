package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFESpecularLightingElement;
public class SVGOMFESpecularLightingElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFESpecularLightingElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_SURFACE_SCALE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_SPECULAR_CONSTANT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_SPECULAR_EXPONENT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedString in;
    protected SVGOMAnimatedNumber surfaceScale;
    protected SVGOMAnimatedNumber specularConstant;
    protected SVGOMAnimatedNumber specularExponent;
    protected SVGOMFESpecularLightingElement() {
    }
    public SVGOMFESpecularLightingElement(String prefix,
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
        specularConstant =
            createLiveAnimatedNumber(null, SVG_SPECULAR_CONSTANT_ATTRIBUTE, 1f);
        specularExponent =
            createLiveAnimatedNumber(null, SVG_SPECULAR_EXPONENT_ATTRIBUTE, 1f);
    }
    public String getLocalName() {
        return SVG_FE_SPECULAR_LIGHTING_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedNumber getSurfaceScale() {
        return surfaceScale;
    }
    public SVGAnimatedNumber getSpecularConstant() {
        return specularConstant;
    }
    public SVGAnimatedNumber getSpecularExponent() {
        return specularExponent;
    }
    protected Node newNode() {
        return new SVGOMFESpecularLightingElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
