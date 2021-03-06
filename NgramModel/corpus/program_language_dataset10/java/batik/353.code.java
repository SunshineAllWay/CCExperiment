package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class FontStyleManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_ALL_VALUE,
                   ValueConstants.ALL_VALUE);
        values.put(CSSConstants.CSS_ITALIC_VALUE,
                   ValueConstants.ITALIC_VALUE);
        values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
        values.put(CSSConstants.CSS_OBLIQUE_VALUE,
                   ValueConstants.OBLIQUE_VALUE);
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
        return CSSConstants.CSS_FONT_STYLE_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
