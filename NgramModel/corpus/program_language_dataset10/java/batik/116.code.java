package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;
public abstract class AnimatableGenericSVGBridge
        extends AnimatableSVGBridge
        implements GenericBridge, BridgeUpdateHandler, SVGContext {
    public void handleElement(BridgeContext ctx, Element e) {
        if (ctx.isDynamic()) {
            this.e = e;
            this.ctx = ctx;
            ((SVGOMElement) e).setSVGContext(this);
        }
    }
    public float getPixelUnitToMillimeter() {
        return ctx.getUserAgent().getPixelUnitToMillimeter();
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    public Rectangle2D getBBox() {
        return null;
    }
    public AffineTransform getScreenTransform() {
        return ctx.getUserAgent().getTransform();
    }
    public void setScreenTransform(AffineTransform at) {
        ctx.getUserAgent().setTransform(at);
    }
    public AffineTransform getCTM() {
        return null;
    }
    public AffineTransform getGlobalTransform() {
        return null;
    }
    public float getViewportWidth() {
        return 0f;
    }
    public float getViewportHeight() {
        return 0f;
    }
    public float getFontSize() {
        return 0f;
    }
    public void dispose() {
        ((SVGOMElement) e).setSVGContext(null);
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) { 
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) { 
    }
    public void handleDOMNodeRemovedEvent(MutationEvent evt) { 
        dispose();
    }
    public void handleDOMAttrModifiedEvent(MutationEvent evt) {
    }
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
    }
    public void handleOtherAnimationChanged(String type) {
    }
}
