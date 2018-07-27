package org.apache.batik.css.engine.value.svg12;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGValueConstants;
import org.apache.batik.util.SVG12CSSConstants;
import org.w3c.dom.css.CSSPrimitiveValue;
public interface SVG12ValueConstants extends SVGValueConstants {
    Value START_VALUE  = new StringValue(CSSPrimitiveValue.CSS_IDENT,
                                         SVG12CSSConstants.CSS_FULL_VALUE);
    Value MIDDLE_VALUE = new StringValue(CSSPrimitiveValue.CSS_IDENT,
                                         SVG12CSSConstants.CSS_MIDDLE_VALUE);
    Value END_VALUE    = new StringValue(CSSPrimitiveValue.CSS_IDENT,
                                         SVG12CSSConstants.CSS_END_VALUE);
    Value FULL_VALUE   = new StringValue(CSSPrimitiveValue.CSS_IDENT,
                                         SVG12CSSConstants.CSS_FULL_VALUE);
    Value NORMAL_VALUE = new StringValue(CSSPrimitiveValue.CSS_IDENT,
                                         SVG12CSSConstants.CSS_NORMAL_VALUE);
}
