package org.apache.batik.css.dom;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
public class CSSOMStyleDeclaration implements CSSStyleDeclaration {
    protected ValueProvider valueProvider;
    protected ModificationHandler handler;
    protected CSSRule parentRule;
    protected Map values;
    public CSSOMStyleDeclaration(ValueProvider vp, CSSRule parent) {
        valueProvider = vp;
        parentRule = parent;
    }
    public void setModificationHandler(ModificationHandler h) {
        handler = h;
    }
    public String getCssText() {
        return valueProvider.getText();
    }
    public void setCssText(String cssText) throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            values = null;
            handler.textChanged(cssText);
        }
    }
    public String getPropertyValue(String propertyName) {
        Value value = valueProvider.getValue(propertyName);
        if (value == null) {
            return "";
        }
        return value.getCssText();
    }
    public CSSValue getPropertyCSSValue(String propertyName) {
        Value value = valueProvider.getValue(propertyName);
        if (value == null) {
            return null;
        }
        return getCSSValue(propertyName);
    }
    public String removeProperty(String propertyName) throws DOMException {
        String result = getPropertyValue(propertyName);
        if (result.length() > 0) {
            if (handler == null) {
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            } else {
                if (values != null) {
                    values.remove(propertyName);
                }
                handler.propertyRemoved(propertyName);
            }
        }
        return result;
    }
    public String getPropertyPriority(String propertyName) {
        return (valueProvider.isImportant(propertyName)) ? "important" : "";
    }
    public void setProperty(String propertyName, String value, String prio)
        throws DOMException {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            handler.propertyChanged(propertyName, value, prio);
        }
    }
    public int getLength() {
        return valueProvider.getLength();
    }
    public String item(int index) {
        return valueProvider.item(index);
    }
    public CSSRule getParentRule() {
        return parentRule;
    }
    protected CSSValue getCSSValue(String name) {
        CSSValue result = null;
        if (values != null) {
            result = (CSSValue)values.get(name);
        }
        if (result == null) {
            result = createCSSValue(name);
            if (values == null) {
                values = new HashMap(11);
            }
            values.put(name, result);
        }
        return result;
    }
    protected CSSValue createCSSValue(String name) {
        return new StyleDeclarationValue(name);
    }
    public interface ValueProvider {
        Value getValue(String name);
        boolean isImportant(String name);
        String getText();
        int getLength();
        String item(int idx);
    }
    public interface ModificationHandler {
        void textChanged(String text) throws DOMException;
        void propertyRemoved(String name) throws DOMException;
        void propertyChanged(String name, String value, String prio)
            throws DOMException;
    }
    public class StyleDeclarationValue
        extends CSSOMValue
        implements CSSOMValue.ValueProvider {
        protected String property;
        public StyleDeclarationValue(String prop) {
            super(null);
            this.valueProvider = this;
            this.setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return StyleDeclarationValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        if (values == null ||
                            values.get(this) == null ||
                            StyleDeclarationValue.this.handler == null) {
                            throw new DOMException
                                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
                        }
                        String prio = getPropertyPriority(property);
                        CSSOMStyleDeclaration.this.
                            handler.propertyChanged(property, text, prio);
                    }
                });
            property = prop;
        }
        public Value getValue() {
            return CSSOMStyleDeclaration.this.valueProvider.getValue(property);
        }
    }
}
