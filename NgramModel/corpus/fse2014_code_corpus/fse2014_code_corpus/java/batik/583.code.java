package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEImageElement;
public class SVGOMFEImageElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEImageElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        t.put(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_PRESERVE_ASPECT_RATIO_VALUE));
        t.put(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_URI));
        xmlTraitInformation = t;
    }
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(4);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null, "xmlns:xlink",
                                          XLinkSupport.XLINK_NAMESPACE_URI);
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "type", "simple");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "show", "embed");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "actuate", "onLoad");
    }
    protected SVGOMAnimatedString href;
    protected SVGOMAnimatedPreserveAspectRatio preserveAspectRatio;
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected SVGOMFEImageElement() {
    }
    public SVGOMFEImageElement(String prefix,
                               AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        href =
            createLiveAnimatedString(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
        preserveAspectRatio = createLiveAnimatedPreserveAspectRatio();
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
    }
    public String getLocalName() {
        return SVG_FE_IMAGE_TAG;
    }
    public SVGAnimatedString getHref() {
        return href;
    }
    public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
        return preserveAspectRatio;
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
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return externalResourcesRequired;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMFEImageElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
