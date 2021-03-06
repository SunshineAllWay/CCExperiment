package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMElement;
public abstract class SVGDescriptiveElementBridge extends AbstractSVGBridge
    implements GenericBridge,  BridgeUpdateHandler, SVGContext {
    Element theElt;
    Element parent;
    BridgeContext theCtx;
    public SVGDescriptiveElementBridge() {
    }
    public void handleElement(BridgeContext ctx, Element e){
        UserAgent ua = ctx.getUserAgent();
        ua.handleElement(e, Boolean.TRUE);
        if (ctx.isDynamic()) {
            SVGDescriptiveElementBridge b;
            b = (SVGDescriptiveElementBridge)getInstance();
            b.theElt = e;
            b.parent = (Element)e.getParentNode();
            b.theCtx = ctx;
            ((SVGOMElement)e).setSVGContext(b);
        }
    }
    public void dispose() {
        UserAgent ua = theCtx.getUserAgent();
        ((SVGOMElement)theElt).setSVGContext(null);
        ua.handleElement(theElt, parent);
        theElt = null;
        parent = null;
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        UserAgent ua = theCtx.getUserAgent();
        ua.handleElement(theElt, Boolean.TRUE);
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) {
        UserAgent ua = theCtx.getUserAgent();
        ua.handleElement(theElt, Boolean.TRUE);
    }
    public void handleDOMNodeRemovedEvent (MutationEvent evt) {
        dispose();
    }
    public void handleDOMAttrModifiedEvent(MutationEvent evt) { }
    public void handleCSSEngineEvent(CSSEngineEvent evt) { }
    public void handleAnimatedAttributeChanged
        (AnimatedLiveAttributeValue alav) { }
    public void handleOtherAnimationChanged(String type) { }
    public float getPixelUnitToMillimeter() {
        return theCtx.getUserAgent().getPixelUnitToMillimeter();
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    public Rectangle2D getBBox() { return null; }
    public AffineTransform getScreenTransform() {
        return theCtx.getUserAgent().getTransform();
    }
    public void setScreenTransform(AffineTransform at) {
        theCtx.getUserAgent().setTransform(at);
    }
    public AffineTransform getCTM() { return null; }
    public AffineTransform getGlobalTransform() { return null; }
    public float getViewportWidth() {
        return theCtx.getBlockWidth(theElt);
    }
    public float getViewportHeight() {
        return theCtx.getBlockHeight(theElt);
    }
    public float getFontSize() { return 0; }
}
