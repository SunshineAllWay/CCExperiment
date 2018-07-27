package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class BaselineShiftManager extends LengthManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_BASELINE_VALUE,
                   SVGValueConstants.BASELINE_VALUE);
        values.put(CSSConstants.CSS_SUB_VALUE,
                   SVGValueConstants.SUB_VALUE);
        values.put(CSSConstants.CSS_SUPER_VALUE,
                   SVGValueConstants.SUPER_VALUE);
    }
    public boolean isInheritedProperty() {
        return false;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_BASELINE_SHIFT_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_BASELINE_SHIFT_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.BASELINE_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        case LexicalUnit.SAC_IDENT:
            Object v = values.get(lu.getStringValue().toLowerCase().intern());
            if (v == null) {
                throw createInvalidIdentifierDOMException(lu.getStringValue());
            }
            return (Value)v;
        }
        return super.createValue(lu, engine);
    }
    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidIdentifierDOMException(value);
        }
        Object v = values.get(value.toLowerCase().intern());
        if (v == null) {
            throw createInvalidIdentifierDOMException(value);
        }
        return (Value)v;
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            sm.putLineHeightRelative(idx, true);
            int fsi = engine.getLineHeightIndex();
            CSSStylableElement parent;
            parent = (CSSStylableElement)elt.getParentNode();
            if (parent == null) {
                parent = elt;
            }
            Value fs = engine.getComputedStyle(parent, pseudo, fsi);
            float fsv = fs.getFloatValue();
            float v = value.getFloatValue();
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  (fsv * v) / 100f);
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }
    protected int getOrientation() {
        return BOTH_ORIENTATION; 
    }
}
