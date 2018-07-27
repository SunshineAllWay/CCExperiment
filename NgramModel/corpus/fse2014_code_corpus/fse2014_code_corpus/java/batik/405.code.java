package org.apache.batik.css.engine.value.svg12;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.SVG12CSSConstants;
import org.apache.batik.util.SVGTypes;
public class TextAlignManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(SVG12CSSConstants.CSS_START_VALUE,
                   SVG12ValueConstants.START_VALUE);
        values.put(SVG12CSSConstants.CSS_MIDDLE_VALUE,
                   SVG12ValueConstants.MIDDLE_VALUE);
        values.put(SVG12CSSConstants.CSS_END_VALUE,
                   SVG12ValueConstants.END_VALUE);
        values.put(SVG12CSSConstants.CSS_FULL_VALUE,
                   SVG12ValueConstants.FULL_VALUE);
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
        return SVG12CSSConstants.CSS_TEXT_ALIGN_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.INHERIT_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
