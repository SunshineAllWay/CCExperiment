package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class StrokeMiterlimitManager extends AbstractValueManager {
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return true;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_NUMBER_OR_INHERIT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_STROKE_MITERLIMIT_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.NUMBER_4;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        case  LexicalUnit.SAC_INTEGER:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());
        case  LexicalUnit.SAC_REAL:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());
        default:
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        }
    }
    public Value createFloatValue(short unitType, float floatValue)
        throws DOMException {
        if (unitType == CSSPrimitiveValue.CSS_NUMBER) {
            return new FloatValue(unitType, floatValue);
        }
        throw createInvalidFloatTypeDOMException(unitType);
    }
}
