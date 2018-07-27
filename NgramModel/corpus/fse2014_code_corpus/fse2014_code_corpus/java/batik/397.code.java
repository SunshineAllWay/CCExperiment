package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class TextAnchorManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_START_VALUE,
                   SVGValueConstants.START_VALUE);
        values.put(CSSConstants.CSS_MIDDLE_VALUE,
                   SVGValueConstants.MIDDLE_VALUE);
        values.put(CSSConstants.CSS_END_VALUE,
                   SVGValueConstants.END_VALUE);
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
        return CSSConstants.CSS_TEXT_ANCHOR_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.START_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
