package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.CSSConstants;
public class GlyphOrientationHorizontalManager
    extends GlyphOrientationManager {
    public String getPropertyName() {
        return CSSConstants.CSS_GLYPH_ORIENTATION_HORIZONTAL_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.ZERO_DEGREE;
    }
}
