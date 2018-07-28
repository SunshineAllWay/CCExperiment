package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
public class SVGColorManager extends ColorManager {
    protected String property;
    protected Value defaultValue;
    public SVGColorManager(String prop) {
        this(prop, SVGValueConstants.BLACK_RGB_VALUE);
    }
    public SVGColorManager(String prop, Value v) {
        property = prop;
        defaultValue = v;
    }
    public boolean isInheritedProperty() {
        return false;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return true;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_COLOR;
    }
    public String getPropertyName() {
        return property;
    }
    public Value getDefaultValue() {
        return defaultValue;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
            if (lu.getStringValue().equalsIgnoreCase
                (CSSConstants.CSS_CURRENTCOLOR_VALUE)) {
                return SVGValueConstants.CURRENTCOLOR_VALUE;
            }
        }
        Value v = super.createValue(lu, engine);
        lu = lu.getNextLexicalUnit();
        if (lu == null) {
            return v;
        }
        if (lu.getLexicalUnitType() != LexicalUnit.SAC_FUNCTION ||
            !lu.getFunctionName().equalsIgnoreCase("icc-color")) {
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        }
        lu = lu.getParameters();
        if (lu.getLexicalUnitType() != LexicalUnit.SAC_IDENT) {
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        }
        ListValue result = new ListValue(' ');
        result.append(v);
        ICCColor icc = new ICCColor(lu.getStringValue());
        result.append(icc);
        lu = lu.getNextLexicalUnit();
        while (lu != null) {
            if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
                throw createInvalidLexicalUnitDOMException
                    (lu.getLexicalUnitType());
            }
            lu = lu.getNextLexicalUnit();
            if (lu == null) {
                throw createInvalidLexicalUnitDOMException((short)-1);
            }
            icc.append(getColorValue(lu));
            lu = lu.getNextLexicalUnit();
        }
        return result;
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value == SVGValueConstants.CURRENTCOLOR_VALUE) {
            sm.putColorRelative(idx, true);
            int ci = engine.getColorIndex();
            return engine.getComputedStyle(elt, pseudo, ci);
        }
        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            ListValue lv = (ListValue)value;
            Value v = lv.item(0);
            Value t = super.computeValue(elt, pseudo, engine, idx, sm, v);
            if (t != v) {
                ListValue result = new ListValue(' ');
                result.append(t);
                result.append(lv.item(1));
                return result;
            }
            return value;
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }
    protected float getColorValue(LexicalUnit lu) {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INTEGER:
            return lu.getIntegerValue();
        case LexicalUnit.SAC_REAL:
            return lu.getFloatValue();
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
    }
}
