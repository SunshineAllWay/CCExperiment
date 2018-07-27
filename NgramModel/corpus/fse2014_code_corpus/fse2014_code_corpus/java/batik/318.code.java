package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;
public class CSSPseudoClassCondition extends AbstractAttributeCondition {
    protected String namespaceURI;
    public CSSPseudoClassCondition(String namespaceURI, String value) {
        super(value);
        this.namespaceURI = namespaceURI;
    }
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        CSSPseudoClassCondition c = (CSSPseudoClassCondition)obj;
        return c.namespaceURI.equals(namespaceURI);
    }
    public int hashCode() {
        return namespaceURI.hashCode();
    }
    public short getConditionType() {
        return SAC_PSEUDO_CLASS_CONDITION;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return null;
    }
    public boolean getSpecified() {
        return false;
    }
    public boolean match(Element e, String pseudoE) {
        return (e instanceof CSSStylableElement)
            ? ((CSSStylableElement)e).isPseudoInstanceOf(getValue())
            : false;
    }
    public void fillAttributeSet(Set attrSet) {
    }
    public String toString() {
        return ":" + getValue();
    }
}
