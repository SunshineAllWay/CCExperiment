package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGAnimatedAngle;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGMarkerElement;
public class SVGOMMarkerElement
    extends    SVGStylableElement
    implements SVGMarkerElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
        t.put(null, SVG_REF_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_REF_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_MARKER_WIDTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_MARKER_HEIGHT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_MARKER_UNITS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_ORIENT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_PRESERVE_ASPECT_RATIO_VALUE));
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        xmlTraitInformation = t;
    }
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(1);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                          "xMidYMid meet");
    }
    protected static final String[] UNITS_VALUES = {
        "",
        SVG_USER_SPACE_ON_USE_VALUE,
        SVG_STROKE_WIDTH_ATTRIBUTE
    };
    protected static final String[] ORIENT_TYPE_VALUES = {
        "",
        SVG_AUTO_VALUE,
        ""
    };
    protected SVGOMAnimatedLength refX;
    protected SVGOMAnimatedLength refY;
    protected SVGOMAnimatedLength markerWidth;
    protected SVGOMAnimatedLength markerHeight;
    protected SVGOMAnimatedMarkerOrientValue orient;
    protected SVGOMAnimatedEnumeration markerUnits;
    protected SVGOMAnimatedPreserveAspectRatio preserveAspectRatio;
    protected SVGOMAnimatedRect viewBox;
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected SVGOMMarkerElement() {
    }
    public SVGOMMarkerElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        refX =
            createLiveAnimatedLength
                (null, SVG_REF_X_ATTRIBUTE, SVG_MARKER_REF_X_DEFAULT_VALUE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        refY =
            createLiveAnimatedLength
                (null, SVG_REF_Y_ATTRIBUTE, SVG_MARKER_REF_Y_DEFAULT_VALUE,
                 SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        markerWidth =
            createLiveAnimatedLength
                (null, SVG_MARKER_WIDTH_ATTRIBUTE,
                 SVG_MARKER_MARKER_WIDTH_DEFAULT_VALUE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
        markerHeight =
            createLiveAnimatedLength
                (null, SVG_MARKER_HEIGHT_ATTRIBUTE,
                 SVG_MARKER_MARKER_WIDTH_DEFAULT_VALUE,
                 SVGOMAnimatedLength.VERTICAL_LENGTH, true);
        orient =
            createLiveAnimatedMarkerOrientValue(null, SVG_ORIENT_ATTRIBUTE);
        markerUnits =
            createLiveAnimatedEnumeration
                (null, SVG_MARKER_UNITS_ATTRIBUTE, UNITS_VALUES, (short) 2);
        preserveAspectRatio =
            createLiveAnimatedPreserveAspectRatio();
        viewBox = createLiveAnimatedRect(null, SVG_VIEW_BOX_ATTRIBUTE, null);
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
    }
    public String getLocalName() {
        return SVG_MARKER_TAG;
    }
    public SVGAnimatedLength getRefX() {
        return refX;
    }
    public SVGAnimatedLength getRefY() {
        return refY;
    }
    public SVGAnimatedEnumeration getMarkerUnits() {
        return markerUnits;
    }
    public SVGAnimatedLength getMarkerWidth() {
        return markerWidth;
    }
    public SVGAnimatedLength getMarkerHeight() {
        return markerHeight;
    }
    public SVGAnimatedEnumeration getOrientType() {
        return orient.getAnimatedEnumeration();
    }
    public SVGAnimatedAngle getOrientAngle() {
        return orient.getAnimatedAngle();
    }
    public void setOrientToAuto() {
        setAttributeNS(null, SVG_ORIENT_ATTRIBUTE, SVG_AUTO_VALUE);
    }
    public void setOrientToAngle(SVGAngle angle) {
        setAttributeNS(null, SVG_ORIENT_ATTRIBUTE, angle.getValueAsString());
    }
    public SVGAnimatedRect getViewBox() {
        return viewBox;
    }
    public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
        return preserveAspectRatio;
    }
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return externalResourcesRequired;
    }
    public String getXMLlang() {
        return XMLSupport.getXMLLang(this);
    }
    public void setXMLlang(String lang) {
        setAttributeNS(XML_NAMESPACE_URI, XML_LANG_QNAME, lang);
    }
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }
    public void setXMLspace(String space) {
        setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, space);
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMMarkerElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
