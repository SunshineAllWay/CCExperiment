package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
public class EnableBackgroundManager extends LengthManager {
    protected int orientation;
    public boolean isInheritedProperty() {
        return false;
    }
    public boolean isAnimatableProperty() {
        return false;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_ENABLE_BACKGROUND_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_ENABLE_BACKGROUND_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.ACCUMULATE_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        default:
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        case LexicalUnit.SAC_IDENT:
            String id = lu.getStringValue().toLowerCase().intern();
            if (id == CSSConstants.CSS_ACCUMULATE_VALUE) {
                return SVGValueConstants.ACCUMULATE_VALUE;
            }
            if (id != CSSConstants.CSS_NEW_VALUE) {
                throw createInvalidIdentifierDOMException(id);
            }
            ListValue result = new ListValue(' ');
            result.append(SVGValueConstants.NEW_VALUE);
            lu = lu.getNextLexicalUnit();
            if (lu == null) {
                return result;
            }
            result.append(super.createValue(lu, engine));
            for (int i = 1; i < 4; i++) {
                lu = lu.getNextLexicalUnit();
                if (lu == null){
                    throw createMalformedLexicalUnitDOMException();
                }
                result.append(super.createValue(lu, engine));
            }
            return result;
        }
    }
    public Value createStringValue(short type, String value,
                                   CSSEngine engine) {
        if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidStringTypeDOMException(type);
        }
        if (!value.equalsIgnoreCase(CSSConstants.CSS_ACCUMULATE_VALUE)) {
            throw createInvalidIdentifierDOMException(value);
        }
        return SVGValueConstants.ACCUMULATE_VALUE;
    }
    public Value createFloatValue(short unitType, float floatValue)
        throws DOMException {
        throw createDOMException();
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            ListValue lv = (ListValue)value;
            if (lv.getLength() == 5) {
                Value lv1 = lv.item(1);
                orientation = HORIZONTAL_ORIENTATION;
                Value v1 = super.computeValue(elt, pseudo, engine,
                                              idx, sm, lv1);
                Value lv2 = lv.item(2);
                orientation = VERTICAL_ORIENTATION;
                Value v2 = super.computeValue(elt, pseudo, engine,
                                              idx, sm, lv2);
                Value lv3 = lv.item(3);
                orientation = HORIZONTAL_ORIENTATION;
                Value v3 = super.computeValue(elt, pseudo, engine,
                                              idx, sm, lv3);
                Value lv4 = lv.item(4);
                orientation = VERTICAL_ORIENTATION;
                Value v4 = super.computeValue(elt, pseudo, engine,
                                              idx, sm, lv4);
                if (lv1 != v1 || lv2 != v2 ||
                    lv3 != v3 || lv4 != v4) {
                    ListValue result = new ListValue(' ');
                    result.append(lv.item(0));
                    result.append(v1);
                    result.append(v2);
                    result.append(v3);
                    result.append(v4);
                    return result;
                }
            }
        }
        return value;
    }
    protected int getOrientation() {
        return orientation;
    }
}
