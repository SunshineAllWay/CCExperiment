package org.apache.batik.dom.svg;
import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.UnitProcessor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLength;
public abstract class AbstractSVGLength
    implements SVGLength {
    public static final short HORIZONTAL_LENGTH =
        UnitProcessor.HORIZONTAL_LENGTH;
    public static final short VERTICAL_LENGTH =
        UnitProcessor.VERTICAL_LENGTH;
    public static final short OTHER_LENGTH =
        UnitProcessor.OTHER_LENGTH;
    protected short unitType;
    protected float value;
    protected short direction;
    protected UnitProcessor.Context context;
    protected static final String[] UNITS = {
        "", "", "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc"
    };
    protected abstract SVGOMElement getAssociatedElement();
    public AbstractSVGLength(short direction) {
        context = new DefaultContext();
        this.direction = direction;
        this.value = 0.0f;
        this.unitType = SVGLength.SVG_LENGTHTYPE_NUMBER;
    }
    public short getUnitType() {
        revalidate();
        return unitType;
    }
    public float getValue() {
        revalidate();
        try {
            return UnitProcessor.svgToUserSpace(value, unitType,
                                                direction, context);
        } catch (IllegalArgumentException ex) {
            return 0f;
        }
    }
    public void setValue(float value) throws DOMException {
        this.value = UnitProcessor.userSpaceToSVG(value, unitType,
                                                  direction, context);
        reset();
    }
    public float getValueInSpecifiedUnits() {
        revalidate();
        return value;
    }
    public void setValueInSpecifiedUnits(float value) throws DOMException {
        revalidate();
        this.value = value;
        reset();
    }
    public String getValueAsString() {
        revalidate();
        if (unitType == SVGLength.SVG_LENGTHTYPE_UNKNOWN) {
            return "";
        }
        return Float.toString(value) + UNITS[unitType];
    }
    public void setValueAsString(String value) throws DOMException {
        parse(value);
        reset();
    }
    public void newValueSpecifiedUnits(short unit, float value) {
        unitType = unit;
        this.value = value;
        reset();
    }
    public void convertToSpecifiedUnits(short unit) {
        float v = getValue();
        unitType = unit;
        setValue(v);
    }
    protected void reset() {
    }
    protected void revalidate() {
    }
    protected void parse(String s) {
        try {
            LengthParser lengthParser = new LengthParser();
            UnitProcessor.UnitResolver ur =
                new UnitProcessor.UnitResolver();
            lengthParser.setLengthHandler(ur);
            lengthParser.parse(s);
            unitType = ur.unit;
            value = ur.value;
        } catch (ParseException e) {
            unitType = SVG_LENGTHTYPE_UNKNOWN;
            value = 0;
        }
    }
    protected class DefaultContext implements UnitProcessor.Context {
        public Element getElement() {
            return getAssociatedElement();
        }
        public float getPixelUnitToMillimeter() {
            return getAssociatedElement().getSVGContext()
                .getPixelUnitToMillimeter();
        }
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
        }
        public float getFontSize() {
            return getAssociatedElement().getSVGContext().getFontSize();
        }
        public float getXHeight() {
            return 0.5f;
        }
        public float getViewportWidth() {
            return getAssociatedElement().getSVGContext().getViewportWidth();
        }
        public float getViewportHeight() {
            return getAssociatedElement().getSVGContext().getViewportHeight();
        }
    }
}
