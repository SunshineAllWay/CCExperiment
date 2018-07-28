package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class AlignmentBaselineManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_AFTER_EDGE_VALUE,
                   SVGValueConstants.AFTER_EDGE_VALUE);
        values.put(CSSConstants.CSS_ALPHABETIC_VALUE,
                   SVGValueConstants.ALPHABETIC_VALUE);
        values.put(CSSConstants.CSS_AUTO_VALUE,
                   SVGValueConstants.AUTO_VALUE);
        values.put(CSSConstants.CSS_BASELINE_VALUE,
                   SVGValueConstants.BASELINE_VALUE);
        values.put(CSSConstants.CSS_BEFORE_EDGE_VALUE,
                   SVGValueConstants.BEFORE_EDGE_VALUE);
        values.put(CSSConstants.CSS_HANGING_VALUE,
                   SVGValueConstants.HANGING_VALUE);
        values.put(CSSConstants.CSS_IDEOGRAPHIC_VALUE,
                   SVGValueConstants.IDEOGRAPHIC_VALUE);
        values.put(CSSConstants.CSS_MATHEMATICAL_VALUE,
                   SVGValueConstants.MATHEMATICAL_VALUE);
        values.put(CSSConstants.CSS_MIDDLE_VALUE,
                   SVGValueConstants.MIDDLE_VALUE);
        values.put(CSSConstants.CSS_TEXT_AFTER_EDGE_VALUE,
                   SVGValueConstants.TEXT_AFTER_EDGE_VALUE);
        values.put(CSSConstants.CSS_TEXT_BEFORE_EDGE_VALUE,
                   SVGValueConstants.TEXT_BEFORE_EDGE_VALUE);
    }
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
        return SVGTypes.TYPE_IDENT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_ALIGNMENT_BASELINE_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.AUTO_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
