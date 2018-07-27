package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.DOMException;
import org.w3c.css.sac.LexicalUnit;
public class SrcManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_NONE_VALUE,
                   ValueConstants.NONE_VALUE);
    }
    public SrcManager() {
    }
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
        return SVGTypes.TYPE_FONT_DESCRIPTOR_SRC_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_SRC_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.NONE_VALUE;
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
        case LexicalUnit.SAC_URI:
        }
        ListValue result = new ListValue();
        for (;;) {
            switch (lu.getLexicalUnitType()) {
            case LexicalUnit.SAC_STRING_VALUE:
                result.append(new StringValue(CSSPrimitiveValue.CSS_STRING,
                                              lu.getStringValue()));
                lu = lu.getNextLexicalUnit();
                break;
            case LexicalUnit.SAC_URI:
                String uri = resolveURI(engine.getCSSBaseURI(),
                                        lu.getStringValue());
                result.append(new URIValue(lu.getStringValue(), uri));
                lu = lu.getNextLexicalUnit();
                if ((lu != null) &&
                    (lu.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION)) {
                    if (!lu.getFunctionName().equalsIgnoreCase("format")) {
                        break;
                    }
                    lu = lu.getNextLexicalUnit();
                }
                break;
            case LexicalUnit.SAC_IDENT:
                StringBuffer sb = new StringBuffer(lu.getStringValue());
                lu = lu.getNextLexicalUnit();
                if (lu != null &&
                    lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
                    do {
                        sb.append(' ');
                        sb.append(lu.getStringValue());
                        lu = lu.getNextLexicalUnit();
                    } while (lu != null &&
                             lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT);
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
                break;
            }
            if (lu == null) {
                return result;
            }
            if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
                throw createInvalidLexicalUnitDOMException
                    (lu.getLexicalUnitType());
            }
            lu = lu.getNextLexicalUnit();
            if (lu == null) {
                throw createMalformedLexicalUnitDOMException();
            }
        }
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
