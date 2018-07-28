package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGGradientElement;
public abstract class SVGOMGradientElement
    extends    SVGStylableElement
    implements SVGGradientElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
        t.put(null, SVG_GRADIENT_UNITS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_SPREAD_METHOD_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_GRADIENT_TRANSFORM_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_TRANSFORM_LIST));
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
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
                                          "xlink", "show", "other");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "actuate", "onLoad");
    }
    protected static final String[] UNITS_VALUES = {
        "",
        SVG_USER_SPACE_ON_USE_VALUE,
        SVG_OBJECT_BOUNDING_BOX_VALUE
    };
    protected static final String[] SPREAD_METHOD_VALUES = {
        "",
        SVG_PAD_VALUE,
        SVG_REFLECT_VALUE,
        SVG_REPEAT_VALUE
    };
    protected SVGOMAnimatedEnumeration gradientUnits;
    protected SVGOMAnimatedEnumeration spreadMethod;
    protected SVGOMAnimatedString href;
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected SVGOMGradientElement() {
    }
    protected SVGOMGradientElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        gradientUnits =
            createLiveAnimatedEnumeration
                (null, SVG_GRADIENT_UNITS_ATTRIBUTE, UNITS_VALUES, (short) 2);
        spreadMethod =
            createLiveAnimatedEnumeration
                (null, SVG_SPREAD_METHOD_ATTRIBUTE, SPREAD_METHOD_VALUES,
                 (short) 1);
        href =
            createLiveAnimatedString(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
    }
    public SVGAnimatedTransformList getGradientTransform() {
        throw new UnsupportedOperationException
            ("SVGGradientElement.getGradientTransform is not implemented"); 
    }
    public SVGAnimatedEnumeration getGradientUnits() {
        return gradientUnits;
    }
    public SVGAnimatedEnumeration getSpreadMethod() {
        return spreadMethod;
    }
    public SVGAnimatedString getHref() {
        return href;
    }
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return externalResourcesRequired;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMAElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
