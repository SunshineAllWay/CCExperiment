package org.apache.batik.css.engine.value;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
public abstract class RectManager extends LengthManager {
    protected int orientation;
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_FUNCTION:
            if (!lu.getFunctionName().equalsIgnoreCase("rect")) {
                break;
            }
        case LexicalUnit.SAC_RECT_FUNCTION:
            lu = lu.getParameters();
            Value top = createRectComponent(lu);
            lu = lu.getNextLexicalUnit();
            if (lu == null ||
                lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
                throw createMalformedRectDOMException();
            }
            lu = lu.getNextLexicalUnit();
            Value right = createRectComponent(lu);
            lu = lu.getNextLexicalUnit();
            if (lu == null ||
                lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
                throw createMalformedRectDOMException();
            }
            lu = lu.getNextLexicalUnit();
            Value bottom = createRectComponent(lu);
            lu = lu.getNextLexicalUnit();
            if (lu == null ||
                lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
                throw createMalformedRectDOMException();
            }
            lu = lu.getNextLexicalUnit();
            Value left = createRectComponent(lu);
            return new RectValue(top, right, bottom, left);
        }
        throw createMalformedRectDOMException();
    }
    private Value createRectComponent(LexicalUnit lu) throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_IDENT:
            if (lu.getStringValue().equalsIgnoreCase
                (CSSConstants.CSS_AUTO_VALUE)) {
                return ValueConstants.AUTO_VALUE;
            }
            break;
        case LexicalUnit.SAC_EM:
            return new FloatValue(CSSPrimitiveValue.CSS_EMS,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_EX:
            return new FloatValue(CSSPrimitiveValue.CSS_EXS,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_PIXEL:
            return new FloatValue(CSSPrimitiveValue.CSS_PX,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_CENTIMETER:
            return new FloatValue(CSSPrimitiveValue.CSS_CM,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_MILLIMETER:
            return new FloatValue(CSSPrimitiveValue.CSS_MM,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_INCH:
            return new FloatValue(CSSPrimitiveValue.CSS_IN,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_POINT:
            return new FloatValue(CSSPrimitiveValue.CSS_PT,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_PICA:
            return new FloatValue(CSSPrimitiveValue.CSS_PC,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_INTEGER:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());
        case LexicalUnit.SAC_REAL:
            return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());
        case LexicalUnit.SAC_PERCENTAGE:
            return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE,
                                  lu.getFloatValue());
        }
        throw createMalformedRectDOMException();
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
            return value;
        }
        if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_RECT) {
            return value;
        }
        RectValue rect = (RectValue)value;
        orientation = VERTICAL_ORIENTATION;
        Value top = super.computeValue(elt, pseudo, engine, idx, sm,
                                       rect.getTop());
        Value bottom = super.computeValue(elt, pseudo, engine, idx, sm,
                                          rect.getBottom());
        orientation = HORIZONTAL_ORIENTATION;
        Value left = super.computeValue(elt, pseudo, engine, idx, sm,
                                        rect.getLeft());
        Value right = super.computeValue(elt, pseudo, engine, idx, sm,
                                         rect.getRight());
        if (top != rect.getTop() ||
            right != rect.getRight() ||
            bottom != rect.getBottom() ||
            left != rect.getLeft()) {
            return new RectValue(top, right, bottom, left);
        } else {
            return value;
        }
    }
    protected int getOrientation() {
        return orientation;
    }
    private DOMException createMalformedRectDOMException() {
        Object[] p = new Object[] { getPropertyName() };
        String s = Messages.formatMessage("malformed.rect", p);
        return new DOMException(DOMException.SYNTAX_ERR, s);
    }
}
