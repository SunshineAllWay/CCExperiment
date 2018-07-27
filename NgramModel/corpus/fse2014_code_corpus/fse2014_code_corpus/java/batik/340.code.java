package org.apache.batik.css.engine.value;
import org.w3c.dom.css.CSSPrimitiveValue;
public class URIValue extends StringValue {
    String cssText;
    public URIValue(String cssText, String uri) {
        super(CSSPrimitiveValue.CSS_URI, uri);
        this.cssText = cssText;
    }
    public String getCssText() {
        return "url(" + cssText + ')';
    }
}
