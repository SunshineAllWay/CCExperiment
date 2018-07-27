package org.apache.batik.bridge;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMAnimatedRect;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.dom.svg.SVGSVGContext;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.TextNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;
public class SVGSVGElementBridge 
    extends SVGGElementBridge 
    implements SVGSVGContext {
    public SVGSVGElementBridge() {}
    public String getLocalName() {
        return SVG_SVG_TAG;
    }
    public Bridge getInstance(){
        return new SVGSVGElementBridge();
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return new CanvasGraphicsNode();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        CanvasGraphicsNode cgn;
        cgn = (CanvasGraphicsNode)instantiateGraphicsNode();
        associateSVGContext(ctx, e, cgn);
        try {
            SVGDocument doc = (SVGDocument)e.getOwnerDocument();
            SVGOMSVGElement se = (SVGOMSVGElement) e;
            boolean isOutermost = (doc.getRootElement() == e);
            float x = 0;
            float y = 0;
            if (!isOutermost) {
                AbstractSVGAnimatedLength _x =
                    (AbstractSVGAnimatedLength) se.getX();
                x = _x.getCheckedValue();
                AbstractSVGAnimatedLength _y =
                    (AbstractSVGAnimatedLength) se.getY();
                y = _y.getCheckedValue();
            }
            AbstractSVGAnimatedLength _width =
                (AbstractSVGAnimatedLength) se.getWidth();
            float w = _width.getCheckedValue();
            AbstractSVGAnimatedLength _height =
                (AbstractSVGAnimatedLength) se.getHeight();
            float h = _height.getCheckedValue();
            cgn.setVisible(CSSUtilities.convertVisibility(e));
            SVGOMAnimatedRect vb = (SVGOMAnimatedRect) se.getViewBox();
            SVGAnimatedPreserveAspectRatio par = se.getPreserveAspectRatio();
            AffineTransform viewingTransform =
                ViewBox.getPreserveAspectRatioTransform(e, vb, par, w, h, ctx);
            float actualWidth = w;
            float actualHeight = h;
            try {
                AffineTransform vtInv = viewingTransform.createInverse();
                actualWidth = (float) (w*vtInv.getScaleX());
                actualHeight = (float) (h*vtInv.getScaleY());
            } catch (NoninvertibleTransformException ex) {}
            AffineTransform positionTransform =
                AffineTransform.getTranslateInstance(x, y);
            if (!isOutermost) {
                cgn.setPositionTransform(positionTransform);
            } else if (doc == ctx.getDocument()) {
                final double dw = w;
                final double dh = h;
                ctx.setDocumentSize(new Dimension2D() {
                        double w= dw;
                        double h= dh;
                        public double getWidth()  { return w; }
                        public double getHeight() { return h; }
                        public void   setSize(double w, double h) {
                            this.w = w;
                            this.h = h;
                        }
                    });
            }
            cgn.setViewingTransform(viewingTransform);
            Shape clip = null;
            if (CSSUtilities.convertOverflow(e)) { 
                float [] offsets = CSSUtilities.convertClip(e);
                if (offsets == null) { 
                    clip = new Rectangle2D.Float(x, y, w, h);
                } else { 
                    clip = new Rectangle2D.Float(x+offsets[3],
                                                 y+offsets[0],
                                                 w-offsets[1]-offsets[3],
                                                 h-offsets[2]-offsets[0]);
                }
            }
            if (clip != null) {
                try {
                    AffineTransform at = new AffineTransform(positionTransform);
                    at.concatenate(viewingTransform);
                    at = at.createInverse(); 
                    clip = at.createTransformedShape(clip);
                    Filter filter = cgn.getGraphicsNodeRable(true);
                    cgn.setClip(new ClipRable8Bit(filter, clip));
                } catch (NoninvertibleTransformException ex) {}
            }
            RenderingHints hints = null;
            hints = CSSUtilities.convertColorRendering(e, hints);
            if (hints != null)
                cgn.setRenderingHints(hints);
            Rectangle2D r = CSSUtilities.convertEnableBackground(e);
            if (r != null) {
                cgn.setBackgroundEnable(r);
            }
            if (vb.isSpecified()) {
                SVGRect vbr = vb.getAnimVal();
                actualWidth = vbr.getWidth();
                actualHeight = vbr.getHeight();
            }
            ctx.openViewport
                (e, new SVGSVGElementViewport(actualWidth,
                                              actualHeight));
            return cgn;
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        node.setComposite(CSSUtilities.convertOpacity(e));
        node.setFilter(CSSUtilities.convertFilter(e, node, ctx));
        node.setMask(CSSUtilities.convertMask(e, node, ctx));
        node.setPointerEventType(CSSUtilities.convertPointerEvents(e));
        initializeDynamicSupport(ctx, e, node);
        ctx.closeViewport(e);
    }
    public void dispose() {
        ctx.removeViewport(e);
        super.dispose();
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        try {
            boolean rebuild = false;
            if (alav.getNamespaceURI() == null) {
                String ln = alav.getLocalName();
                if (ln.equals(SVG_WIDTH_ATTRIBUTE)
                        || ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                    rebuild = true;
                } else if (ln.equals(SVG_X_ATTRIBUTE)
                        || ln.equals(SVG_Y_ATTRIBUTE)) {
                    SVGDocument doc = (SVGDocument)e.getOwnerDocument();
                    SVGOMSVGElement se = (SVGOMSVGElement) e;
                    boolean isOutermost = doc.getRootElement() == e;
                    if (!isOutermost) {
                        AbstractSVGAnimatedLength _x =
                            (AbstractSVGAnimatedLength) se.getX();
                        float x = _x.getCheckedValue();
                        AbstractSVGAnimatedLength _y =
                            (AbstractSVGAnimatedLength) se.getY();
                        float y = _y.getCheckedValue();
                        AffineTransform positionTransform =
                            AffineTransform.getTranslateInstance(x, y);
                        CanvasGraphicsNode cgn;
                        cgn = (CanvasGraphicsNode)node;
                        cgn.setPositionTransform(positionTransform);
                        return;
                    }
                } else if (ln.equals(SVG_VIEW_BOX_ATTRIBUTE)
                        || ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                    SVGDocument doc = (SVGDocument)e.getOwnerDocument();
                    SVGOMSVGElement se = (SVGOMSVGElement) e;
                    boolean isOutermost = doc.getRootElement() == e;
                    float x = 0;
                    float y = 0;
                    if (!isOutermost) {
                        AbstractSVGAnimatedLength _x =
                            (AbstractSVGAnimatedLength) se.getX();
                        x = _x.getCheckedValue();
                        AbstractSVGAnimatedLength _y =
                            (AbstractSVGAnimatedLength) se.getY();
                        y = _y.getCheckedValue();
                    }
                    AbstractSVGAnimatedLength _width =
                        (AbstractSVGAnimatedLength) se.getWidth();
                    float w = _width.getCheckedValue();
                    AbstractSVGAnimatedLength _height =
                        (AbstractSVGAnimatedLength) se.getHeight();
                    float h = _height.getCheckedValue();
                    CanvasGraphicsNode cgn;
                    cgn = (CanvasGraphicsNode)node;
                    SVGOMAnimatedRect vb = (SVGOMAnimatedRect) se.getViewBox();
                    SVGAnimatedPreserveAspectRatio par = se.getPreserveAspectRatio();
                    AffineTransform newVT = ViewBox.getPreserveAspectRatioTransform
                        (e, vb, par, w, h, ctx);
                    AffineTransform oldVT = cgn.getViewingTransform();
                    if ((newVT.getScaleX() != oldVT.getScaleX()) ||
                        (newVT.getScaleY() != oldVT.getScaleY()) ||
                        (newVT.getShearX() != oldVT.getShearX()) ||
                        (newVT.getShearY() != oldVT.getShearY()))
                        rebuild = true;
                    else {
                        cgn.setViewingTransform(newVT);
                        Shape clip = null;
                        if (CSSUtilities.convertOverflow(e)) { 
                            float [] offsets = CSSUtilities.convertClip(e);
                            if (offsets == null) { 
                                clip = new Rectangle2D.Float(x, y, w, h);
                            } else { 
                                clip = new Rectangle2D.Float(x+offsets[3],
                                                             y+offsets[0],
                                                             w-offsets[1]-offsets[3],
                                                             h-offsets[2]-offsets[0]);
                            }
                        }
                        if (clip != null) {
                            try {
                                AffineTransform at;
                                at = cgn.getPositionTransform();
                                if (at == null) at = new AffineTransform();
                                else            at = new AffineTransform(at);
                                at.concatenate(newVT);
                                at = at.createInverse(); 
                                clip = at.createTransformedShape(clip);
                                Filter filter = cgn.getGraphicsNodeRable(true);
                                cgn.setClip(new ClipRable8Bit(filter, clip));
                            } catch (NoninvertibleTransformException ex) {}
                        }
                    }
                }
                if (rebuild) {
                    CompositeGraphicsNode gn = node.getParent();
                    gn.remove(node);
                    disposeTree(e, false);
                    handleElementAdded(gn, e.getParentNode(), e);
                    return;
                }
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
        super.handleAnimatedAttributeChanged(alav);
    }
    public static class SVGSVGElementViewport implements Viewport {
        private float width;
        private float height;
        public SVGSVGElementViewport(float w, float h) {
            this.width = w;
            this.height = h;
        }
        public float getWidth(){
            return width;
        }
        public float getHeight(){
            return height;
        }
    }
    public List getIntersectionList(SVGRect svgRect, Element end) {
        List ret = new ArrayList();
        Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(),
                                                 svgRect.getY(),
                                                 svgRect.getWidth(),
                                                 svgRect.getHeight());
        GraphicsNode svgGN = ctx.getGraphicsNode(e);
        if (svgGN == null) return ret;
        Rectangle2D svgBounds = svgGN.getSensitiveBounds();
        if (svgBounds == null)
            return ret;
        if (!rect.intersects(svgBounds))
            return ret;
        Element base = e;
        AffineTransform ati = svgGN.getGlobalTransform();
        try {
            ati = ati.createInverse();
        } catch (NoninvertibleTransformException e) {
        }
        Element curr;
        Node    next = base.getFirstChild();
        while (next != null) {
            if (next instanceof Element) 
                break;
            next = next.getNextSibling();
        }
        if (next == null) return ret;
        curr = (Element)next;
        Set ancestors = null;
        if (end != null) {
            ancestors = getAncestors(end, base);
            if (ancestors == null)
                end = null;
        }
        while (curr != null) {
            String nsURI = curr.getNamespaceURI();
            String tag = curr.getLocalName();
            boolean isGroup;
            isGroup = SVG_NAMESPACE_URI.equals(nsURI)
                && (SVG_G_TAG.equals(tag)
                        || SVG_SVG_TAG.equals(tag)
                        || SVG_A_TAG.equals(tag));
            GraphicsNode gn = ctx.getGraphicsNode(curr);
            if (gn == null) {
                if ((ancestors != null) && (ancestors.contains(curr)))
                    break;
                curr = getNext(curr, base, end);
                continue;
            }
            AffineTransform at = gn.getGlobalTransform();
            Rectangle2D gnBounds = gn.getSensitiveBounds();
            at.preConcatenate(ati);
            if (gnBounds != null)
                gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
            if ((gnBounds == null) || 
                (!rect.intersects(gnBounds))) {
                if ((ancestors != null) && (ancestors.contains(curr)))
                    break;
                curr = getNext(curr, base, end);
                continue;
            }
            if (isGroup) {
                next = curr.getFirstChild();
                while (next != null) {
                    if (next instanceof Element) 
                        break;
                    next = next.getNextSibling();
                }
                if (next != null) {
                    curr = (Element)next;
                    continue;
                }
            } else {
                if (curr == end) break;
                if (SVG_NAMESPACE_URI.equals(nsURI)
                        && SVG_USE_TAG.equals(tag)) {
                    if (rect.contains(gnBounds))
                        ret.add(curr);
                } if (gn instanceof ShapeNode) {
                    ShapeNode sn = (ShapeNode)gn;
                    Shape sensitive = sn.getSensitiveArea();
                    if (sensitive != null) {
                        sensitive = at.createTransformedShape(sensitive);
                        if (sensitive.intersects(rect))
                            ret.add(curr);
                    }
                } else if (gn instanceof TextNode) {
                    SVGOMElement svgElem = (SVGOMElement)curr;
                    SVGTextElementBridge txtBridge;
                    txtBridge = (SVGTextElementBridge)svgElem.getSVGContext();
                    Set elems = txtBridge.getTextIntersectionSet(at, rect);
                    if ((ancestors != null) && ancestors.contains(curr))
                        filterChildren(curr, end, elems, ret);
                    else
                        ret.addAll(elems);
                } else {
                    ret.add(curr);
                }
            }
            curr = getNext(curr, base, end);
        }
        return ret;
    }
    public List getEnclosureList(SVGRect svgRect, Element end) {
        List ret = new ArrayList();
        Rectangle2D rect = new Rectangle2D.Float(svgRect.getX(),
                                                 svgRect.getY(),
                                                 svgRect.getWidth(),
                                                 svgRect.getHeight());
        GraphicsNode svgGN     = ctx.getGraphicsNode(e);
        if (svgGN == null) return ret;
        Rectangle2D  svgBounds = svgGN.getSensitiveBounds();
        if (svgBounds == null)
            return ret;
        if (!rect.intersects(svgBounds))
            return ret;
        Element base = e;
        AffineTransform ati = svgGN.getGlobalTransform();
        try {
            ati = ati.createInverse();
        } catch (NoninvertibleTransformException e) {
        }
        Element curr;
        Node    next = base.getFirstChild();
        while (next != null) {
            if (next instanceof Element) 
                break;
            next = next.getNextSibling();
        }
        if (next == null) return ret;
        curr = (Element)next;
        Set ancestors = null;
        if (end != null) {
            ancestors = getAncestors(end, base);
            if (ancestors == null)
                end = null;
        }
        while (curr != null) {
            String nsURI = curr.getNamespaceURI();
            String tag = curr.getLocalName();
            boolean isGroup;
            isGroup = SVG_NAMESPACE_URI.equals(nsURI)
                && (SVG_G_TAG.equals(tag)
                        || SVG_SVG_TAG.equals(tag)
                        || SVG_A_TAG.equals(tag));
            GraphicsNode gn = ctx.getGraphicsNode(curr);
            if (gn == null) {
                if ((ancestors != null) && (ancestors.contains(curr)))
                    break;
                curr = getNext(curr, base, end);
                continue;
            }
            AffineTransform at = gn.getGlobalTransform();
            Rectangle2D gnBounds = gn.getSensitiveBounds();
            at.preConcatenate(ati);
            if (gnBounds != null)
                gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
            if ((gnBounds == null) || 
                (!rect.intersects(gnBounds))) {
                if ((ancestors != null) && (ancestors.contains(curr)))
                    break;
                curr = getNext(curr, base, end);
                continue;
            }
            if (isGroup) {
                next = curr.getFirstChild();
                while (next != null) {
                    if (next instanceof Element) 
                        break;
                    next = next.getNextSibling();
                }
                if (next != null) {
                    curr = (Element)next;
                    continue;
                }
            } else {
                if (curr == end) break;
                if (SVG_NAMESPACE_URI.equals(nsURI)
                        && SVG_USE_TAG.equals(tag)) {
                    if (rect.contains(gnBounds))
                        ret.add(curr);
                } else if (gn instanceof TextNode) {
                    SVGOMElement svgElem = (SVGOMElement)curr;
                    SVGTextElementBridge txtBridge;
                    txtBridge = (SVGTextElementBridge)svgElem.getSVGContext();
                    Set elems = txtBridge.getTextEnclosureSet(at, rect);
                    if ((ancestors != null) && ancestors.contains(curr))
                        filterChildren(curr, end, elems, ret);
                    else
                        ret.addAll(elems);
                } else if (rect.contains(gnBounds)) {
                    ret.add(curr);
                }
            }
            curr = getNext(curr, base, end);
        }
        return ret;
    }
    public boolean checkIntersection (Element element, SVGRect svgRect ) {
        GraphicsNode svgGN = ctx.getGraphicsNode(e);
        if (svgGN == null) return false; 
        Rectangle2D rect = new Rectangle2D.Float
            (svgRect.getX(),     svgRect.getY(), 
             svgRect.getWidth(), svgRect.getHeight());
        AffineTransform ati = svgGN.getGlobalTransform();
        try {
            ati = ati.createInverse();
        } catch (NoninvertibleTransformException e) {  }
        SVGContext svgctx = null;
        if (element instanceof SVGOMElement) {
            svgctx  = ((SVGOMElement)element).getSVGContext();
            if ((svgctx instanceof SVGTextElementBridge) ||
                (svgctx instanceof 
                 SVGTextElementBridge.AbstractTextChildSVGContext)) {
                return SVGTextElementBridge.getTextIntersection
                    (ctx, element, ati, rect, true);
            }
        }
        Rectangle2D gnBounds = null;
        GraphicsNode gn    = ctx.getGraphicsNode(element);
        if (gn != null)
            gnBounds = gn.getSensitiveBounds();
        if (gnBounds == null) return false;
        AffineTransform at = gn.getGlobalTransform();
        at.preConcatenate(ati);
        gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
        if (!rect.intersects(gnBounds))
            return false;
        if (!(gn instanceof ShapeNode)) 
            return true;
        ShapeNode sn = (ShapeNode)gn;
        Shape sensitive = sn.getSensitiveArea();
        if (sensitive == null) return false;
        sensitive = at.createTransformedShape(sensitive);
        if (sensitive.intersects(rect))
            return true;
        return false;
    }
    public boolean checkEnclosure (Element element, SVGRect svgRect ) {
        GraphicsNode gn    = ctx.getGraphicsNode(element);
        Rectangle2D gnBounds = null;
        SVGContext svgctx = null;
        if (element instanceof SVGOMElement) {
            svgctx  = ((SVGOMElement)element).getSVGContext();
            if ((svgctx instanceof SVGTextElementBridge) ||
                (svgctx instanceof 
                 SVGTextElementBridge.AbstractTextChildSVGContext)) {
                gnBounds = SVGTextElementBridge.getTextBounds
                    (ctx, element, true);
                Element p = (Element)element.getParentNode();
                while ((p != null) && (gn == null)) {
                    gn = ctx.getGraphicsNode(p);
                    p = (Element)p.getParentNode();
                }
            } else if (gn != null) 
                gnBounds = gn.getSensitiveBounds();
        } else if (gn != null) 
            gnBounds = gn.getSensitiveBounds();
        if (gnBounds == null) return false;
        GraphicsNode svgGN = ctx.getGraphicsNode(e);
        if (svgGN == null) return false; 
        Rectangle2D rect = new Rectangle2D.Float
            (svgRect.getX(),     svgRect.getY(), 
             svgRect.getWidth(), svgRect.getHeight());
        AffineTransform ati = svgGN.getGlobalTransform();
        try {
            ati = ati.createInverse();
        } catch (NoninvertibleTransformException e) {  }
        AffineTransform at = gn.getGlobalTransform();
        at.preConcatenate(ati);
        gnBounds = at.createTransformedShape(gnBounds).getBounds2D();
        return rect.contains(gnBounds);
    }
    public boolean filterChildren(Element curr, Element end,
                                  Set elems, List ret) {
        Node child = curr.getFirstChild();
        while (child != null) {
            if ((child instanceof Element) &&
                filterChildren((Element)child, end, elems, ret))
                return true;
            child = child.getNextSibling();
        }
        if (curr == end) return true;
        if (elems.contains(curr))
            ret.add(curr);
        return false;
    }
    protected Set getAncestors(Element end, Element base) {
        Set ret = new HashSet();
        Element p = end;
        do {
            ret.add(p);
            p = (Element)p.getParentNode();
        } while ((p != null) && (p != base));
        if (p == null) 
            return null;
        return ret;
    }
    protected Element getNext(Element curr, Element base, Element end) {
        Node next;
        next = curr.getNextSibling();
        while (next != null) {
            if (next instanceof Element) 
                break;
            next = next.getNextSibling();
        }
        while (next == null) {
            curr = (Element)curr.getParentNode();
            if ((curr == end) || (curr == base)) {
                next = null; 
                break;
            }
            next = curr.getNextSibling();
            while (next != null) {
                if (next instanceof Element) 
                    break;
                next = next.getNextSibling();
            }
        }
        return (Element)next;
    }
    public void deselectAll() {
        ctx.getUserAgent().deselectAll();
    }
    public int          suspendRedraw ( int max_wait_milliseconds ) {
        UpdateManager um = ctx.getUpdateManager();
        if (um != null)
            return um.addRedrawSuspension(max_wait_milliseconds);
        return -1;
    }
    public boolean      unsuspendRedraw ( int suspend_handle_id ) {
        UpdateManager um = ctx.getUpdateManager();
        if (um != null)
            return um.releaseRedrawSuspension(suspend_handle_id);
        return false; 
    }
    public void         unsuspendRedrawAll (  ) {
        UpdateManager um = ctx.getUpdateManager();
        if (um != null)
            um.releaseAllRedrawSuspension();
    }
    public void          forceRedraw (  ) {
        UpdateManager um = ctx.getUpdateManager();
        if (um != null)
            um.forceRepaint();
    }
    public void pauseAnimations() {
        ctx.getAnimationEngine().pause();
    }
    public void unpauseAnimations() {
        ctx.getAnimationEngine().unpause();
    }
    public boolean animationsPaused() {
        return ctx.getAnimationEngine().isPaused();
    }
    public float getCurrentTime() {
        return ctx.getAnimationEngine().getCurrentTime();
    }
    public void setCurrentTime(float t) {
        ctx.getAnimationEngine().setCurrentTime(t);
    }
}
