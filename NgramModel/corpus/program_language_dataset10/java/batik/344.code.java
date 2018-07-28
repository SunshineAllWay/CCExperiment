package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.RectManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class ClipManager extends RectManager {
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
        return SVGTypes.TYPE_CLIP_VALUE;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_CLIP_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.AUTO_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return InheritValue.INSTANCE;
        case LexicalUnit.SAC_IDENT:
            if (lu.getStringValue().equalsIgnoreCase
                (CSSConstants.CSS_AUTO_VALUE)) {
                return ValueConstants.AUTO_VALUE;
            }
        }
        return super.createValue(lu, engine);
    }
    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidStringTypeDOMException(type);
        }
        if (!value.equalsIgnoreCase(CSSConstants.CSS_AUTO_VALUE)) {
            throw createInvalidIdentifierDOMException(value);
        }
        return ValueConstants.AUTO_VALUE;
    }
}
