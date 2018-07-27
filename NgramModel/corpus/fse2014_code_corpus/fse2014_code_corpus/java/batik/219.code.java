package org.apache.batik.bridge;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGRadialGradientElementBridge
    extends AbstractSVGGradientElementBridge {
    public SVGRadialGradientElementBridge() {}
    public String getLocalName() {
        return SVG_RADIAL_GRADIENT_TAG;
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
        String cxStr = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_CX_ATTRIBUTE, ctx);
        if (cxStr.length() == 0) {
            cxStr = SVG_RADIAL_GRADIENT_CX_DEFAULT_VALUE;
        }
        String cyStr = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_CY_ATTRIBUTE, ctx);
        if (cyStr.length() == 0) {
            cyStr = SVG_RADIAL_GRADIENT_CY_DEFAULT_VALUE;
        }
        String rStr = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_R_ATTRIBUTE, ctx);
        if (rStr.length() == 0) {
            rStr = SVG_RADIAL_GRADIENT_R_DEFAULT_VALUE;
        }
        String fxStr = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_FX_ATTRIBUTE, ctx);
        if (fxStr.length() == 0) {
            fxStr = cxStr;
        }
        String fyStr = SVGUtilities.getChainableAttributeNS
            (paintElement, null, SVG_FY_ATTRIBUTE, ctx);
        if (fyStr.length() == 0) {
            fyStr = cyStr;
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
            transform = SVGUtilities.toObjectBBox(transform,
                                                  paintedNode);
        }
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, paintElement);
        float r = SVGUtilities.convertLength(rStr,
                                             SVG_R_ATTRIBUTE,
                                             coordSystemType,
                                             uctx);
        if (r == 0) {
            return colors[colors.length-1];
        } else {
            Point2D c = SVGUtilities.convertPoint(cxStr,
                                                  SVG_CX_ATTRIBUTE,
                                                  cyStr,
                                                  SVG_CY_ATTRIBUTE,
                                                  coordSystemType,
                                                  uctx);
            Point2D f = SVGUtilities.convertPoint(fxStr,
                                                  SVG_FX_ATTRIBUTE,
                                                  fyStr,
                                                  SVG_FY_ATTRIBUTE,
                                                  coordSystemType,
                                                  uctx);
            return new RadialGradientPaint(c,
                                           r,
                                           f,
                                           offsets,
                                           colors,
                                           spreadMethod,
                                           RadialGradientPaint.SRGB,
                                           transform);
        }
    }
}
