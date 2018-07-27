package org.apache.batik.css.engine;
import org.w3c.css.sac.SelectorList;
public class StyleRule implements Rule {
    public static final short TYPE = (short)0;
    protected SelectorList selectorList;
    protected StyleDeclaration styleDeclaration;
    public short getType() {
        return TYPE;
    }
    public void setSelectorList(SelectorList sl) {
        selectorList = sl;
    }
    public SelectorList getSelectorList() {
        return selectorList;
    }
    public void setStyleDeclaration(StyleDeclaration sd) {
        styleDeclaration = sd;
    }
    public StyleDeclaration getStyleDeclaration() {
        return styleDeclaration;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        if (selectorList != null) {
            sb.append(selectorList.item(0));
            for (int i = 1; i < selectorList.getLength(); i++) {
                sb.append(", ");
                sb.append(selectorList.item(i));
            }
        }
        sb.append(" {\n");
        if (styleDeclaration != null) {
            sb.append(styleDeclaration.toString(eng));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
