package org.apache.batik.css.engine;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public interface CSSNavigableDocumentListener {
    void nodeInserted(Node newNode);
    void nodeToBeRemoved(Node oldNode);
    void subtreeModified(Node rootOfModifications);
    void characterDataModified(Node text);
    void attrModified(Element e, Attr attr, short attrChange,
                      String prevValue, String newValue);
    void overrideStyleTextChanged(CSSStylableElement e, String text);
    void overrideStylePropertyRemoved(CSSStylableElement e, String name);
    void overrideStylePropertyChanged(CSSStylableElement e, String name,
                                      String val, String prio);
}
