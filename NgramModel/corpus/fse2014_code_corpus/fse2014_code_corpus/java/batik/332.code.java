package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
public class ListValue extends AbstractValue {
    protected int length;
    protected Value[] items = new Value[5];
    protected char separator = ',';
    public ListValue() {
    }
    public ListValue(char s) {
        separator = s;
    }
    public char getSeparatorChar() {
        return separator;
    }
    public short getCssValueType() {
        return CSSValue.CSS_VALUE_LIST;
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer( length * 8 );
        if (length > 0) {
            sb.append(items[0].getCssText());
        }
        for (int i = 1; i < length; i++) {
            sb.append(separator);
            sb.append(items[i].getCssText());
        }
        return sb.toString();
    }
    public int getLength() throws DOMException {
        return length;
    }
    public Value item(int index) throws DOMException {
        return items[index];
    }
    public String toString() {
        return getCssText();
    }
    public void append(Value v) {
        if (length == items.length) {
            Value[] t = new Value[length * 2];
            System.arraycopy( items, 0, t, 0, length );
            items = t;
        }
        items[length++] = v;
    }
}
