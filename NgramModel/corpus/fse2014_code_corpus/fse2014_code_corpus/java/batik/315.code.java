package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;
public class CSSIdCondition extends AbstractAttributeCondition {
    protected String namespaceURI;
    protected String localName;
    public CSSIdCondition(String ns, String ln, String value) {
        super(value);
        namespaceURI = ns;
        localName = ln;
    }
    public short getConditionType() {
        return SAC_ID_CONDITION;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return localName;
    }
    public boolean getSpecified() {
        return true;
    }
    public boolean match(Element e, String pseudoE) {
        return (e instanceof CSSStylableElement)
            ? ((CSSStylableElement)e).getXMLId().equals(getValue())
            : false;
    }
    public void fillAttributeSet(Set attrSet) {
        attrSet.add(localName);
    }
    public int getSpecificity() {
        return 1 << 16;
    }
    public String toString() {
        return '#' + getValue();
    }
}
