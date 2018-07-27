package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFEPointLightElement;
public class SVGOMFEPointLightElement
    extends    SVGOMElement
    implements SVGFEPointLightElement {
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
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedNumber x;
    protected SVGOMAnimatedNumber y;
    protected SVGOMAnimatedNumber z;
    protected SVGOMFEPointLightElement() {
    }
    public SVGOMFEPointLightElement(String prefix,
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
    }
    public String getLocalName() {
        return SVG_FE_POINT_LIGHT_TAG;
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
    protected Node newNode() {
        return new SVGOMFEPointLightElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
