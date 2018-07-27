package org.apache.batik.css.dom;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.views.DocumentView;
public class CSSOMViewCSS implements ViewCSS {
    protected CSSEngine cssEngine;
    public CSSOMViewCSS(CSSEngine engine) {
        cssEngine = engine;
    }
    public DocumentView getDocument() {
        return (DocumentView)cssEngine.getDocument();
    }
    public CSSStyleDeclaration getComputedStyle(Element elt,
                                                String pseudoElt) {
        if (elt instanceof CSSStylableElement) {
            return new CSSOMComputedStyle(cssEngine,
                                          (CSSStylableElement)elt,
                                          pseudoElt);
        }
        return null;
    }
}
