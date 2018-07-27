package org.apache.batik.extension;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGLocatableSupport;
import org.apache.batik.dom.svg.SVGOMAnimatedBoolean;
import org.apache.batik.dom.svg.SVGOMAnimatedTransformList;
import org.apache.batik.dom.svg.SVGTestsSupport;
import org.apache.batik.dom.svg.TraitInformation;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGExternalResourcesRequired;
import org.w3c.dom.svg.SVGLangSpace;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStringList;
import org.w3c.dom.svg.SVGTests;
import org.w3c.dom.svg.SVGTransformable;
public abstract class GraphicsExtensionElement
        extends    StylableExtensionElement
        implements SVGTransformable {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(StylableExtensionElement.xmlTraitInformation);
        t.put(null, SVG_TRANSFORM_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_TRANSFORM_LIST));
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        t.put(null, SVG_REQUIRED_EXTENSIONS_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_URI_LIST));
        t.put(null, SVG_REQUIRED_FEATURES_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_URI_LIST));
        t.put(null, SVG_SYSTEM_LANGUAGE_ATTRIBUTE,
                new TraitInformation(false, SVGTypes.TYPE_LANG_LIST));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedTransformList transform =
        createLiveAnimatedTransformList(null, SVG_TRANSFORM_ATTRIBUTE, "");
    protected SVGOMAnimatedBoolean externalResourcesRequired =
        createLiveAnimatedBoolean
            (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
    protected GraphicsExtensionElement() {
    }
    protected GraphicsExtensionElement(String name, AbstractDocument owner) {
        super(name, owner);
    }
    public SVGElement getNearestViewportElement() {
        return SVGLocatableSupport.getNearestViewportElement(this);
    }
    public SVGElement getFarthestViewportElement() {
        return SVGLocatableSupport.getFarthestViewportElement(this);
    }
    public SVGRect getBBox() {
        return SVGLocatableSupport.getBBox(this);
    }
    public SVGMatrix getCTM() {
        return SVGLocatableSupport.getCTM(this);
    }
    public SVGMatrix getScreenCTM() {
        return SVGLocatableSupport.getScreenCTM(this);
    }
    public SVGMatrix getTransformToElement(SVGElement element)
        throws SVGException {
        return SVGLocatableSupport.getTransformToElement(this, element);
    }
    public SVGAnimatedTransformList getTransform() {
        return transform;
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
    public SVGStringList getRequiredFeatures() {
        return SVGTestsSupport.getRequiredFeatures(this);
    }
    public SVGStringList getRequiredExtensions() {
        return SVGTestsSupport.getRequiredExtensions(this);
    }
    public SVGStringList getSystemLanguage() {
        return SVGTestsSupport.getSystemLanguage(this);
    }
    public boolean hasExtension(String extension) {
        return SVGTestsSupport.hasExtension(this, extension);
    }
}
