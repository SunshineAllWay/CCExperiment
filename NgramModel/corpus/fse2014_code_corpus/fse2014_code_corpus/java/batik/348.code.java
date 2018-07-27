package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class FontFamilyManager extends AbstractValueManager {
    protected static final ListValue DEFAULT_VALUE = new ListValue();
    static {
        DEFAULT_VALUE.append
            (new StringValue(CSSPrimitiveValue.CSS_STRING,
                             "Arial"));
        DEFAULT_VALUE.append
            (new StringValue(CSSPrimitiveValue.CSS_STRING,
                             "Helvetica"));
        DEFAULT_VALUE.append
            (new StringValue(CSSPrimitiveValue.CSS_IDENT,
                             CSSConstants.CSS_SANS_SERIF_VALUE));
    }
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_CURSIVE_VALUE,
                   ValueConstants.CURSIVE_VALUE);
        values.put(CSSConstants.CSS_FANTASY_VALUE,
                   ValueConstants.FANTASY_VALUE);
        values.put(CSSConstants.CSS_MONOSPACE_VALUE,
                   ValueConstants.MONOSPACE_VALUE);
        values.put(CSSConstants.CSS_SERIF_VALUE,
                   ValueConstants.SERIF_VALUE);
        values.put(CSSConstants.CSS_SANS_SERIF_VALUE,
                   ValueConstants.SANS_SERIF_VALUE);
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
        return SVGTypes.TYPE_FONT_FAMILY_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_FONT_FAMILY_PROPERTY;
    }
    public Value getDefaultValue() {
        return DEFAULT_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return ValueConstants.INHERIT_VALUE;
        default:
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        case LexicalUnit.SAC_IDENT:
        case LexicalUnit.SAC_STRING_VALUE:
        }
        ListValue result = new ListValue();
        for (;;) {
            switch (lu.getLexicalUnitType()) {
            case LexicalUnit.SAC_STRING_VALUE:
                result.append(new StringValue(CSSPrimitiveValue.CSS_STRING,
                                              lu.getStringValue()));
                lu = lu.getNextLexicalUnit();
                break;
            case LexicalUnit.SAC_IDENT:
                StringBuffer sb = new StringBuffer(lu.getStringValue());
                lu = lu.getNextLexicalUnit();
                if (lu != null && isIdentOrNumber(lu)) {
                    do {
                        sb.append(' ');
                        switch (lu.getLexicalUnitType()) {
                        case LexicalUnit.SAC_IDENT:
                            sb.append(lu.getStringValue());
                            break;
                        case LexicalUnit.SAC_INTEGER:
                            sb.append(Integer.toString(lu.getIntegerValue()));
                        }
                        lu = lu.getNextLexicalUnit();
                    } while (lu != null && isIdentOrNumber(lu));
                    result.append(new StringValue(CSSPrimitiveValue.CSS_STRING,
                                                  sb.toString()));
                } else {
                    String id = sb.toString();
                    String s = id.toLowerCase().intern();
                    Value v = (Value)values.get(s);
                    result.append((v != null)
                                  ? v
                                  : new StringValue
                                        (CSSPrimitiveValue.CSS_STRING, id));
                }
            }
            if (lu == null)
                return result;
            if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA)
                throw createInvalidLexicalUnitDOMException
                    (lu.getLexicalUnitType());
            lu = lu.getNextLexicalUnit();
            if (lu == null)
                throw createMalformedLexicalUnitDOMException();
        }
    }
    private boolean isIdentOrNumber(LexicalUnit lu) {
        short type = lu.getLexicalUnitType();
        switch (type) {
        case LexicalUnit.SAC_IDENT:
        case LexicalUnit.SAC_INTEGER:
            return true;
        default:
            return false;
        }
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value == DEFAULT_VALUE) {
            CSSContext ctx = engine.getCSSContext();
            value = ctx.getDefaultFontFamily();
        }
        return value;
    }
}
