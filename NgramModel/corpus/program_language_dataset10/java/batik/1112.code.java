package org.apache.batik.parser;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLength;
public abstract class UnitProcessor {
    public static final short HORIZONTAL_LENGTH = 2;
    public static final short VERTICAL_LENGTH = 1;
    public static final short OTHER_LENGTH = 0;
    static final double SQRT2 = Math.sqrt( 2.0 );
    protected UnitProcessor() { }
    public static float svgToObjectBoundingBox(String s,
                                               String attr,
                                               short d,
                                               Context ctx)
        throws ParseException {
        LengthParser lengthParser = new LengthParser();
        UnitResolver ur = new UnitResolver();
        lengthParser.setLengthHandler(ur);
        lengthParser.parse(s);
        return svgToObjectBoundingBox(ur.value, ur.unit, d, ctx);
    }
    public static float svgToObjectBoundingBox(float value,
                                               short type,
                                               short d,
                                               Context ctx) {
        switch (type) {
        case SVGLength.SVG_LENGTHTYPE_NUMBER:
            return value;
        case SVGLength.SVG_LENGTHTYPE_PERCENTAGE:
            return value / 100f;
        case SVGLength.SVG_LENGTHTYPE_PX:
        case SVGLength.SVG_LENGTHTYPE_MM:
        case SVGLength.SVG_LENGTHTYPE_CM:
        case SVGLength.SVG_LENGTHTYPE_IN:
        case SVGLength.SVG_LENGTHTYPE_PT:
        case SVGLength.SVG_LENGTHTYPE_PC:
        case SVGLength.SVG_LENGTHTYPE_EMS:
        case SVGLength.SVG_LENGTHTYPE_EXS:
            return svgToUserSpace(value, type, d, ctx);
        default:
            throw new IllegalArgumentException("Length has unknown type");
        }
    }
    public static float svgToUserSpace(String s,
                                       String attr,
                                       short d,
                                       Context ctx) throws ParseException {
        LengthParser lengthParser = new LengthParser();
        UnitResolver ur = new UnitResolver();
        lengthParser.setLengthHandler(ur);
        lengthParser.parse(s);
        return svgToUserSpace(ur.value, ur.unit, d, ctx);
    }
    public static float svgToUserSpace(float v,
                                       short type,
                                       short d,
                                       Context ctx) {
        switch (type) {
        case SVGLength.SVG_LENGTHTYPE_NUMBER:
        case SVGLength.SVG_LENGTHTYPE_PX:
            return v;
        case SVGLength.SVG_LENGTHTYPE_MM:
            return (v / ctx.getPixelUnitToMillimeter());
        case SVGLength.SVG_LENGTHTYPE_CM:
            return (v * 10f / ctx.getPixelUnitToMillimeter());
        case SVGLength.SVG_LENGTHTYPE_IN:
            return (v * 25.4f / ctx.getPixelUnitToMillimeter());
        case SVGLength.SVG_LENGTHTYPE_PT:
            return (v * 25.4f / (72f * ctx.getPixelUnitToMillimeter()));
        case SVGLength.SVG_LENGTHTYPE_PC:
            return (v * 25.4f / (6f * ctx.getPixelUnitToMillimeter()));
        case SVGLength.SVG_LENGTHTYPE_EMS:
            return emsToPixels(v, d, ctx);
        case SVGLength.SVG_LENGTHTYPE_EXS:
            return exsToPixels(v, d, ctx);
        case SVGLength.SVG_LENGTHTYPE_PERCENTAGE:
            return percentagesToPixels(v, d, ctx);
        default:
            throw new IllegalArgumentException("Length has unknown type");
        }
    }
    public static float userSpaceToSVG(float v,
                                       short type,
                                       short d,
                                       Context ctx) {
        switch (type) {
        case SVGLength.SVG_LENGTHTYPE_NUMBER:
        case SVGLength.SVG_LENGTHTYPE_PX:
            return v;
        case SVGLength.SVG_LENGTHTYPE_MM:
            return (v * ctx.getPixelUnitToMillimeter());
        case SVGLength.SVG_LENGTHTYPE_CM:
            return (v * ctx.getPixelUnitToMillimeter() / 10f);
        case SVGLength.SVG_LENGTHTYPE_IN:
            return (v * ctx.getPixelUnitToMillimeter() / 25.4f);
        case SVGLength.SVG_LENGTHTYPE_PT:
            return (v * (72f * ctx.getPixelUnitToMillimeter()) / 25.4f);
        case SVGLength.SVG_LENGTHTYPE_PC:
            return (v * (6f * ctx.getPixelUnitToMillimeter()) / 25.4f);
        case SVGLength.SVG_LENGTHTYPE_EMS:
            return pixelsToEms(v, d, ctx);
        case SVGLength.SVG_LENGTHTYPE_EXS:
            return pixelsToExs(v, d, ctx);
        case SVGLength.SVG_LENGTHTYPE_PERCENTAGE:
            return pixelsToPercentages(v, d, ctx);
        default:
            throw new IllegalArgumentException("Length has unknown type");
        }
    }
    protected static float percentagesToPixels(float v, short d, Context ctx) {
        if (d == HORIZONTAL_LENGTH) {
            float w = ctx.getViewportWidth();
            return w * v / 100f;
        } else if (d == VERTICAL_LENGTH) {
            float h = ctx.getViewportHeight();
            return h * v / 100f;
        } else {
            double w = ctx.getViewportWidth();
            double h = ctx.getViewportHeight();
            double vpp = Math.sqrt(w * w + h * h) / SQRT2;
            return (float)(vpp * v / 100d);
        }
    }
    protected static float pixelsToPercentages(float v, short d, Context ctx) {
        if (d == HORIZONTAL_LENGTH) {
            float w = ctx.getViewportWidth();
            return v * 100f / w;
        } else if (d == VERTICAL_LENGTH) {
            float h = ctx.getViewportHeight();
            return v * 100f / h;
        } else {
            double w = ctx.getViewportWidth();
            double h = ctx.getViewportHeight();
            double vpp = Math.sqrt(w * w + h * h) / SQRT2;
            return (float)(v * 100d / vpp);
        }
    }
    protected static float pixelsToEms(float v, short d, Context ctx) {
        return v / ctx.getFontSize();
    }
    protected static float emsToPixels(float v, short d, Context ctx) {
        return v * ctx.getFontSize();
    }
    protected static float pixelsToExs(float v, short d, Context ctx) {
        float xh = ctx.getXHeight();
        return v / xh / ctx.getFontSize();
    }
    protected static float exsToPixels(float v, short d, Context ctx) {
        float xh = ctx.getXHeight();
        return v * xh * ctx.getFontSize();
    }
    public static class UnitResolver implements LengthHandler {
        public float value;
        public short unit = SVGLength.SVG_LENGTHTYPE_NUMBER;
        public void startLength() throws ParseException {
        }
        public void lengthValue(float v) throws ParseException {
            this.value = v;
        }
        public void em() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_EMS;
        }
        public void ex() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_EXS;
        }
        public void in() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_IN;
        }
        public void cm() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_CM;
        }
        public void mm() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_MM;
        }
        public void pc() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_PC;
        }
        public void pt() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_PT;
        }
        public void px() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_PX;
        }
        public void percentage() throws ParseException {
            this.unit = SVGLength.SVG_LENGTHTYPE_PERCENTAGE;
        }
        public void endLength() throws ParseException {
        }
    }
    public interface Context {
        Element getElement();
        float getPixelUnitToMillimeter();
        float getPixelToMM();
        float getFontSize();
        float getXHeight();
        float getViewportWidth();
        float getViewportHeight();
    }
}
