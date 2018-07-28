package org.apache.batik.bridge;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Element;
public abstract class UnitProcessor
    extends org.apache.batik.parser.UnitProcessor {
    public static Context createContext(BridgeContext ctx, Element e) {
        return new DefaultContext(ctx, e);
    }
    public static
        float svgHorizontalCoordinateToObjectBoundingBox(String s,
                                                         String attr,
                                                         Context ctx) {
        return svgToObjectBoundingBox(s, attr, HORIZONTAL_LENGTH, ctx);
    }
    public static
        float svgVerticalCoordinateToObjectBoundingBox(String s,
                                                       String attr,
                                                       Context ctx) {
        return svgToObjectBoundingBox(s, attr, VERTICAL_LENGTH, ctx);
    }
    public static
        float svgOtherCoordinateToObjectBoundingBox(String s,
                                                    String attr,
                                                    Context ctx) {
        return svgToObjectBoundingBox(s, attr, OTHER_LENGTH, ctx);
    }
    public static
        float svgHorizontalLengthToObjectBoundingBox(String s,
                                                     String attr,
                                                     Context ctx) {
        return svgLengthToObjectBoundingBox(s, attr, HORIZONTAL_LENGTH, ctx);
    }
    public static
        float svgVerticalLengthToObjectBoundingBox(String s,
                                                   String attr,
                                                   Context ctx) {
        return svgLengthToObjectBoundingBox(s, attr, VERTICAL_LENGTH, ctx);
    }
    public static
        float svgOtherLengthToObjectBoundingBox(String s,
                                                String attr,
                                                Context ctx) {
        return svgLengthToObjectBoundingBox(s, attr, OTHER_LENGTH, ctx);
    }
    public static float svgLengthToObjectBoundingBox(String s,
                                                     String attr,
                                                     short d,
                                                     Context ctx) {
        float v = svgToObjectBoundingBox(s, attr, d, ctx);
        if (v < 0) {
            throw new BridgeException(getBridgeContext(ctx), ctx.getElement(),
                                      ErrorConstants.ERR_LENGTH_NEGATIVE,
                                      new Object[] {attr, s});
        }
        return v;
    }
    public static float svgToObjectBoundingBox(String s,
                                               String attr,
                                               short d,
                                               Context ctx) {
        try {
            return org.apache.batik.parser.UnitProcessor.
                svgToObjectBoundingBox(s, attr, d, ctx);
        } catch (ParseException pEx ) {
            throw new BridgeException
                (getBridgeContext(ctx), ctx.getElement(),
                 pEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {attr, s, pEx });
        }
    }
    public static float svgHorizontalLengthToUserSpace(String s,
                                                       String attr,
                                                       Context ctx) {
        return svgLengthToUserSpace(s, attr, HORIZONTAL_LENGTH, ctx);
    }
    public static float svgVerticalLengthToUserSpace(String s,
                                                     String attr,
                                                     Context ctx) {
        return svgLengthToUserSpace(s, attr, VERTICAL_LENGTH, ctx);
    }
    public static float svgOtherLengthToUserSpace(String s,
                                                  String attr,
                                                  Context ctx) {
        return svgLengthToUserSpace(s, attr, OTHER_LENGTH, ctx);
    }
    public static float svgHorizontalCoordinateToUserSpace(String s,
                                                           String attr,
                                                           Context ctx) {
        return svgToUserSpace(s, attr, HORIZONTAL_LENGTH, ctx);
    }
    public static float svgVerticalCoordinateToUserSpace(String s,
                                                         String attr,
                                                         Context ctx) {
        return svgToUserSpace(s, attr, VERTICAL_LENGTH, ctx);
    }
    public static float svgOtherCoordinateToUserSpace(String s,
                                                      String attr,
                                                      Context ctx) {
        return svgToUserSpace(s, attr, OTHER_LENGTH, ctx);
    }
    public static float svgLengthToUserSpace(String s,
                                             String attr,
                                             short d,
                                             Context ctx) {
        float v = svgToUserSpace(s, attr, d, ctx);
        if (v < 0) {
            throw new BridgeException(getBridgeContext(ctx), ctx.getElement(),
                                      ErrorConstants.ERR_LENGTH_NEGATIVE,
                                      new Object[] {attr, s});
        } else {
            return v;
        }
    }
    public static float svgToUserSpace(String s,
                                       String attr,
                                       short d,
                                       Context ctx) {
        try {
            return org.apache.batik.parser.UnitProcessor.
                svgToUserSpace(s, attr, d, ctx);
        } catch (ParseException pEx ) {
            throw new BridgeException
                (getBridgeContext(ctx), ctx.getElement(),
                 pEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {attr, s, pEx, });
        }
    }
    protected static BridgeContext getBridgeContext(Context ctx) {
        if (ctx instanceof DefaultContext) {
            return ((DefaultContext) ctx).ctx;
        }
        return null;
    }
    public static class DefaultContext implements Context {
        protected Element e;
        protected BridgeContext ctx;
        public DefaultContext(BridgeContext ctx, Element e) {
            this.ctx = ctx;
            this.e = e;
        }
        public Element getElement() {
            return e;
        }
        public float getPixelUnitToMillimeter() {
            return ctx.getUserAgent().getPixelUnitToMillimeter();
        }
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
        }
        public float getFontSize() {
            return CSSUtilities.getComputedStyle
                (e, SVGCSSEngine.FONT_SIZE_INDEX).getFloatValue();
        }
        public float getXHeight() {
            return 0.5f;
        }
        public float getViewportWidth() {
            return ctx.getViewport(e).getWidth();
        }
        public float getViewportHeight() {
            return ctx.getViewport(e).getHeight();
        }
    }
}
