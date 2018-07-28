package org.apache.batik.bridge;
import java.awt.RenderingHints;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;
public abstract class SVGShapeElementBridge extends AbstractGraphicsNodeBridge {
    protected SVGShapeElementBridge() {}
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        ShapeNode shapeNode = (ShapeNode)super.createGraphicsNode(ctx, e);
        if (shapeNode == null) {
            return null;
        }
        associateSVGContext(ctx, e, shapeNode);
        buildShape(ctx, e, shapeNode);
        RenderingHints hints = null;
        hints = CSSUtilities.convertColorRendering(e, hints);
        hints = CSSUtilities.convertShapeRendering(e, hints);
        if (hints != null)
            shapeNode.setRenderingHints(hints);
        return shapeNode;
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return new ShapeNode();
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        ShapeNode shapeNode = (ShapeNode)node;
        shapeNode.setShapePainter(createShapePainter(ctx, e, shapeNode));
        super.buildGraphicsNode(ctx, e, node);
    }
    protected ShapePainter createShapePainter(BridgeContext ctx,
                                              Element e,
                                              ShapeNode shapeNode) {
        return PaintServer.convertFillAndStroke(e, shapeNode, ctx);
    }
    protected abstract void buildShape(BridgeContext ctx,
                                       Element e,
                                       ShapeNode node);
    public boolean isComposite() {
        return false;
    }
    protected void handleGeometryChanged() {
        super.handleGeometryChanged();
        ShapeNode shapeNode = (ShapeNode)node;
        shapeNode.setShapePainter(createShapePainter(ctx, e, shapeNode));
    }
    protected boolean hasNewShapePainter;
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
        hasNewShapePainter = false;
        super.handleCSSEngineEvent(evt);
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {
        case SVGCSSEngine.FILL_INDEX:
        case SVGCSSEngine.FILL_OPACITY_INDEX:
        case SVGCSSEngine.STROKE_INDEX:
        case SVGCSSEngine.STROKE_OPACITY_INDEX:
        case SVGCSSEngine.STROKE_WIDTH_INDEX:
        case SVGCSSEngine.STROKE_LINECAP_INDEX:
        case SVGCSSEngine.STROKE_LINEJOIN_INDEX:
        case SVGCSSEngine.STROKE_MITERLIMIT_INDEX:
        case SVGCSSEngine.STROKE_DASHARRAY_INDEX:
        case SVGCSSEngine.STROKE_DASHOFFSET_INDEX: {
            if (!hasNewShapePainter) {
                hasNewShapePainter = true;
                ShapeNode shapeNode = (ShapeNode)node;
                shapeNode.setShapePainter(createShapePainter(ctx, e, shapeNode));
            }
            break;
        }
        case SVGCSSEngine.SHAPE_RENDERING_INDEX: {
            RenderingHints hints = node.getRenderingHints();
            hints = CSSUtilities.convertShapeRendering(e, hints);
            if (hints != null) {
                node.setRenderingHints(hints);
            }
            break;
          }
        case SVGCSSEngine.COLOR_RENDERING_INDEX: {
            RenderingHints hints = node.getRenderingHints();
            hints = CSSUtilities.convertColorRendering(e, hints);
            if (hints != null) {
                node.setRenderingHints(hints);
            }
            break;
        } 
        default:
            super.handleCSSPropertyChanged(property);
        }
    }
}
