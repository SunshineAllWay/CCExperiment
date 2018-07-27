package org.apache.batik.dom.svg;
import java.awt.geom.AffineTransform;
import java.util.List;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.dom.util.ListNodeList;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.stylesheets.DocumentStyle;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGStringList;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGViewSpec;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;
public class SVGOMSVGElement
    extends    SVGStylableElement
    implements SVGSVGElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
        t.put(null, SVG_X_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_Y_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_WIDTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_HEIGHT_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_PRESERVE_ASPECT_RATIO_VALUE));
        t.put(null, SVG_VIEW_BOX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_RECT));
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        xmlTraitInformation = t;
    }
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(7);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null,
                                          "xmlns",
                                          SVG_NAMESPACE_URI);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          "xmlns",
                                          "xlink",
                                          XLinkSupport.XLINK_NAMESPACE_URI);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                          "xMidYMid meet");
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_ZOOM_AND_PAN_ATTRIBUTE,
                                          SVG_MAGNIFY_VALUE);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_VERSION_ATTRIBUTE,
                                          SVG_VERSION);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE,
                                          "text/ecmascript");
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_CONTENT_STYLE_TYPE_ATTRIBUTE,
                                          "text/css");
    }
    protected SVGOMAnimatedLength x;
    protected SVGOMAnimatedLength y;
    protected SVGOMAnimatedLength width;
    protected SVGOMAnimatedLength height;
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected SVGOMAnimatedPreserveAspectRatio preserveAspectRatio;
    protected SVGOMAnimatedRect viewBox;
    protected SVGOMSVGElement() {
    }
    public SVGOMSVGElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        x = createLiveAnimatedLength
            (null, SVG_X_ATTRIBUTE, SVG_SVG_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        y = createLiveAnimatedLength
            (null, SVG_Y_ATTRIBUTE, SVG_SVG_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        width =
            createLiveAnimatedLength
                (null, SVG_WIDTH_ATTRIBUTE, SVG_SVG_WIDTH_DEFAULT_VALUE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
        height =
            createLiveAnimatedLength
                (null, SVG_HEIGHT_ATTRIBUTE, SVG_SVG_HEIGHT_DEFAULT_VALUE,
                 SVGOMAnimatedLength.VERTICAL_LENGTH, true);
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
        preserveAspectRatio = createLiveAnimatedPreserveAspectRatio();
        viewBox = createLiveAnimatedRect(null, SVG_VIEW_BOX_ATTRIBUTE, null);
    }
    public String getLocalName() {
        return SVG_SVG_TAG;
    }
    public SVGAnimatedLength getX() {
        return x;
    }
    public SVGAnimatedLength getY() {
        return y;
    }
    public SVGAnimatedLength getWidth() {
        return width;
    }
    public SVGAnimatedLength getHeight() {
        return height;
    }
    public String getContentScriptType() {
        return getAttributeNS(null, SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE);
    }
    public void setContentScriptType(String type) {
        setAttributeNS(null, SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE, type);
    }
    public String getContentStyleType() {
        return getAttributeNS(null, SVG_CONTENT_STYLE_TYPE_ATTRIBUTE);
    }
    public void setContentStyleType(String type) {
        setAttributeNS(null, SVG_CONTENT_STYLE_TYPE_ATTRIBUTE, type);
    }
    public SVGRect getViewport() {
        SVGContext ctx = getSVGContext();
        return new SVGOMRect(0, 0, ctx.getViewportWidth(),
                             ctx.getViewportHeight());
    }
    public float getPixelUnitToMillimeterX() {
        return getSVGContext().getPixelUnitToMillimeter();
    }
    public float getPixelUnitToMillimeterY() {
        return getSVGContext().getPixelUnitToMillimeter();
    }
    public float getScreenPixelToMillimeterX() {
        return getSVGContext().getPixelUnitToMillimeter();
    }
    public float getScreenPixelToMillimeterY() {
        return getSVGContext().getPixelUnitToMillimeter();
    }
    public boolean getUseCurrentView() {
        throw new UnsupportedOperationException
            ("SVGSVGElement.getUseCurrentView is not implemented"); 
    }
    public void setUseCurrentView(boolean useCurrentView) throws DOMException {
        throw new UnsupportedOperationException
            ("SVGSVGElement.setUseCurrentView is not implemented"); 
    }
    public SVGViewSpec getCurrentView() {
        throw new UnsupportedOperationException
            ("SVGSVGElement.getCurrentView is not implemented"); 
    }
    public float getCurrentScale() {
        AffineTransform scrnTrans = getSVGContext().getScreenTransform();
        if (scrnTrans != null) {
            return (float)Math.sqrt(scrnTrans.getDeterminant());
        }
        return 1;
    }
    public void setCurrentScale(float currentScale) throws DOMException {
        SVGContext context = getSVGContext();
        AffineTransform scrnTrans = context.getScreenTransform();
        float scale = 1;
        if (scrnTrans != null) {
            scale = (float)Math.sqrt(scrnTrans.getDeterminant());
        }
        float delta = currentScale/scale;
        scrnTrans = new AffineTransform
            (scrnTrans.getScaleX()*delta, scrnTrans.getShearY()*delta,
             scrnTrans.getShearX()*delta, scrnTrans.getScaleY()*delta,
             scrnTrans.getTranslateX(), scrnTrans.getTranslateY());
        context.setScreenTransform(scrnTrans);
    }
    public SVGPoint getCurrentTranslate() {
        return new SVGPoint() {
            protected AffineTransform getScreenTransform() {
                SVGContext context = getSVGContext();
                return context.getScreenTransform();
            }
            public float getX() {
                AffineTransform scrnTrans = getScreenTransform();
                return (float)scrnTrans.getTranslateX();
            }
            public float getY() {
                AffineTransform scrnTrans = getScreenTransform();
                return (float)scrnTrans.getTranslateY();
            }
            public void setX(float newX) {
                SVGContext context = getSVGContext();
                AffineTransform scrnTrans = context.getScreenTransform();
                scrnTrans = new AffineTransform
                    (scrnTrans.getScaleX(), scrnTrans.getShearY(),
                     scrnTrans.getShearX(), scrnTrans.getScaleY(),
                     newX, scrnTrans.getTranslateY());
                context.setScreenTransform(scrnTrans);
            }
            public void setY(float newY) {
                SVGContext context = getSVGContext();
                AffineTransform scrnTrans = context.getScreenTransform();
                scrnTrans = new AffineTransform
                    (scrnTrans.getScaleX(), scrnTrans.getShearY(),
                     scrnTrans.getShearX(), scrnTrans.getScaleY(),
                     scrnTrans.getTranslateX(), newY);
                context.setScreenTransform(scrnTrans);
            }
            public SVGPoint matrixTransform(SVGMatrix mat) {
                AffineTransform scrnTrans = getScreenTransform();
                float x = (float)scrnTrans.getTranslateX();
                float y = (float)scrnTrans.getTranslateY();
                float newX = mat.getA() * x + mat.getC() * y + mat.getE();
                float newY = mat.getB() * x + mat.getD() * y + mat.getF();
                return new SVGOMPoint(newX, newY);
            }
        };
    }
    public int suspendRedraw(int max_wait_milliseconds) {
        if (max_wait_milliseconds > 60000) {
            max_wait_milliseconds = 60000;
        } else if (max_wait_milliseconds < 0) {
            max_wait_milliseconds = 0;
        }
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        return ctx.suspendRedraw(max_wait_milliseconds);
    }
    public void unsuspendRedraw(int suspend_handle_id) throws DOMException {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        if (!ctx.unsuspendRedraw(suspend_handle_id)) {
            throw createDOMException
                (DOMException.NOT_FOUND_ERR, "invalid.suspend.handle",
                 new Object[] { new Integer(suspend_handle_id) });
        }
    }
    public void unsuspendRedrawAll() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        ctx.unsuspendRedrawAll();
    }
    public void forceRedraw() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        ctx.forceRedraw();
    }
    public void pauseAnimations() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        ctx.pauseAnimations();
    }
    public void unpauseAnimations() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        ctx.unpauseAnimations();
    }
    public boolean animationsPaused() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        return ctx.animationsPaused();
    }
    public float getCurrentTime() {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        return ctx.getCurrentTime();
    }
    public void setCurrentTime(float seconds) {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        ctx.setCurrentTime(seconds);
    }
    public NodeList getIntersectionList(SVGRect rect,
                                        SVGElement referenceElement) {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        List list = ctx.getIntersectionList(rect, referenceElement);
        return new ListNodeList(list);
    }
    public NodeList getEnclosureList(SVGRect rect,
                                     SVGElement referenceElement) {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        List list = ctx.getEnclosureList(rect, referenceElement);
        return new ListNodeList(list);
    }
    public boolean checkIntersection(SVGElement element, SVGRect rect) {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        return ctx.checkIntersection(element, rect);
    }
    public boolean checkEnclosure(SVGElement element, SVGRect rect) {
        SVGSVGContext ctx = (SVGSVGContext)getSVGContext();
        return ctx.checkEnclosure(element, rect);
    }
    public void deselectAll() {
        ((SVGSVGContext)getSVGContext()).deselectAll();
    }
    public SVGNumber createSVGNumber() {
        return new SVGNumber() {
            protected float value;
            public float getValue() {
                return value;
            }
            public void setValue(float f) {
                value = f;
            }
        };
    }
    public SVGLength createSVGLength() {
        return new SVGOMLength(this);
    }
    public SVGAngle createSVGAngle() {
        return new SVGOMAngle();
    }
    public SVGPoint createSVGPoint() {
        return new SVGOMPoint(0, 0);
    }
    public SVGMatrix createSVGMatrix() {
        return new AbstractSVGMatrix() {
            protected AffineTransform at = new AffineTransform();
            protected AffineTransform getAffineTransform() {
                return at;
            }
        };
    }
    public SVGRect createSVGRect() {
        return new SVGOMRect(0, 0, 0, 0);
    }
    public SVGTransform createSVGTransform() {
        SVGOMTransform ret = new SVGOMTransform();
        ret.setType(SVGTransform.SVG_TRANSFORM_MATRIX);
        return ret;
    }
    public SVGTransform createSVGTransformFromMatrix(SVGMatrix matrix) {
        SVGOMTransform tr = new SVGOMTransform();
        tr.setMatrix(matrix);
        return tr;
    }
    public Element getElementById(String elementId) {
        return ownerDocument.getChildElementById(this, elementId);
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
    public DocumentView getDocument() {
        return (DocumentView)getOwnerDocument();
    }
    public CSSStyleDeclaration getComputedStyle(Element elt,
                                                String pseudoElt) {
        AbstractView av = ((DocumentView)getOwnerDocument()).getDefaultView();
        return ((ViewCSS)av).getComputedStyle(elt, pseudoElt);
    }
    public Event createEvent(String eventType) throws DOMException {
        return ((DocumentEvent)getOwnerDocument()).createEvent(eventType);
    }
    public boolean canDispatch(String namespaceURI, String type)
            throws DOMException {
        AbstractDocument doc = (AbstractDocument) getOwnerDocument();
        return doc.canDispatch(namespaceURI, type);
    }
    public StyleSheetList getStyleSheets() {
        return ((DocumentStyle)getOwnerDocument()).getStyleSheets();
    }
    public CSSStyleDeclaration getOverrideStyle(Element elt,
                                                String pseudoElt) {
        return ((DocumentCSS)getOwnerDocument()).getOverrideStyle(elt,
                                                                  pseudoElt);
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
    public short getZoomAndPan() {
        return SVGZoomAndPanSupport.getZoomAndPan(this);
    }
    public void setZoomAndPan(short val) {
        SVGZoomAndPanSupport.setZoomAndPan(this, val);
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
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMSVGElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
