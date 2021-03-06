package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
public class MarkerShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {
    public String getPropertyName() {
        return CSSConstants.CSS_MARKER_PROPERTY;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {
        ph.property(CSSConstants.CSS_MARKER_END_PROPERTY, lu, imp);
        ph.property(CSSConstants.CSS_MARKER_MID_PROPERTY, lu, imp);
        ph.property(CSSConstants.CSS_MARKER_START_PROPERTY, lu, imp);
    }
}
