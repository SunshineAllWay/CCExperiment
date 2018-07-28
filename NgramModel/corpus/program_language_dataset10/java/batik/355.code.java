package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class FontWeightManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_ALL_VALUE,
                   ValueConstants.ALL_VALUE);
        values.put(CSSConstants.CSS_BOLD_VALUE,
                   ValueConstants.BOLD_VALUE);
        values.put(CSSConstants.CSS_BOLDER_VALUE,
                   ValueConstants.BOLDER_VALUE);
        values.put(CSSConstants.CSS_LIGHTER_VALUE,
                   ValueConstants.LIGHTER_VALUE);
        values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
    }
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_FONT_WEIGHT_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_FONT_WEIGHT_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
            int i = lu.getIntegerValue();
            switch (i) {
            case 100:
                return ValueConstants.NUMBER_100;
            case 200:
                return ValueConstants.NUMBER_200;
            case 300:
                return ValueConstants.NUMBER_300;
            case 400:
                return ValueConstants.NUMBER_400;
            case 500:
                return ValueConstants.NUMBER_500;
            case 600:
                return ValueConstants.NUMBER_600;
            case 700:
                return ValueConstants.NUMBER_700;
            case 800:
                return ValueConstants.NUMBER_800;
            case 900:
                return ValueConstants.NUMBER_900;
            }
            throw createInvalidFloatValueDOMException(i);
        }
        return super.createValue(lu, engine);
    }
    public Value createFloatValue(short type, float floatValue)
        throws DOMException {
        if (type == CSSPrimitiveValue.CSS_NUMBER) {
            int i = (int)floatValue;
            if (floatValue == i) {
                switch (i) {
                case 100:
                    return ValueConstants.NUMBER_100;
                case 200:
                    return ValueConstants.NUMBER_200;
                case 300:
                    return ValueConstants.NUMBER_300;
                case 400:
                    return ValueConstants.NUMBER_400;
                case 500:
                    return ValueConstants.NUMBER_500;
                case 600:
                    return ValueConstants.NUMBER_600;
                case 700:
                    return ValueConstants.NUMBER_700;
                case 800:
                    return ValueConstants.NUMBER_800;
                case 900:
                    return ValueConstants.NUMBER_900;
                }
            }
        }
        throw createInvalidFloatValueDOMException(floatValue);
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value == ValueConstants.BOLDER_VALUE) {
            sm.putParentRelative(idx, true);
            CSSContext ctx = engine.getCSSContext();
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            float fw;
            if (p == null) {
                fw = 400;
            } else {
                Value v = engine.getComputedStyle(p, pseudo, idx);
                fw = v.getFloatValue();
            }
            return createFontWeight(ctx.getBolderFontWeight(fw));
        } else if (value == ValueConstants.LIGHTER_VALUE) {
            sm.putParentRelative(idx, true);
            CSSContext ctx = engine.getCSSContext();
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            float fw;
            if (p == null) {
                fw = 400;
            } else {
                Value v = engine.getComputedStyle(p, pseudo, idx);
                fw = v.getFloatValue();
            }
            return createFontWeight(ctx.getLighterFontWeight(fw));
        } else if (value == ValueConstants.NORMAL_VALUE) {
            return ValueConstants.NUMBER_400;
        } else if (value == ValueConstants.BOLD_VALUE) {
            return ValueConstants.NUMBER_700;
        }
        return value;
    }
    protected Value createFontWeight(float f) {
        switch ((int)f) {
        case 100:
            return ValueConstants.NUMBER_100;
        case 200:
            return ValueConstants.NUMBER_200;
        case 300:
            return ValueConstants.NUMBER_300;
        case 400:
            return ValueConstants.NUMBER_400;
        case 500:
            return ValueConstants.NUMBER_500;
        case 600:
            return ValueConstants.NUMBER_600;
        case 700:
            return ValueConstants.NUMBER_700;
        case 800:
            return ValueConstants.NUMBER_800;
        default: 
            return ValueConstants.NUMBER_900;
        }
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
