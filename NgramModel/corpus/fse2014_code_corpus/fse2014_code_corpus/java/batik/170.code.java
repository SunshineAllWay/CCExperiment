package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.anim.AnimatableElement;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.dom.anim.AnimationTargetListener;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.SVGAnimationContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGElement;
public abstract class SVGAnimationElementBridge extends AbstractSVGBridge
        implements GenericBridge,
                   BridgeUpdateHandler,
                   SVGAnimationContext,
                   AnimatableElement {
    protected SVGOMElement element;
    protected BridgeContext ctx;
    protected SVGAnimationEngine eng;
    protected TimedElement timedElement;
    protected AbstractAnimation animation;
    protected String attributeNamespaceURI;
    protected String attributeLocalName;
    protected short animationType;
    protected SVGOMElement targetElement;
    protected AnimationTarget animationTarget;
    public TimedElement getTimedElement() {
        return timedElement;
    }
    public AnimatableValue getUnderlyingValue() {
        if (animationType == AnimationEngine.ANIM_TYPE_XML) {
            return animationTarget.getUnderlyingValue(attributeNamespaceURI,
                                                      attributeLocalName);
        } else {
            return eng.getUnderlyingCSSValue(element,
                                             animationTarget,
                                             attributeLocalName);
        }
    }
    public void handleElement(BridgeContext ctx, Element e) {
        if (ctx.isDynamic() && BridgeContext.getSVGContext(e) == null) {
            SVGAnimationElementBridge b =
                (SVGAnimationElementBridge) getInstance();
            b.element = (SVGOMElement) e;
            b.ctx = ctx;
            b.eng = ctx.getAnimationEngine();
            b.element.setSVGContext(b);
            if (b.eng.hasStarted()) {
                b.initializeAnimation();
                b.initializeTimedElement();
            } else {
                b.eng.addInitialBridge(b);
            }
        }
    }
    protected void initializeAnimation() {
        String uri = XLinkSupport.getXLinkHref(element);
        Node t;
        if (uri.length() == 0) {
            t = element.getParentNode();
        } else {
            t = ctx.getReferencedElement(element, uri);
            if (t.getOwnerDocument() != element.getOwnerDocument()) {
                throw new BridgeException
                    (ctx, element, ErrorConstants.ERR_URI_BAD_TARGET,
                     new Object[] { uri });
            }
        }
        animationTarget = null;
        if (t instanceof SVGOMElement) {
            targetElement = (SVGOMElement) t;
            animationTarget = targetElement;
        }
        if (animationTarget == null) {
            throw new BridgeException
                (ctx, element, ErrorConstants.ERR_URI_BAD_TARGET,
                 new Object[] { uri });
        }
        String an = element.getAttributeNS(null, SVG_ATTRIBUTE_NAME_ATTRIBUTE);
        int ci = an.indexOf(':');
        if (ci == -1) {
            if (element.hasProperty(an)) {
                animationType = AnimationEngine.ANIM_TYPE_CSS;
                attributeLocalName = an;
            } else {
                animationType = AnimationEngine.ANIM_TYPE_XML;
                attributeLocalName = an;
            }
        } else {
            animationType = AnimationEngine.ANIM_TYPE_XML;
            String prefix = an.substring(0, ci);
            attributeNamespaceURI = element.lookupNamespaceURI(prefix);
            attributeLocalName = an.substring(ci + 1);
        }
        if (animationType == AnimationEngine.ANIM_TYPE_CSS
                && !targetElement.isPropertyAnimatable(attributeLocalName)
            || animationType == AnimationEngine.ANIM_TYPE_XML
                && !targetElement.isAttributeAnimatable(attributeNamespaceURI,
                                                        attributeLocalName)) {
            throw new BridgeException
                (ctx, element, "attribute.not.animatable",
                 new Object[] { targetElement.getNodeName(), an });
        }
        int type;
        if (animationType == AnimationEngine.ANIM_TYPE_CSS) {
            type = targetElement.getPropertyType(attributeLocalName);
        } else {
            type = targetElement.getAttributeType(attributeNamespaceURI,
                                                  attributeLocalName);
        }
        if (!canAnimateType(type)) {
            throw new BridgeException
                (ctx, element, "type.not.animatable",
                 new Object[] { targetElement.getNodeName(), an,
                                element.getNodeName() });
        }
        timedElement = createTimedElement();
        animation = createAnimation(animationTarget);
        eng.addAnimation(animationTarget, animationType, attributeNamespaceURI,
                         attributeLocalName, animation);
    }
    protected abstract boolean canAnimateType(int type);
    protected boolean checkValueType(AnimatableValue v) {
        return true;
    }
    protected void initializeTimedElement() {
        initializeTimedElement(timedElement);
        timedElement.initialize();
    }
    protected TimedElement createTimedElement() {
        return new SVGTimedElement();
    }
    protected abstract AbstractAnimation createAnimation(AnimationTarget t);
    protected AnimatableValue parseAnimatableValue(String an) {
        if (!element.hasAttributeNS(null, an)) {
            return null;
        }
        String s = element.getAttributeNS(null, an);
        AnimatableValue val = eng.parseAnimatableValue
            (element, animationTarget, attributeNamespaceURI,
             attributeLocalName, animationType == AnimationEngine.ANIM_TYPE_CSS,
             s);
        if (!checkValueType(val)) {
            throw new BridgeException
                (ctx, element, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] { an, s });
        }
        return val;
    }
    protected void initializeTimedElement(TimedElement timedElement) {
        timedElement.parseAttributes
            (element.getAttributeNS(null, "begin"),
             element.getAttributeNS(null, "dur"),
             element.getAttributeNS(null, "end"),
             element.getAttributeNS(null, "min"),
             element.getAttributeNS(null, "max"),
             element.getAttributeNS(null, "repeatCount"),
             element.getAttributeNS(null, "repeatDur"),
             element.getAttributeNS(null, "fill"),
             element.getAttributeNS(null, "restart"));
    }
    public void handleDOMAttrModifiedEvent(MutationEvent evt) {
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
    }
    public void handleDOMNodeRemovedEvent(MutationEvent evt) {
        element.setSVGContext(null);
        dispose();
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) {
    }
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
    }
    public void handleOtherAnimationChanged(String type) {
    }
    public void dispose() {
        if (element.getSVGContext() == null) {
            eng.removeAnimation(animation);
            timedElement.deinitialize();
            timedElement = null;
            element = null;
        }
    }
    public float getPixelUnitToMillimeter() {
        return ctx.getUserAgent().getPixelUnitToMillimeter();
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    public Rectangle2D getBBox() { return null; }
    public AffineTransform getScreenTransform() { 
        return ctx.getUserAgent().getTransform();
    }
    public void setScreenTransform(AffineTransform at) { 
        ctx.getUserAgent().setTransform(at);
    }
    public AffineTransform getCTM() { return null; }
    public AffineTransform getGlobalTransform() { return null; }
    public float getViewportWidth() {
        return ctx.getBlockWidth(element);
    }
    public float getViewportHeight() {
        return ctx.getBlockHeight(element);
    }
    public float getFontSize() { return 0; }
    public float svgToUserSpace(float v, int type, int pcInterp) {
        return 0;
    }
    public void addTargetListener(String pn, AnimationTargetListener l) {
    }
    public void removeTargetListener(String pn, AnimationTargetListener l) {
    }
    public SVGElement getTargetElement() {
        return targetElement;
    }
    public float getStartTime() {
        return timedElement.getCurrentBeginTime();
    }
    public float getCurrentTime() {
        return timedElement.getLastSampleTime();
    }
    public float getSimpleDuration() {
        return timedElement.getSimpleDur();
    }
    public float getHyperlinkBeginTime() {
        return timedElement.getHyperlinkBeginTime();
    }
    public boolean beginElement() throws DOMException {
        timedElement.beginElement();
        return timedElement.canBegin();
    }
    public boolean beginElementAt(float offset) throws DOMException {
        timedElement.beginElement(offset);
        return true;
    }
    public boolean endElement() throws DOMException {
        timedElement.endElement();
        return timedElement.canEnd();
    }
    public boolean endElementAt(float offset) throws DOMException {
        timedElement.endElement(offset);
        return true;
    }
    protected boolean isConstantAnimation() {
        return false;
    }
    protected class SVGTimedElement extends TimedElement {
        public Element getElement() {
            return element;
        }
        protected void fireTimeEvent(String eventType, Calendar time,
                                     int detail) {
            AnimationSupport.fireTimeEvent(element, eventType, time, detail);
        }
        protected void toActive(float begin) {
            eng.toActive(animation, begin);
        }
        protected void toInactive(boolean stillActive, boolean isFrozen) {
            eng.toInactive(animation, isFrozen);
        }
        protected void removeFill() {
            eng.removeFill(animation);
        }
        protected void sampledAt(float simpleTime, float simpleDur,
                                 int repeatIteration) {
            eng.sampledAt(animation, simpleTime, simpleDur, repeatIteration);
        }
        protected void sampledLastValue(int repeatIteration) {
            eng.sampledLastValue(animation, repeatIteration);
        }
        protected TimedElement getTimedElementById(String id) {
            return AnimationSupport.getTimedElementById(id, element);
        }
        protected EventTarget getEventTargetById(String id) {
            return AnimationSupport.getEventTargetById(id, element);
        }
        protected EventTarget getRootEventTarget() {
            return (EventTarget) element.getOwnerDocument();
        }
        protected EventTarget getAnimationEventTarget() {
            return targetElement;
        }
        public boolean isBefore(TimedElement other) {
            Element e = ((SVGTimedElement) other).getElement();
            int pos = ((AbstractNode) element).compareDocumentPosition(e);
            return (pos & AbstractNode.DOCUMENT_POSITION_PRECEDING) != 0;
        }
        public String toString() {
            if (element != null) {
                String id = element.getAttributeNS(null, "id");
                if (id.length() != 0) {
                    return id;
                }
            }
            return super.toString();
        }
        protected boolean isConstantAnimation() {
            return SVGAnimationElementBridge.this.isConstantAnimation();
        }
    }
}
