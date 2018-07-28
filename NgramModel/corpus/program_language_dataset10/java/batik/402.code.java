package org.apache.batik.css.engine.value.svg12;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.engine.value.svg.SVGValueConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
public class MarginLengthManager extends LengthManager {
    protected String  prop;
    public MarginLengthManager(String prop) {
        this.prop = prop;
    }
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return true;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_LENGTH_OR_INHERIT;
    }
    public String getPropertyName() {
        return prop;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.NUMBER_0;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
            return SVGValueConstants.INHERIT_VALUE;
        }
        return super.createValue(lu, engine);
    }
    protected int getOrientation() {
        return HORIZONTAL_ORIENTATION;
    }
}
