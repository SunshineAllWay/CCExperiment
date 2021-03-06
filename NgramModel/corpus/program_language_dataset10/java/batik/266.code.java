package org.apache.batik.css.dom;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
public class CSSOMComputedStyle implements CSSStyleDeclaration {
    protected CSSEngine cssEngine;
    protected CSSStylableElement element;
    protected String pseudoElement;
    protected Map values = new HashMap();
    public CSSOMComputedStyle(CSSEngine e,
                              CSSStylableElement elt,
                              String pseudoElt) {
        cssEngine = e;
        element = elt;
        pseudoElement = pseudoElt;
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cssEngine.getNumberOfProperties(); i++) {
            sb.append(cssEngine.getPropertyName(i));
            sb.append(": ");
            sb.append(cssEngine.getComputedStyle(element, pseudoElement,
                                                 i).getCssText());
            sb.append(";\n");
        }
        return sb.toString();
    }
    public void setCssText(String cssText) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
    }
    public String getPropertyValue(String propertyName) {
        int idx = cssEngine.getPropertyIndex(propertyName);
        if (idx == -1) {
            return "";
        }
        Value v = cssEngine.getComputedStyle(element, pseudoElement, idx);
        return v.getCssText();
    }
    public CSSValue getPropertyCSSValue(String propertyName) {
        CSSValue result = (CSSValue)values.get(propertyName);
        if (result == null) {
            int idx = cssEngine.getPropertyIndex(propertyName);
            if (idx != -1) {
                result = createCSSValue(idx);
                values.put(propertyName, result);
            }
        }
        return result;
    }
    public String removeProperty(String propertyName) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
    }
    public String getPropertyPriority(String propertyName) {
        return "";
    }
    public void setProperty(String propertyName, String value, String prio)
        throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
    }
    public int getLength() {
        return cssEngine.getNumberOfProperties();
    }
    public String item(int index) {
        if (index < 0 || index >= cssEngine.getNumberOfProperties()) {
            return "";
        }
        return cssEngine.getPropertyName(index);
    }
    public CSSRule getParentRule() {
        return null;
    }
    protected CSSValue createCSSValue(int idx) {
        return new ComputedCSSValue(idx);
    }
    public class ComputedCSSValue
        extends CSSOMValue
        implements CSSOMValue.ValueProvider {
        protected int index;
        public ComputedCSSValue(int idx) {
            super(null);
            valueProvider = this;
            index = idx;
        }
        public Value getValue() {
            return cssEngine.getComputedStyle(element, pseudoElement, index);
        }
    }
}
