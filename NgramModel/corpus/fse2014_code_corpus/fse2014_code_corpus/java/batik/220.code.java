package org.apache.batik.bridge;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;
public class SVGRectElementBridge extends SVGShapeElementBridge {
    public SVGRectElementBridge() {}
    public String getLocalName() {
        return SVG_RECT_TAG;
    }
    public Bridge getInstance() {
        return new SVGRectElementBridge();
    }
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) {
        try {
            SVGOMRectElement re = (SVGOMRectElement) e;
            AbstractSVGAnimatedLength _x =
                (AbstractSVGAnimatedLength) re.getX();
            float x = _x.getCheckedValue();
            AbstractSVGAnimatedLength _y =
                (AbstractSVGAnimatedLength) re.getY();
            float y = _y.getCheckedValue();
            AbstractSVGAnimatedLength _width =
                (AbstractSVGAnimatedLength) re.getWidth();
            float w = _width.getCheckedValue();
            AbstractSVGAnimatedLength _height =
                (AbstractSVGAnimatedLength) re.getHeight();
            float h = _height.getCheckedValue();
            AbstractSVGAnimatedLength _rx =
                (AbstractSVGAnimatedLength) re.getRx();
            float rx = _rx.getCheckedValue();
            if (rx > w / 2) {
                rx = w / 2;
            }
            AbstractSVGAnimatedLength _ry =
                (AbstractSVGAnimatedLength) re.getRy();
            float ry = _ry.getCheckedValue();
            if (ry > h / 2) {
                ry = h / 2;
            }
            Shape shape;
            if (rx == 0 || ry == 0) {
                shape = new Rectangle2D.Float(x, y, w, h);
            } else {
                shape = new RoundRectangle2D.Float(x, y, w, h, rx * 2, ry * 2);
            }
            shapeNode.setShape(shape);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals(SVG_X_ATTRIBUTE)
                    || ln.equals(SVG_Y_ATTRIBUTE)
                    || ln.equals(SVG_WIDTH_ATTRIBUTE)
                    || ln.equals(SVG_HEIGHT_ATTRIBUTE)
                    || ln.equals(SVG_RX_ATTRIBUTE)
                    || ln.equals(SVG_RY_ATTRIBUTE)) {
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
        Shape shape = shapeNode.getShape();
        Rectangle2D r2d = shape.getBounds2D();
        if ((r2d.getWidth() == 0) || (r2d.getHeight() == 0))
            return null;
        return super.createShapePainter(ctx, e, shapeNode);
    }
}
