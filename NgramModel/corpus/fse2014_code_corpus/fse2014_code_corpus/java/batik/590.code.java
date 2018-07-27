package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFESpotLightElement;
public class SVGOMFESpotLightElement
    extends    SVGOMElement
    implements SVGFESpotLightElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_Z_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_POINTS_AT_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_POINTS_AT_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_POINTS_AT_Z_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_SPECULAR_EXPONENT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_LIMITING_CONE_ANGLE_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedNumber x;
    protected SVGOMAnimatedNumber y;
    protected SVGOMAnimatedNumber z;
    protected SVGOMAnimatedNumber pointsAtX;
    protected SVGOMAnimatedNumber pointsAtY;
    protected SVGOMAnimatedNumber pointsAtZ;
    protected SVGOMAnimatedNumber specularExponent;
    protected SVGOMAnimatedNumber limitingConeAngle;
    protected SVGOMFESpotLightElement() {
    }
    public SVGOMFESpotLightElement(String prefix,
                                   AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x = createLiveAnimatedNumber(null, SVG_X_ATTRIBUTE, 0f);
        y = createLiveAnimatedNumber(null, SVG_Y_ATTRIBUTE, 0f);
        z = createLiveAnimatedNumber(null, SVG_Z_ATTRIBUTE, 0f);
        pointsAtX =
            createLiveAnimatedNumber(null, SVG_POINTS_AT_X_ATTRIBUTE, 0f);
        pointsAtY =
            createLiveAnimatedNumber(null, SVG_POINTS_AT_Y_ATTRIBUTE, 0f);
        pointsAtZ =
            createLiveAnimatedNumber(null, SVG_POINTS_AT_Z_ATTRIBUTE, 0f);
        specularExponent =
            createLiveAnimatedNumber(null, SVG_SPECULAR_EXPONENT_ATTRIBUTE, 1f);
        limitingConeAngle =
            createLiveAnimatedNumber
                (null, SVG_LIMITING_CONE_ANGLE_ATTRIBUTE, 0f);
    }
    public String getLocalName() {
        return SVG_FE_SPOT_LIGHT_TAG;
    }
    public SVGAnimatedNumber getX() {
        return x;
    }
    public SVGAnimatedNumber getY() {
        return y;
    }
    public SVGAnimatedNumber getZ() {
        return z;
    }
    public SVGAnimatedNumber getPointsAtX() {
        return pointsAtX;
    }
    public SVGAnimatedNumber getPointsAtY() {
        return pointsAtY;
    }
    public SVGAnimatedNumber getPointsAtZ() {
        return pointsAtZ;
    }
    public SVGAnimatedNumber getSpecularExponent() {
        return specularExponent;
    }
    public SVGAnimatedNumber getLimitingConeAngle() {
        return limitingConeAngle;
    }
    protected Node newNode() {
        return new SVGOMFESpotLightElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
