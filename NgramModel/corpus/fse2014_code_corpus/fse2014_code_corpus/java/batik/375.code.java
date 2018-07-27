package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public abstract class GlyphOrientationManager extends AbstractValueManager {
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return false;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_ANGLE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        case LexicalUnit.SAC_DEGREE:
            return new FloatValue(CSSPrimitiveValue.CSS_DEG,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_GRADIAN:
            return new FloatValue(CSSPrimitiveValue.CSS_GRAD,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_RADIAN:
            return new FloatValue(CSSPrimitiveValue.CSS_RAD,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_INTEGER:
            { 
                int n = lu.getIntegerValue();
                return new FloatValue(CSSPrimitiveValue.CSS_DEG, n);
            }
        case LexicalUnit.SAC_REAL:
            { 
                float n = lu.getFloatValue();
                return new FloatValue(CSSPrimitiveValue.CSS_DEG, n);
            }
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
    }
    public Value createFloatValue(short type, float floatValue)
        throws DOMException {
        switch (type) {
        case CSSPrimitiveValue.CSS_DEG:
        case CSSPrimitiveValue.CSS_GRAD:
        case CSSPrimitiveValue.CSS_RAD:
            return new FloatValue(type, floatValue);
        }
        throw createInvalidFloatValueDOMException(floatValue);
    }
}
