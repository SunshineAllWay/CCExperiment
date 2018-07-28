package org.apache.batik.bridge;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.apache.batik.ext.awt.color.ICCColorSpaceExt;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.Marker;
import org.apache.batik.gvt.MarkerShapePainter;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
public abstract class PaintServer
    implements SVGConstants, CSSConstants, ErrorConstants {
    protected PaintServer() {}
    public static ShapePainter convertMarkers(Element e,
                                              ShapeNode node,
                                              BridgeContext ctx) {
        Value v;
        v = CSSUtilities.getComputedStyle(e, SVGCSSEngine.MARKER_START_INDEX);
        Marker startMarker = convertMarker(e, v, ctx);
        v = CSSUtilities.getComputedStyle(e, SVGCSSEngine.MARKER_MID_INDEX);
        Marker midMarker = convertMarker(e, v, ctx);
        v = CSSUtilities.getComputedStyle(e, SVGCSSEngine.MARKER_END_INDEX);
        Marker endMarker = convertMarker(e, v, ctx);
        if (startMarker != null || midMarker != null || endMarker != null) {
            MarkerShapePainter p = new MarkerShapePainter(node.getShape());
            p.setStartMarker(startMarker);
            p.setMiddleMarker(midMarker);
            p.setEndMarker(endMarker);
            return p;
        } else {
            return null;
        }
    }
    public static Marker convertMarker(Element e,
                                       Value v,
                                       BridgeContext ctx) {
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            return null; 
        } else {
            String uri = v.getStringValue();
            Element markerElement = ctx.getReferencedElement(e, uri);
            Bridge bridge = ctx.getBridge(markerElement);
            if (bridge == null || !(bridge instanceof MarkerBridge)) {
                throw new BridgeException(ctx, e, ERR_CSS_URI_BAD_TARGET,
                                          new Object[] {uri});
            }
            return ((MarkerBridge)bridge).createMarker(ctx, markerElement, e);
        }
    }
    public static ShapePainter convertFillAndStroke(Element e,
                                                    ShapeNode node,
                                                    BridgeContext ctx) {
        Shape shape = node.getShape();
        if (shape == null) return null;
        Paint  fillPaint   = convertFillPaint  (e, node, ctx);
        FillShapePainter fp = new FillShapePainter(shape);
        fp.setPaint(fillPaint);
        Stroke stroke      = convertStroke     (e);
        if (stroke == null)
            return fp;
        Paint  strokePaint = convertStrokePaint(e, node, ctx);
        StrokeShapePainter sp = new StrokeShapePainter(shape);
        sp.setStroke(stroke);
        sp.setPaint(strokePaint);
        CompositeShapePainter cp = new CompositeShapePainter(shape);
        cp.addShapePainter(fp);
        cp.addShapePainter(sp);
        return cp;
    }
    public static ShapePainter convertStrokePainter(Element e,
                                                    ShapeNode node,
                                                    BridgeContext ctx) {
        Shape shape = node.getShape();
        if (shape == null) return null;
        Stroke stroke = convertStroke(e);
        if (stroke == null)
            return null;
        Paint  strokePaint = convertStrokePaint(e, node, ctx);
        StrokeShapePainter sp = new StrokeShapePainter(shape);
        sp.setStroke(stroke);
        sp.setPaint(strokePaint);
        return sp;
    }
    public static Paint convertStrokePaint(Element strokedElement,
                                           GraphicsNode strokedNode,
                                           BridgeContext ctx) {
        Value v = CSSUtilities.getComputedStyle
            (strokedElement, SVGCSSEngine.STROKE_OPACITY_INDEX);
        float opacity = convertOpacity(v);
        v = CSSUtilities.getComputedStyle
            (strokedElement, SVGCSSEngine.STROKE_INDEX);
        return convertPaint(strokedElement,
                            strokedNode,
                            v,
                            opacity,
                            ctx);
    }
    public static Paint convertFillPaint(Element filledElement,
                                         GraphicsNode filledNode,
                                         BridgeContext ctx) {
        Value v = CSSUtilities.getComputedStyle
            (filledElement, SVGCSSEngine.FILL_OPACITY_INDEX);
        float opacity = convertOpacity(v);
        v = CSSUtilities.getComputedStyle
            (filledElement, SVGCSSEngine.FILL_INDEX);
        return convertPaint(filledElement,
                            filledNode,
                            v,
                            opacity,
                            ctx);
    }
    public static Paint convertPaint(Element paintedElement,
                                        GraphicsNode paintedNode,
                                        Value paintDef,
                                        float opacity,
                                        BridgeContext ctx) {
        if (paintDef.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            switch (paintDef.getPrimitiveType()) {
            case CSSPrimitiveValue.CSS_IDENT:
                return null; 
            case CSSPrimitiveValue.CSS_RGBCOLOR:
                return convertColor(paintDef, opacity);
            case CSSPrimitiveValue.CSS_URI:
                return convertURIPaint(paintedElement,
                                       paintedNode,
                                       paintDef,
                                       opacity,
                                       ctx);
            default:
                throw new IllegalArgumentException
                    ("Paint argument is not an appropriate CSS value");
            }
        } else { 
            Value v = paintDef.item(0);
            switch (v.getPrimitiveType()) {
            case CSSPrimitiveValue.CSS_RGBCOLOR:
                return convertRGBICCColor(paintedElement, v,
                                          (ICCColor)paintDef.item(1),
                                          opacity, ctx);
            case CSSPrimitiveValue.CSS_URI: {
                Paint result = silentConvertURIPaint(paintedElement,
                                                     paintedNode,
                                                     v, opacity, ctx);
                if (result != null) return result;
                v = paintDef.item(1);
                switch (v.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_IDENT:
                    return null; 
                case CSSPrimitiveValue.CSS_RGBCOLOR:
                    if (paintDef.getLength() == 2) {
                        return convertColor(v, opacity);
                    } else {
                        return convertRGBICCColor(paintedElement, v,
                                                  (ICCColor)paintDef.item(2),
                                                  opacity, ctx);
                    }
                default:
                    throw new IllegalArgumentException
                        ("Paint argument is not an appropriate CSS value");
                }
            }
            default:
                throw new IllegalArgumentException
                    ("Paint argument is not an appropriate CSS value");
            }
        }
    }
    public static Paint silentConvertURIPaint(Element paintedElement,
                                              GraphicsNode paintedNode,
                                              Value paintDef,
                                              float opacity,
                                              BridgeContext ctx) {
        Paint paint = null;
        try {
            paint = convertURIPaint(paintedElement, paintedNode,
                                    paintDef, opacity, ctx);
        } catch (BridgeException ex) {
        }
        return paint;
    }
    public static Paint convertURIPaint(Element paintedElement,
                                        GraphicsNode paintedNode,
                                        Value paintDef,
                                        float opacity,
                                        BridgeContext ctx) {
        String uri = paintDef.getStringValue();
        Element paintElement = ctx.getReferencedElement(paintedElement, uri);
        Bridge bridge = ctx.getBridge(paintElement);
        if (bridge == null || !(bridge instanceof PaintBridge)) {
            throw new BridgeException
                (ctx, paintedElement, ERR_CSS_URI_BAD_TARGET,
                 new Object[] {uri});
        }
        return ((PaintBridge)bridge).createPaint(ctx,
                                                 paintElement,
                                                 paintedElement,
                                                 paintedNode,
                                                 opacity);
    }
    public static Color convertRGBICCColor(Element paintedElement,
                                           Value colorDef,
                                           ICCColor iccColor,
                                           float opacity,
                                           BridgeContext ctx) {
        Color color = null;
        if (iccColor != null){
            color = convertICCColor(paintedElement, iccColor, opacity, ctx);
        }
        if (color == null){
            color = convertColor(colorDef, opacity);
        }
        return color;
    }
    public static Color convertICCColor(Element e,
                                        ICCColor c,
                                        float opacity,
                                        BridgeContext ctx){
        String iccProfileName = c.getColorProfile();
        if (iccProfileName == null){
            return null;
        }
        SVGColorProfileElementBridge profileBridge
            = (SVGColorProfileElementBridge)
            ctx.getBridge(SVG_NAMESPACE_URI, SVG_COLOR_PROFILE_TAG);
        if (profileBridge == null){
            return null; 
        }
        ICCColorSpaceExt profileCS
            = profileBridge.createICCColorSpaceExt(ctx, e, iccProfileName);
        if (profileCS == null){
            return null; 
        }
        int n = c.getNumberOfColors();
        float[] colorValue = new float[n];
        if (n == 0) {
            return null;
        }
        for (int i = 0; i < n; i++) {
            colorValue[i] = c.getColor(i);
        }
        float[] rgb = profileCS.intendedToRGB(colorValue);
        return new Color(rgb[0], rgb[1], rgb[2], opacity);
    }
    public static Color convertColor(Value c, float opacity) {
        int r = resolveColorComponent(c.getRed());
        int g = resolveColorComponent(c.getGreen());
        int b = resolveColorComponent(c.getBlue());
        return new Color(r, g, b, Math.round(opacity * 255f));
    }
    public static Stroke convertStroke(Element e) {
        Value v;
        v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.STROKE_WIDTH_INDEX);
        float width = v.getFloatValue();
        if (width == 0.0f)
            return null; 
        v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.STROKE_LINECAP_INDEX);
        int linecap = convertStrokeLinecap(v);
        v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.STROKE_LINEJOIN_INDEX);
        int linejoin = convertStrokeLinejoin(v);
        v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.STROKE_MITERLIMIT_INDEX);
        float miterlimit = convertStrokeMiterlimit(v);
        v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.STROKE_DASHARRAY_INDEX);
        float[] dasharray = convertStrokeDasharray(v);
        float dashoffset = 0;
        if (dasharray != null) {
            v =  CSSUtilities.getComputedStyle
                (e, SVGCSSEngine.STROKE_DASHOFFSET_INDEX);
            dashoffset = v.getFloatValue();
            if ( dashoffset < 0 ) {
                float dashpatternlength = 0;
                for ( int i=0; i<dasharray.length; i++ ) {
                    dashpatternlength += dasharray[i];
                }
                if ( (dasharray.length % 2) != 0 )
                    dashpatternlength *= 2;
                if (dashpatternlength ==0) {
                    dashoffset=0;
                } else {
                    while (dashoffset < 0)
                        dashoffset += dashpatternlength;
                }
            }
        }
        return new BasicStroke(width,
                               linecap,
                               linejoin,
                               miterlimit,
                               dasharray,
                               dashoffset);
    }
    public static float [] convertStrokeDasharray(Value v) {
        float [] dasharray = null;
        if (v.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            int length = v.getLength();
            dasharray = new float[length];
            float sum = 0;
            for (int i = 0; i < dasharray.length; ++i) {
                dasharray[i] = v.item(i).getFloatValue();
                sum += dasharray[i];
            }
            if (sum == 0) {
                dasharray = null;
            }
        }
        return dasharray;
    }
    public static float convertStrokeMiterlimit(Value v) {
        float miterlimit = v.getFloatValue();
        return (miterlimit < 1.0f) ? 1.0f : miterlimit;
    }
    public static int convertStrokeLinecap(Value v) {
        String s = v.getStringValue();
        switch (s.charAt(0)) {
        case 'b':
            return BasicStroke.CAP_BUTT;
        case 'r':
            return BasicStroke.CAP_ROUND;
        case 's':
            return BasicStroke.CAP_SQUARE;
        default:
            throw new IllegalArgumentException
                ("Linecap argument is not an appropriate CSS value");
        }
    }
    public static int convertStrokeLinejoin(Value v) {
        String s = v.getStringValue();
        switch (s.charAt(0)) {
        case 'm':
            return BasicStroke.JOIN_MITER;
        case 'r':
            return BasicStroke.JOIN_ROUND;
        case 'b':
            return BasicStroke.JOIN_BEVEL;
        default:
            throw new IllegalArgumentException
                ("Linejoin argument is not an appropriate CSS value");
        }
    }
    public static int resolveColorComponent(Value v) {
        float f;
        switch(v.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_PERCENTAGE:
            f = v.getFloatValue();
            f = (f > 100f) ? 100f : (f < 0f) ? 0f : f;
            return Math.round(255f * f / 100f);
        case CSSPrimitiveValue.CSS_NUMBER:
            f = v.getFloatValue();
            f = (f > 255f) ? 255f : (f < 0f) ? 0f : f;
            return Math.round(f);
        default:
            throw new IllegalArgumentException
                ("Color component argument is not an appropriate CSS value");
        }
    }
    public static float convertOpacity(Value v) {
        float r = v.getFloatValue();
        return (r < 0f) ? 0f : (r > 1.0f) ? 1.0f : r;
    }
}
