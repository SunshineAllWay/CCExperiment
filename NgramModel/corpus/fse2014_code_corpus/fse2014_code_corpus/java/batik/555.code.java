package org.apache.batik.dom.svg;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGStringList;
public abstract class SVGOMAnimationElement
    extends SVGOMElement
    implements SVGAnimationElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected SVGOMAnimationElement() {
    }
    protected SVGOMAnimationElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
    }
    public SVGElement getTargetElement() {
        return ((SVGAnimationContext) getSVGContext()).getTargetElement();
    }
    public float getStartTime() {
        return ((SVGAnimationContext) getSVGContext()).getStartTime();
    }
    public float getCurrentTime() {
        return ((SVGAnimationContext) getSVGContext()).getCurrentTime();
    }
    public float getSimpleDuration() throws DOMException {
        float dur = ((SVGAnimationContext) getSVGContext()).getSimpleDuration();
        if (dur == TimedElement.INDEFINITE) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "animation.dur.indefinite",
                                     null);
        }
        return dur;
    }
    public float getHyperlinkBeginTime() {
        return ((SVGAnimationContext) getSVGContext()).getHyperlinkBeginTime();
    }
    public boolean beginElement() throws DOMException {
        return ((SVGAnimationContext) getSVGContext()).beginElement();
    }
    public boolean beginElementAt(float offset) throws DOMException {
        return ((SVGAnimationContext) getSVGContext()).beginElementAt(offset);
    }
    public boolean endElement() throws DOMException {
        return ((SVGAnimationContext) getSVGContext()).endElement();
    }
    public boolean endElementAt(float offset) throws DOMException {
        return ((SVGAnimationContext) getSVGContext()).endElementAt(offset);
    }
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return externalResourcesRequired;
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
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
