package org.apache.batik.extension;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGStylable;
public abstract class StylableExtensionElement
    extends ExtensionElement
    implements CSSStylableElement,
               SVGStylable {
    protected ParsedURL cssBase;
    protected StyleMap computedStyleMap;
    protected StylableExtensionElement() {
    }
    protected StylableExtensionElement(String name, AbstractDocument owner) {
        super(name, owner);
    }
    public StyleMap getComputedStyleMap(String pseudoElement) {
        return computedStyleMap;
    }
    public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
        computedStyleMap = sm;
    }
    public String getXMLId() {
        return getAttributeNS(null, "id");
    }
    public String getCSSClass() {
        return getAttributeNS(null, "class");
    }
    public ParsedURL getCSSBase() {
        if (cssBase == null) {
            String bu = getBaseURI();
            if (bu == null) {
                return null;
            }
            cssBase = new ParsedURL(bu);
        }
        return cssBase;
    }
    public boolean isPseudoInstanceOf(String pseudoClass) {
        if (pseudoClass.equals("first-child")) {
            Node n = getPreviousSibling();
            while (n != null && n.getNodeType() != ELEMENT_NODE) {
                n = n.getPreviousSibling();
            }
            return n == null;
        }
        return false;
    }
    public StyleDeclarationProvider getOverrideStyleDeclarationProvider() {
        return null;
    }
    public CSSStyleDeclaration getStyle() {
        throw new UnsupportedOperationException("Not implemented");
    }
    public CSSValue getPresentationAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }
    public SVGAnimatedString getClassName() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
