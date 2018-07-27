package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class ClipRuleManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_EVENODD_VALUE,
                   SVGValueConstants.EVENODD_VALUE);
        values.put(CSSConstants.CSS_NONZERO_VALUE,
                   SVGValueConstants.NONZERO_VALUE);
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
        return CSSConstants.CSS_CLIP_RULE_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.NONZERO_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
