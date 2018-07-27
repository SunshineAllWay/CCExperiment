package org.apache.batik.css.engine;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
public interface CSSStylableElement extends Element {
    StyleMap getComputedStyleMap(String pseudoElement);
    void setComputedStyleMap(String pseudoElement, StyleMap sm);
    String getXMLId();
    String getCSSClass();
    ParsedURL getCSSBase();
    boolean isPseudoInstanceOf(String pseudoClass);
    StyleDeclarationProvider getOverrideStyleDeclarationProvider();
}
