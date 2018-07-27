package org.apache.batik.bridge;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.FilterAlphaRable;
import org.apache.batik.ext.awt.image.renderable.FilterColorInterpolation;
import org.apache.batik.ext.awt.image.renderable.FloodRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.BackgroundRable8Bit;
import org.w3c.dom.Element;
public abstract class AbstractSVGFilterPrimitiveElementBridge
        extends AnimatableGenericSVGBridge
        implements FilterPrimitiveBridge, ErrorConstants {
    protected AbstractSVGFilterPrimitiveElementBridge() {}
    protected static Filter getIn(Element filterElement,
                                  Element filteredElement,
                                  GraphicsNode filteredNode,
                                  Filter inputFilter,
                                  Map filterMap,
                                  BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_IN_ATTRIBUTE);
        if (s.length() == 0) {
            return inputFilter;
        } else {
            return getFilterSource(filterElement,
                                   s,
                                   filteredElement,
                                   filteredNode,
                                   filterMap,
                                   ctx);
        }
    }
    protected static Filter getIn2(Element filterElement,
                                   Element filteredElement,
                                   GraphicsNode filteredNode,
                                   Filter inputFilter,
                                   Map filterMap,
                                   BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, SVG_IN2_ATTRIBUTE);
        if (s.length() == 0) {
            throw new BridgeException(ctx, filterElement, ERR_ATTRIBUTE_MISSING,
                                      new Object [] {SVG_IN2_ATTRIBUTE});
        }
        return getFilterSource(filterElement,
                               s,
                               filteredElement,
                               filteredNode,
                               filterMap,
                               ctx);
    }
    protected static void updateFilterMap(Element filterElement,
                                          Filter filter,
                                          Map filterMap) {
        String s = filterElement.getAttributeNS(null, SVG_RESULT_ATTRIBUTE);
        if ((s.length() != 0) && (s.trim().length() != 0)) {
            filterMap.put(s, filter);
        }
    }
    protected static void handleColorInterpolationFilters(Filter filter,
                                                          Element filterElement) {
        if (filter instanceof FilterColorInterpolation) {
            boolean isLinear
                = CSSUtilities.convertColorInterpolationFilters(filterElement);
            ((FilterColorInterpolation)filter).setColorSpaceLinear(isLinear);
        }
    }
    static Filter getFilterSource(Element filterElement,
                                  String s,
                                  Element filteredElement,
                                  GraphicsNode filteredNode,
                                  Map filterMap,
                                  BridgeContext ctx) {
        Filter srcG = (Filter)filterMap.get(SVG_SOURCE_GRAPHIC_VALUE);
        Rectangle2D filterRegion = srcG.getBounds2D();
        int length = s.length();
        Filter source = null;
        switch (length) {
        case 13:
            if (SVG_SOURCE_GRAPHIC_VALUE.equals(s)) {
                source = srcG;
            }
            break;
        case 11:
            if (s.charAt(1) == SVG_SOURCE_ALPHA_VALUE.charAt(1)) {
                if (SVG_SOURCE_ALPHA_VALUE.equals(s)) {
                    source = srcG;
                    source = new FilterAlphaRable(source);
                }
            } else if (SVG_STROKE_PAINT_VALUE.equals(s)) {
                    Paint paint = PaintServer.convertStrokePaint
                        (filteredElement,filteredNode, ctx);
                    source = new FloodRable8Bit(filterRegion, paint);
            }
            break;
        case 15:
            if (s.charAt(10) == SVG_BACKGROUND_IMAGE_VALUE.charAt(10)) {
                if (SVG_BACKGROUND_IMAGE_VALUE.equals(s)) {
                    source = new BackgroundRable8Bit(filteredNode);
                    source = new PadRable8Bit(source, filterRegion,
                                              PadMode.ZERO_PAD);
                }
            } else if (SVG_BACKGROUND_ALPHA_VALUE.equals(s)) {
                source = new BackgroundRable8Bit(filteredNode);
                source = new FilterAlphaRable(source);
                source = new PadRable8Bit(source, filterRegion,
                                          PadMode.ZERO_PAD);
            }
            break;
        case 9:
            if (SVG_FILL_PAINT_VALUE.equals(s)) {
                Paint paint = PaintServer.convertFillPaint
                    (filteredElement,filteredNode, ctx);
                if (paint == null) {
                    paint = new Color(0, 0, 0, 0); 
                }
                source = new FloodRable8Bit(filterRegion, paint);
            }
            break;
        }
        if (source == null) {
            source = (Filter)filterMap.get(s);
        }
        return source;
    }
    static final Rectangle2D INFINITE_FILTER_REGION
        = new Rectangle2D.Float(-Float.MAX_VALUE/2,
                                -Float.MAX_VALUE/2,
                                Float.MAX_VALUE,
                                Float.MAX_VALUE);
    protected static int convertInteger(Element filterElement,
                                        String attrName,
                                        int defaultValue,
                                        BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, attrName);
        if (s.length() == 0) {
            return defaultValue;
        } else {
            try {
                return SVGUtilities.convertSVGInteger(s);
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {attrName, s});
            }
        }
    }
    protected static float convertNumber(Element filterElement,
                                         String attrName,
                                         float defaultValue,
                                         BridgeContext ctx) {
        String s = filterElement.getAttributeNS(null, attrName);
        if (s.length() == 0) {
            return defaultValue;
        } else {
            try {
                return SVGUtilities.convertSVGNumber(s);
            } catch (NumberFormatException nfEx) {
                throw new BridgeException
                    (ctx, filterElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {attrName, s, nfEx});
            }
        }
    }
}
