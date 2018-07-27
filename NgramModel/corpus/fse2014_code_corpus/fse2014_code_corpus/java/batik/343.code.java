package org.apache.batik.css.engine.value;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
public interface ValueManager {
    String getPropertyName();
    boolean isInheritedProperty();
    boolean isAnimatableProperty();
    boolean isAdditiveProperty();
    int getPropertyType();
    Value getDefaultValue();
    Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException;
    Value createFloatValue(short unitType, float floatValue)
        throws DOMException;
    Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException;
    Value computeValue(CSSStylableElement elt,
                       String pseudo,
                       CSSEngine engine,
                       int idx,
                       StyleMap sm,
                       Value value);
}
