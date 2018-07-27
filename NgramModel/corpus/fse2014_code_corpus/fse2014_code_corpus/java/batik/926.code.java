package org.apache.batik.extension.svg;
import java.awt.geom.GeneralPath;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.SVGDecoratedShapeElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;
public class BatikRegularPolygonElementBridge
    extends SVGDecoratedShapeElementBridge
    implements BatikExtConstants {
    public BatikRegularPolygonElementBridge() {  }
    public String getNamespaceURI() {
        return BATIK_EXT_NAMESPACE_URI;
    }
    public String getLocalName() {
        return BATIK_EXT_REGULAR_POLYGON_TAG;
    }
    public Bridge getInstance() {
        return new BatikRegularPolygonElementBridge();
    }
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) {
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, e);
        String s;
        s = e.getAttributeNS(null, SVG_CX_ATTRIBUTE);
        float cx = 0;
        if (s.length() != 0) {
            cx = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (s, SVG_CX_ATTRIBUTE, uctx);
        }
        s = e.getAttributeNS(null, SVG_CY_ATTRIBUTE);
        float cy = 0;
        if (s.length() != 0) {
            cy = UnitProcessor.svgVerticalCoordinateToUserSpace
                (s, SVG_CY_ATTRIBUTE, uctx);
        }
        s = e.getAttributeNS(null, SVG_R_ATTRIBUTE);
        float r;
        if (s.length() != 0) {
            r = UnitProcessor.svgOtherLengthToUserSpace
                (s, SVG_R_ATTRIBUTE, uctx);
        } else {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {SVG_R_ATTRIBUTE, s});
        }
        int sides = convertSides(e, BATIK_EXT_SIDES_ATTRIBUTE, 3, ctx);
        GeneralPath gp = new GeneralPath();
        for (int i=0; i<sides; i++) {
            double angle    = (i+0.5)*(2*Math.PI/sides) - (Math.PI/2);
            double x = cx + r*Math.cos(angle);
            double y = cy - r*Math.sin(angle);
            if (i==0)
                gp.moveTo((float)x, (float)y);
            else
                gp.lineTo((float)x, (float)y);
        }
        gp.closePath();
        shapeNode.setShape(gp);
    }
    protected static int convertSides(Element filterElement,
                                      String attrName,
                                      int defaultValue,
                                      BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, attrName);
        if (s.length() == 0) {
            return defaultValue;
        } else {
            int ret = 0;
            try {
                ret = SVGUtilities.convertSVGInteger(s);
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {attrName, s});
            }
            if (ret <3)
                throw new BridgeException
                    (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {attrName, s});
            return ret;
        }
    }
}
