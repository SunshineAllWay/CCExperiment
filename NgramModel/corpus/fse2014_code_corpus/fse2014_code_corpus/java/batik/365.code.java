package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.CSSConstants;
public class ColorInterpolationFiltersManager extends ColorInterpolationManager {
    public String getPropertyName() {
        return CSSConstants.CSS_COLOR_INTERPOLATION_FILTERS_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.LINEARRGB_VALUE;
    }
}
