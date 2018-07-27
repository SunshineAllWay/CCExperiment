package org.apache.batik.css.engine.value.css2;
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
public class FontStretchManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_ALL_VALUE,
                   ValueConstants.ALL_VALUE);
        values.put(CSSConstants.CSS_CONDENSED_VALUE,
                   ValueConstants.CONDENSED_VALUE);
        values.put(CSSConstants.CSS_EXPANDED_VALUE,
                   ValueConstants.EXPANDED_VALUE);
        values.put(CSSConstants.CSS_EXTRA_CONDENSED_VALUE,
                   ValueConstants.EXTRA_CONDENSED_VALUE);
        values.put(CSSConstants.CSS_EXTRA_EXPANDED_VALUE,
                   ValueConstants.EXTRA_EXPANDED_VALUE);
        values.put(CSSConstants.CSS_NARROWER_VALUE,
                   ValueConstants.NARROWER_VALUE);
        values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
        values.put(CSSConstants.CSS_SEMI_CONDENSED_VALUE,
                   ValueConstants.SEMI_CONDENSED_VALUE);
        values.put(CSSConstants.CSS_SEMI_EXPANDED_VALUE,
                   ValueConstants.SEMI_EXPANDED_VALUE);
        values.put(CSSConstants.CSS_ULTRA_CONDENSED_VALUE,
                   ValueConstants.ULTRA_CONDENSED_VALUE);
        values.put(CSSConstants.CSS_ULTRA_EXPANDED_VALUE,
                   ValueConstants.ULTRA_EXPANDED_VALUE);
        values.put(CSSConstants.CSS_WIDER_VALUE,
                   ValueConstants.WIDER_VALUE);
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
        return SVGTypes.TYPE_IDENT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_FONT_STRETCH_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
    }
    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value == ValueConstants.NARROWER_VALUE) {
            sm.putParentRelative(idx, true);
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            if (p == null) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            Value v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            if (v == ValueConstants.CONDENSED_VALUE) {
                return ValueConstants.EXTRA_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXPANDED_VALUE) {
                return ValueConstants.SEMI_EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
                return ValueConstants.NORMAL_VALUE;
            }
            if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
                return ValueConstants.CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
                return ValueConstants.ULTRA_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
                return ValueConstants.EXPANDED_VALUE;
            }
            if (v == ValueConstants.ULTRA_CONDENSED_VALUE) {
                return ValueConstants.ULTRA_CONDENSED_VALUE;
            }
            return ValueConstants.EXTRA_EXPANDED_VALUE;
        } else if (value == ValueConstants.WIDER_VALUE) {
            sm.putParentRelative(idx, true);
            CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
            if (p == null) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            Value v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
                return ValueConstants.SEMI_EXPANDED_VALUE;
            }
            if (v == ValueConstants.CONDENSED_VALUE) {
                return ValueConstants.SEMI_CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXPANDED_VALUE) {
                return ValueConstants.EXTRA_EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
                return ValueConstants.EXPANDED_VALUE;
            }
            if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
                return ValueConstants.NORMAL_VALUE;
            }
            if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
                return ValueConstants.CONDENSED_VALUE;
            }
            if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
                return ValueConstants.ULTRA_EXPANDED_VALUE;
            }
            if (v == ValueConstants.ULTRA_CONDENSED_VALUE) {
                return ValueConstants.EXTRA_CONDENSED_VALUE;
            }
            return ValueConstants.ULTRA_EXPANDED_VALUE;
        }
        return value;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
