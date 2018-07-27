package org.apache.batik.dom.svg;
import org.apache.batik.parser.AngleParser;
import org.apache.batik.parser.DefaultAngleHandler;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAngle;
public class SVGOMAngle implements SVGAngle {
    protected short unitType;
    protected float value;
    protected static final String[] UNITS = {
        "", "", "deg", "rad", "grad"
    };
    public short getUnitType() {
        revalidate();
        return unitType;
    }
    public float getValue() {
        revalidate();
        return toUnit(unitType, value, SVG_ANGLETYPE_DEG);
    }
    public void setValue(float value) throws DOMException {
        revalidate();
        this.unitType = SVG_ANGLETYPE_DEG;
        this.value = value;
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
        value = toUnit(unitType, value, unit);
        unitType = unit;
    }
    protected void reset() {
    }
    protected void revalidate() {
    }
    protected void parse(String s) {
        try {
            AngleParser angleParser = new AngleParser();
            angleParser.setAngleHandler(new DefaultAngleHandler() {
                public void angleValue(float v) throws ParseException {
                    value = v;
                }
                public void deg() throws ParseException {
                    unitType = SVG_ANGLETYPE_DEG;
                }
                public void rad() throws ParseException {
                    unitType = SVG_ANGLETYPE_RAD;
                }
                public void grad() throws ParseException {
                    unitType = SVG_ANGLETYPE_GRAD;
                }
            });
            unitType = SVG_ANGLETYPE_UNSPECIFIED;
            angleParser.parse(s);
        } catch (ParseException e) {
            unitType = SVG_ANGLETYPE_UNKNOWN;
            value = 0;
        }
    }
    protected static double[][] K = {
        {             1,      Math.PI / 180,        Math.PI / 200 },
        { 180 / Math.PI,                  1, 1800 / (9 * Math.PI) },
        {           0.9, 9 * Math.PI / 1800,                    1 }
    };
    public static float toUnit(short fromUnit, float value, short toUnit) {
        if (fromUnit == 1) {
            fromUnit = 2;
        }
        if (toUnit == 1) {
            toUnit = 2;
        }
        return (float) (K[fromUnit - 2][toUnit - 2] * value);
    }
}
