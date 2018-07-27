package org.apache.batik.bridge.svg12;
import java.util.HashMap;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public abstract class AbstractContentSelector {
    protected ContentManager contentManager;
    protected XBLOMContentElement contentElement;
    protected Element boundElement;
    public AbstractContentSelector(ContentManager cm,
                                   XBLOMContentElement content,
                                   Element bound) {
        contentManager = cm;
        contentElement = content;
        boundElement = bound;
    }
    public abstract NodeList getSelectedContent();
    abstract boolean update();
    protected boolean isSelected(Node n) {
        return contentManager.getContentElement(n) != null;
    }
    protected static HashMap selectorFactories = new HashMap();
    static {
        ContentSelectorFactory f1 = new XPathPatternContentSelectorFactory();
        ContentSelectorFactory f2 = new XPathSubsetContentSelectorFactory();
        selectorFactories.put(null, f1);
        selectorFactories.put("XPathPattern", f1);
        selectorFactories.put("XPathSubset", f2);
    }
    public static AbstractContentSelector createSelector
            (String selectorLanguage,
             ContentManager cm,
             XBLOMContentElement content,
             Element bound,
             String selector) {
        ContentSelectorFactory f =
            (ContentSelectorFactory) selectorFactories.get(selectorLanguage);
        if (f == null) {
            throw new RuntimeException
                ("Invalid XBL content selector language '"
                 + selectorLanguage
                 + "'");
        }
        return f.createSelector(cm, content, bound, selector);
    }
    protected static interface ContentSelectorFactory {
        AbstractContentSelector createSelector(ContentManager cm,
                                               XBLOMContentElement content,
                                               Element bound,
                                               String selector);
    }
    protected static class XPathSubsetContentSelectorFactory
            implements ContentSelectorFactory {
        public AbstractContentSelector createSelector(ContentManager cm,
                                                      XBLOMContentElement content,
                                                      Element bound,
                                                      String selector) {
            return new XPathSubsetContentSelector(cm, content, bound, selector);
        }
    }
    protected static class XPathPatternContentSelectorFactory
            implements ContentSelectorFactory {
        public AbstractContentSelector createSelector(ContentManager cm,
                                                      XBLOMContentElement content,
                                                      Element bound,
                                                      String selector) {
            return new XPathPatternContentSelector(cm, content, bound, selector);
        }
    }
}
