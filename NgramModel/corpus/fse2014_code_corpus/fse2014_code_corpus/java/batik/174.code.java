package org.apache.batik.bridge;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMCircleElement;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;
public class SVGCircleElementBridge extends SVGShapeElementBridge {
    public SVGCircleElementBridge() {}
    public String getLocalName() {
        return SVG_CIRCLE_TAG;
    }
    public Bridge getInstance() {
        return new SVGCircleElementBridge();
    }
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) {
        try {
            SVGOMCircleElement ce = (SVGOMCircleElement) e;
            AbstractSVGAnimatedLength _cx =
                (AbstractSVGAnimatedLength) ce.getCx();
            float cx = _cx.getCheckedValue();
            AbstractSVGAnimatedLength _cy =
                (AbstractSVGAnimatedLength) ce.getCy();
            float cy = _cy.getCheckedValue();
            AbstractSVGAnimatedLength _r =
                (AbstractSVGAnimatedLength) ce.getR();
            float r = _r.getCheckedValue();
            float x = cx - r;
            float y = cy - r;
            float w = r * 2;
            shapeNode.setShape(new Ellipse2D.Float(x, y, w, w));
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals(SVG_CX_ATTRIBUTE)
                    || ln.equals(SVG_CY_ATTRIBUTE)
                    || ln.equals(SVG_R_ATTRIBUTE)) {
                buildShape(ctx, e, (ShapeNode)node);
                handleGeometryChanged();
                return;
            }
        }
        super.handleAnimatedAttributeChanged(alav);
    }
    protected ShapePainter createShapePainter(BridgeContext ctx,
                                              Element e,
                                              ShapeNode shapeNode) {
        Rectangle2D r2d = shapeNode.getShape().getBounds2D();
        if ((r2d.getWidth() == 0) || (r2d.getHeight() == 0))
            return null;
        return super.createShapePainter(ctx, e, shapeNode);
    }
}
