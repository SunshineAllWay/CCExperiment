package org.apache.batik.css.parser;
import org.w3c.css.sac.LexicalUnit;
public abstract class CSSLexicalUnit implements LexicalUnit {
    public static final String UNIT_TEXT_CENTIMETER  = "cm";
    public static final String UNIT_TEXT_DEGREE      = "deg";
    public static final String UNIT_TEXT_EM          = "em";
    public static final String UNIT_TEXT_EX          = "ex";
    public static final String UNIT_TEXT_GRADIAN     = "grad";
    public static final String UNIT_TEXT_HERTZ       = "Hz";
    public static final String UNIT_TEXT_INCH        = "in";
    public static final String UNIT_TEXT_KILOHERTZ   = "kHz";
    public static final String UNIT_TEXT_MILLIMETER  = "mm";
    public static final String UNIT_TEXT_MILLISECOND = "ms";
    public static final String UNIT_TEXT_PERCENTAGE  = "%";
    public static final String UNIT_TEXT_PICA        = "pc";
    public static final String UNIT_TEXT_PIXEL       = "px";
    public static final String UNIT_TEXT_POINT       = "pt";
    public static final String UNIT_TEXT_RADIAN      = "rad";
    public static final String UNIT_TEXT_REAL        = "";
    public static final String UNIT_TEXT_SECOND      = "s";
    public static final String TEXT_RGBCOLOR          = "rgb";
    public static final String TEXT_RECT_FUNCTION     = "rect";
    public static final String TEXT_COUNTER_FUNCTION  = "counter";
    public static final String TEXT_COUNTERS_FUNCTION = "counters";
    protected short lexicalUnitType;
    protected LexicalUnit nextLexicalUnit;
    protected LexicalUnit previousLexicalUnit;
    protected CSSLexicalUnit(short t, LexicalUnit prev) {
        lexicalUnitType = t;
        previousLexicalUnit = prev;
        if (prev != null) {
            ((CSSLexicalUnit)prev).nextLexicalUnit = this;
        }
    }
    public short getLexicalUnitType() {
        return lexicalUnitType;
    }
    public LexicalUnit getNextLexicalUnit() {
        return nextLexicalUnit;
    }
    public void setNextLexicalUnit(LexicalUnit lu) {
        nextLexicalUnit = lu;
    }
    public LexicalUnit getPreviousLexicalUnit() {
        return previousLexicalUnit;
    }
    public void setPreviousLexicalUnit(LexicalUnit lu) {
        previousLexicalUnit = lu;
    }
    public int getIntegerValue() {
        throw new IllegalStateException();
    }
    public float getFloatValue() {
        throw new IllegalStateException();
    }
    public String getDimensionUnitText() {
        switch (lexicalUnitType) {
        case LexicalUnit.SAC_CENTIMETER:  return UNIT_TEXT_CENTIMETER;
        case LexicalUnit.SAC_DEGREE:      return UNIT_TEXT_DEGREE;
        case LexicalUnit.SAC_EM:          return UNIT_TEXT_EM;
        case LexicalUnit.SAC_EX:          return UNIT_TEXT_EX;
        case LexicalUnit.SAC_GRADIAN:     return UNIT_TEXT_GRADIAN;
        case LexicalUnit.SAC_HERTZ:       return UNIT_TEXT_HERTZ;
        case LexicalUnit.SAC_INCH:        return UNIT_TEXT_INCH;
        case LexicalUnit.SAC_KILOHERTZ:   return UNIT_TEXT_KILOHERTZ;
        case LexicalUnit.SAC_MILLIMETER:  return UNIT_TEXT_MILLIMETER;
        case LexicalUnit.SAC_MILLISECOND: return UNIT_TEXT_MILLISECOND;
        case LexicalUnit.SAC_PERCENTAGE:  return UNIT_TEXT_PERCENTAGE;
        case LexicalUnit.SAC_PICA:        return UNIT_TEXT_PICA;
        case LexicalUnit.SAC_PIXEL:       return UNIT_TEXT_PIXEL;
        case LexicalUnit.SAC_POINT:       return UNIT_TEXT_POINT;
        case LexicalUnit.SAC_RADIAN:      return UNIT_TEXT_RADIAN;
        case LexicalUnit.SAC_REAL:        return UNIT_TEXT_REAL;
        case LexicalUnit.SAC_SECOND:      return UNIT_TEXT_SECOND;
        default:
            throw new IllegalStateException("No Unit Text for type: " + 
                                            lexicalUnitType);
        }
    }
    public String getFunctionName() {
        throw new IllegalStateException();
    }
    public LexicalUnit getParameters() {
        throw new IllegalStateException();
    }
    public String getStringValue() {
        throw new IllegalStateException();
    }
    public LexicalUnit getSubValues() {
        throw new IllegalStateException();
    }
    public static CSSLexicalUnit createSimple(short t, LexicalUnit prev) {
        return new SimpleLexicalUnit(t, prev);
    }
    protected static class SimpleLexicalUnit extends CSSLexicalUnit {
        public SimpleLexicalUnit(short t, LexicalUnit prev) {
            super(t, prev);
        }
    }
    public static CSSLexicalUnit createInteger(int val, LexicalUnit prev) {
        return new IntegerLexicalUnit(val, prev);
    }
    protected static class IntegerLexicalUnit extends CSSLexicalUnit {
        protected int value;
        public IntegerLexicalUnit(int val, LexicalUnit prev) {
            super(LexicalUnit.SAC_INTEGER, prev);
            value = val;
        }
        public int getIntegerValue() {
            return value;
        }
    }
    public static CSSLexicalUnit createFloat(short t, float val, LexicalUnit prev) {
        return new FloatLexicalUnit(t, val, prev);
    }
    protected static class FloatLexicalUnit extends CSSLexicalUnit {
        protected float value;
        public FloatLexicalUnit(short t, float val, LexicalUnit prev) {
            super(t, prev);
            value = val;
        }
        public float getFloatValue() {
            return value;
        }
    }
    public static CSSLexicalUnit createDimension(float val, String dim,
                                                 LexicalUnit prev) {
        return new DimensionLexicalUnit(val, dim, prev);
    }
    protected static class DimensionLexicalUnit extends CSSLexicalUnit {
        protected float value;
        protected String dimension;
        public DimensionLexicalUnit(float val, String dim, LexicalUnit prev) {
            super(SAC_DIMENSION, prev);
            value = val;
            dimension = dim;
        }
        public float getFloatValue() {
            return value;
        }
        public String getDimensionUnitText() {
            return dimension;
        }
    }
    public static CSSLexicalUnit createFunction(String f, LexicalUnit params,
                                                LexicalUnit prev) {
        return new FunctionLexicalUnit(f, params, prev);
    }
    protected static class FunctionLexicalUnit extends CSSLexicalUnit {
        protected String name;
        protected LexicalUnit parameters;
        public FunctionLexicalUnit(String f, LexicalUnit params, LexicalUnit prev) {
            super(SAC_FUNCTION, prev);
            name = f;
            parameters = params;
        }
        public String getFunctionName() {
            return name;
        }
        public LexicalUnit getParameters() {
            return parameters;
        }
    }
    public static CSSLexicalUnit createPredefinedFunction(short t, LexicalUnit params,
                                                          LexicalUnit prev) {
        return new PredefinedFunctionLexicalUnit(t, params, prev);
    }
    protected static class PredefinedFunctionLexicalUnit extends CSSLexicalUnit {
        protected LexicalUnit parameters;
        public PredefinedFunctionLexicalUnit(short t, LexicalUnit params,
                                             LexicalUnit prev) {
            super(t, prev);
            parameters = params;
        }
        public String getFunctionName() {
            switch (lexicalUnitType) {
            case SAC_RGBCOLOR:          return TEXT_RGBCOLOR;
            case SAC_RECT_FUNCTION:     return TEXT_RECT_FUNCTION;
            case SAC_COUNTER_FUNCTION:  return TEXT_COUNTER_FUNCTION;
            case SAC_COUNTERS_FUNCTION: return TEXT_COUNTERS_FUNCTION;
            default: break;
            }
            return super.getFunctionName();
        }
        public LexicalUnit getParameters() {
            return parameters;
        }
    }
    public static CSSLexicalUnit createString(short t, String val, LexicalUnit prev) {
        return new StringLexicalUnit(t, val, prev);
    }
    protected static class StringLexicalUnit extends CSSLexicalUnit {
        protected String value;
        public StringLexicalUnit(short t, String val, LexicalUnit prev) {
            super(t, prev);
            value = val;
        }
        public String getStringValue() {
            return value;
        }
    }
}
