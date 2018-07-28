package org.apache.batik.bridge;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.svg.AbstractSVGTransformList;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGMotionAnimatableElement;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMAnimatedTransformList;
import org.apache.batik.ext.awt.geom.SegmentList;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGFitToViewBox;
import org.w3c.dom.svg.SVGTransformable;
public abstract class AbstractGraphicsNodeBridge extends AnimatableSVGBridge
    implements SVGContext,
               BridgeUpdateHandler,
               GraphicsNodeBridge,
               ErrorConstants {
    protected GraphicsNode node;
    protected boolean isSVG12;
    protected UnitProcessor.Context unitContext;
    protected AbstractGraphicsNodeBridge() {}
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        GraphicsNode node = instantiateGraphicsNode();
        setTransform(node, e, ctx);
        node.setVisible(CSSUtilities.convertVisibility(e));
        associateSVGContext(ctx, e, node);
        return node;
    }
    protected abstract GraphicsNode instantiateGraphicsNode();
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        node.setComposite(CSSUtilities.convertOpacity(e));
        node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
        node.setMask(CSSUtilities.convertMask(e, node, ctx));
        node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
        node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
        initializeDynamicSupport(ctx, e, node);
    }
    public boolean getDisplay(Element e) {
        return CSSUtilities.convertDisplay(e);
    }
    protected AffineTransform computeTransform(SVGTransformable te,
                                               BridgeContext ctx) {
        try {
            AffineTransform at = new AffineTransform();
            SVGOMAnimatedTransformList atl =
                (SVGOMAnimatedTransformList) te.getTransform();
            if (atl.isSpecified()) {
                atl.check();
                AbstractSVGTransformList tl =
                    (AbstractSVGTransformList) te.getTransform().getAnimVal();
                at.concatenate(tl.getAffineTransform());
            }
            if (e instanceof SVGMotionAnimatableElement) {
                SVGMotionAnimatableElement mae = (SVGMotionAnimatableElement) e;
                AffineTransform mat = mae.getMotionTransform();
                if (mat != null) {
                    at.concatenate(mat);
                }
            }
            return at;
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    protected void setTransform(GraphicsNode n, Element e, BridgeContext ctx) {
        n.setTransform(computeTransform((SVGTransformable) e, ctx));
    }
    protected void associateSVGContext(BridgeContext ctx,
                                       Element e,
                                       GraphicsNode node) {
        this.e = e;
        this.node = node;
        this.ctx = ctx;
        this.unitContext = UnitProcessor.createContext(ctx, e);
        this.isSVG12 = ctx.isSVG12();
        ((SVGOMElement)e).setSVGContext(this);
    }
    protected void initializeDynamicSupport(BridgeContext ctx,
                                            Element e,
                                            GraphicsNode node) {
        if (ctx.isInteractive()) {
            ctx.bind(e, node);
        }
    }
    public void handleDOMAttrModifiedEvent(MutationEvent evt) {
    }
    protected void handleGeometryChanged() {
        node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
        node.setMask(CSSUtilities.convertMask(e, node, ctx));
        node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
        if (isSVG12) {
            if (!SVG_USE_TAG.equals(e.getLocalName())) {
                fireShapeChangeEvent();
            }
            fireBBoxChangeEvent();
        }
    }
    protected void fireShapeChangeEvent() {
        DocumentEvent d = (DocumentEvent) e.getOwnerDocument();
        AbstractEvent evt = (AbstractEvent) d.createEvent("SVGEvents");
        evt.initEventNS(SVG_NAMESPACE_URI,
                        "shapechange",
                        true,
                        false);
        try {
            ((EventTarget) e).dispatchEvent(evt);
        } catch (RuntimeException ex) {
            ctx.getUserAgent().displayError(ex);
        }
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        if (evt.getTarget() instanceof Element) {
            Element e2 = (Element)evt.getTarget();
            Bridge b = ctx.getBridge(e2);
            if (b instanceof GenericBridge) {
                ((GenericBridge) b).handleElement(ctx, e2);
            }
        }
    }
    public void handleDOMNodeRemovedEvent(MutationEvent evt) {
        Node parent = e.getParentNode();
        if (parent instanceof SVGOMElement) {
            SVGContext bridge = ((SVGOMElement) parent).getSVGContext();
            if (bridge instanceof SVGSwitchElementBridge) {
                ((SVGSwitchElementBridge) bridge).handleChildElementRemoved(e);
                return;
            }
        }
        CompositeGraphicsNode gn = node.getParent();
        gn.remove(node);
        disposeTree(e);
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) {
    }
    public void dispose() {
        SVGOMElement elt = (SVGOMElement)e;
        elt.setSVGContext(null);
        ctx.unbind(e);
        bboxShape = null;
    }
    protected static void disposeTree(Node node) {
        disposeTree(node, true);
    }
    protected static void disposeTree(Node node, boolean removeContext) {
        if (node instanceof SVGOMElement) {
            SVGOMElement elt = (SVGOMElement)node;
            SVGContext ctx = elt.getSVGContext();
            if (ctx instanceof BridgeUpdateHandler) {
                BridgeUpdateHandler h = (BridgeUpdateHandler) ctx;
                if (removeContext) {
                    elt.setSVGContext(null);
                }
                h.dispose();
            }
        }
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            disposeTree(n, removeContext);
        }
    }
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
        try {
            SVGCSSEngine eng = (SVGCSSEngine) evt.getSource();
            int[] properties = evt.getProperties();
            for (int i = 0; i < properties.length; i++) {
                int idx = properties[i];
                handleCSSPropertyChanged(idx);
                String pn = eng.getPropertyName(idx);
                fireBaseAttributeListeners(pn);
            }
        } catch (Exception ex) {
            ctx.getUserAgent().displayError(ex);
        }
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {
        case SVGCSSEngine.VISIBILITY_INDEX:
            node.setVisible(CSSUtilities.convertVisibility(e));
            break;
        case SVGCSSEngine.OPACITY_INDEX:
            node.setComposite(CSSUtilities.convertOpacity(e));
            break;
        case SVGCSSEngine.FILTER_INDEX:
            node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
            break;
        case SVGCSSEngine.MASK_INDEX:
            node.setMask(CSSUtilities.convertMask(e, node, ctx));
            break;
        case SVGCSSEngine.CLIP_PATH_INDEX:
            node.setClip(CSSUtilities.convertClipPath(e, node, ctx));
            break;
        case SVGCSSEngine.POINTER_EVENTS_INDEX:
            node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
            break;
        case SVGCSSEngine.DISPLAY_INDEX:
            if (!getDisplay(e)) {
                CompositeGraphicsNode parent = node.getParent();
                parent.remove(node);
                disposeTree(e, false);
            }
            break;
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null
                && alav.getLocalName().equals(SVG_TRANSFORM_ATTRIBUTE)) {
            setTransform(node, e, ctx);
            handleGeometryChanged();
        }
    }
    public void handleOtherAnimationChanged(String type) {
        if (type.equals("motion")) {
            setTransform(node, e, ctx);
            handleGeometryChanged();
        }
    }
    protected void checkBBoxChange() {
        if (e != null) {
                    fireBBoxChangeEvent();
        }
    }
    protected void fireBBoxChangeEvent() {
        DocumentEvent d = (DocumentEvent) e.getOwnerDocument();
        AbstractEvent evt = (AbstractEvent) d.createEvent("SVGEvents");
        evt.initEventNS(SVG_NAMESPACE_URI,
                        "RenderedBBoxChange",
                        true,
                        false);
        try {
            ((EventTarget) e).dispatchEvent(evt);
        } catch (RuntimeException ex) {
            ctx.getUserAgent().displayError(ex);
        }
    }
    public float getPixelUnitToMillimeter() {
        return ctx.getUserAgent().getPixelUnitToMillimeter();
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    protected SoftReference bboxShape = null;
    protected Rectangle2D bbox = null;
    public Rectangle2D getBBox() {
        if (node == null) {
            return null;
        }
        Shape s = node.getOutline();
        if ((bboxShape != null) && (s == bboxShape.get())) return bbox;
        bboxShape = new SoftReference(s); 
        bbox = null;
        if (s == null) return bbox;
        SegmentList sl = new SegmentList(s);
        bbox = sl.getBounds2D();
        return bbox;
    }
    public AffineTransform getCTM() {
        GraphicsNode gn = node;
        AffineTransform ctm = new AffineTransform();
        Element elt = e;
        while (elt != null) {
            if (elt instanceof SVGFitToViewBox) {
                AffineTransform at;
                if (gn instanceof CanvasGraphicsNode) {
                    at = ((CanvasGraphicsNode)gn).getViewingTransform();
                } else {
                    at = gn.getTransform();
                }
                if (at != null) {
                    ctm.preConcatenate(at);
                }
                break;
            }
            AffineTransform at = gn.getTransform();
            if (at != null)
                ctm.preConcatenate(at);
            elt = SVGCSSEngine.getParentCSSStylableElement(elt);
            gn = gn.getParent();
        }
        return ctm;
    }
    public AffineTransform getScreenTransform() {
        return ctx.getUserAgent().getTransform();
    }
    public void setScreenTransform(AffineTransform at) {
        ctx.getUserAgent().setTransform(at);
    }
    public AffineTransform getGlobalTransform() {
        return node.getGlobalTransform();
    }
    public float getViewportWidth() {
        return ctx.getBlockWidth(e);
    }
    public float getViewportHeight() {
        return ctx.getBlockHeight(e);
    }
    public float getFontSize() {
        return CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.FONT_SIZE_INDEX).getFloatValue();
    }
}
