package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class DirectionManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_LTR_VALUE, ValueConstants.LTR_VALUE);
        values.put(CSSConstants.CSS_RTL_VALUE, ValueConstants.RTL_VALUE);
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
        return CSSConstants.CSS_DIRECTION_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.LTR_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
