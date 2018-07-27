package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class StrokeLinecapManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_BUTT_VALUE,
                   SVGValueConstants.BUTT_VALUE);
        values.put(CSSConstants.CSS_ROUND_VALUE,
                   SVGValueConstants.ROUND_VALUE);
        values.put(CSSConstants.CSS_SQUARE_VALUE,
                   SVGValueConstants.SQUARE_VALUE);
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
        return CSSConstants.CSS_STROKE_LINECAP_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.BUTT_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
