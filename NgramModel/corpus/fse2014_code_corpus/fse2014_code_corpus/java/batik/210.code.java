package org.apache.batik.bridge;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGLinearGradientElementBridge
    extends AbstractSVGGradientElementBridge {
    public SVGLinearGradientElementBridge() {}
    public String getLocalName() {
        return SVG_LINEAR_GRADIENT_TAG;
    }
    protected
        Paint buildGradient(Element paintElement,
                            Element paintedElement,
                            GraphicsNode paintedNode,
                            MultipleGradientPaint.CycleMethodEnum spreadMethod,
                            MultipleGradientPaint.ColorSpaceEnum colorSpace,
                            AffineTransform transform,
                            Color [] colors,
                            float [] offsets,
                            BridgeContext ctx) {
        String x1Str = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_X1_ATTRIBUTE, ctx);
        if (x1Str.length() == 0) {
            x1Str = SVG_LINEAR_GRADIENT_X1_DEFAULT_VALUE;
        }
        String y1Str = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_Y1_ATTRIBUTE, ctx);
        if (y1Str.length() == 0) {
            y1Str = SVG_LINEAR_GRADIENT_Y1_DEFAULT_VALUE;
        }
        String x2Str = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_X2_ATTRIBUTE, ctx);
        if (x2Str.length() == 0) {
            x2Str = SVG_LINEAR_GRADIENT_X2_DEFAULT_VALUE;
        }
        String y2Str = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_Y2_ATTRIBUTE, ctx);
        if (y2Str.length() == 0) {
            y2Str = SVG_LINEAR_GRADIENT_Y2_DEFAULT_VALUE;
        }
        short coordSystemType;
        String s = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_GRADIENT_UNITS_ATTRIBUTE, ctx);
        if (s.length() == 0) {
            coordSystemType = SVGUtilities.OBJECT_BOUNDING_BOX;
        } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem
                (paintElement, SVG_GRADIENT_UNITS_ATTRIBUTE, s, ctx);
        }
        SVGContext bridge = BridgeContext.getSVGContext(paintedElement);
        if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX
                && bridge instanceof AbstractGraphicsNodeBridge) {
            Rectangle2D bbox = ((AbstractGraphicsNodeBridge) bridge).getBBox();
            if (bbox != null && (bbox.getWidth() == 0 || bbox.getHeight() == 0)) {
                return null;
            }
        }
        if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX) {
            transform = SVGUtilities.toObjectBBox(transform, paintedNode);
        }
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, paintElement);
        Point2D p1 = SVGUtilities.convertPoint(x1Str,
                                               SVG_X1_ATTRIBUTE,
                                               y1Str,
                                               SVG_Y1_ATTRIBUTE,
                                               coordSystemType,
                                               uctx);
        Point2D p2 = SVGUtilities.convertPoint(x2Str,
                                               SVG_X2_ATTRIBUTE,
                                               y2Str,
                                               SVG_Y2_ATTRIBUTE,
                                               coordSystemType,
                                               uctx);
        if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) {
            return colors[colors.length-1];
        } else {
            return new LinearGradientPaint(p1,
                                           p2,
                                           offsets,
                                           colors,
                                           spreadMethod,
                                           colorSpace,
                                           transform);
        }
    }
}
