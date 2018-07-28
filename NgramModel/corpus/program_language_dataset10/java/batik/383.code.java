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
public class OpacityManager extends AbstractValueManager {
    protected boolean inherited;
    protected String property;
    public OpacityManager(String prop, boolean inherit) {
        property = prop;
        inherited = inherit;
    }
    public boolean isInheritedProperty() {
        return inherited;
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
        return property;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.NUMBER_1;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        case LexicalUnit.SAC_INTEGER:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());
        case LexicalUnit.SAC_REAL:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
    }
    public Value createFloatValue(short type, float floatValue)
        throws DOMException {
        if (type == CSSPrimitiveValue.CSS_NUMBER) {
            return new FloatValue(type, floatValue);
        }
        throw createInvalidFloatTypeDOMException(type);
    }
}
