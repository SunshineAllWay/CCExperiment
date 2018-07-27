package org.apache.batik.dom;
import org.apache.batik.css.engine.CSSEngine;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.views.DocumentView;
import org.w3c.dom.views.AbstractView;
public abstract class AbstractStylableDocument extends AbstractDocument
    implements DocumentCSS,
               DocumentView {
    protected transient AbstractView defaultView;
    protected transient CSSEngine cssEngine;
    protected AbstractStylableDocument() {
    }
    protected AbstractStylableDocument(DocumentType dt,
                                       DOMImplementation impl) {
        super(dt, impl);
    }
    public void setCSSEngine(CSSEngine ctx) {
        cssEngine = ctx;
    }
    public CSSEngine getCSSEngine() {
        return cssEngine;
    }
    public StyleSheetList getStyleSheets() {
        throw new RuntimeException(" !!! Not implemented");
    }
    public AbstractView getDefaultView() {
        if (defaultView == null) {
            ExtensibleDOMImplementation impl;
            impl = (ExtensibleDOMImplementation)implementation;
            defaultView = impl.createViewCSS(this);
        }
        return defaultView;
    }
    public void clearViewCSS() {
        defaultView = null;
        if (cssEngine != null) {
            cssEngine.dispose();
        }
        cssEngine = null;
    }
    public CSSStyleDeclaration getOverrideStyle(Element elt,
                                                String pseudoElt) {
        throw new RuntimeException(" !!! Not implemented");
    }
}
