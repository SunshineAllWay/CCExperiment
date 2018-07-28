package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFEDistantLightElement;
public class SVGOMFEDistantLightElement
    extends    SVGOMElement
    implements SVGFEDistantLightElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
        t.put(null, SVG_AZIMUTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        t.put(null, SVG_ELEVATION_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedNumber azimuth;
    protected SVGOMAnimatedNumber elevation;
    protected SVGOMFEDistantLightElement() {
    }
    public SVGOMFEDistantLightElement(String prefix,
                                      AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        azimuth = createLiveAnimatedNumber(null, SVG_AZIMUTH_ATTRIBUTE, 0f);
        elevation = createLiveAnimatedNumber(null, SVG_ELEVATION_ATTRIBUTE, 0f);
    }
    public String getLocalName() {
        return SVG_FE_DISTANT_LIGHT_TAG;
    }
    public SVGAnimatedNumber getAzimuth() {
        return azimuth;
    }
    public SVGAnimatedNumber getElevation() {
        return elevation;
    }
    protected Node newNode() {
        return new SVGOMFEDistantLightElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
