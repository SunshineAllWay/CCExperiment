package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class WritingModeManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_LR_VALUE,
                   SVGValueConstants.LR_VALUE);
        values.put(CSSConstants.CSS_LR_TB_VALUE,
                   SVGValueConstants.LR_TB_VALUE);
        values.put(CSSConstants.CSS_RL_VALUE,
                   SVGValueConstants.RL_VALUE);
        values.put(CSSConstants.CSS_RL_TB_VALUE,
                   SVGValueConstants.RL_TB_VALUE);
        values.put(CSSConstants.CSS_TB_VALUE,
                   SVGValueConstants.TB_VALUE);
        values.put(CSSConstants.CSS_TB_RL_VALUE,
                   SVGValueConstants.TB_RL_VALUE);
    }
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return false;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_IDENT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_WRITING_MODE_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.LR_TB_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
