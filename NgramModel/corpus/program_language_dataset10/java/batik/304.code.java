package org.apache.batik.css.engine.sac;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
public abstract class AbstractSiblingSelector
    implements SiblingSelector,
               ExtendedSelector {
    protected short nodeType;
    protected Selector selector;
    protected SimpleSelector simpleSelector;
    protected AbstractSiblingSelector(short type,
                                      Selector sel,
                                      SimpleSelector simple) {
        nodeType = type;
        selector = sel;
        simpleSelector = simple;
    }
    public short getNodeType() {
        return nodeType;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        AbstractSiblingSelector s = (AbstractSiblingSelector)obj;
        return s.simpleSelector.equals(simpleSelector);
    }
    public int getSpecificity() {
        return ((ExtendedSelector)selector).getSpecificity() +
               ((ExtendedSelector)simpleSelector).getSpecificity();
    }
    public Selector getSelector() {
        return selector;
    }
    public SimpleSelector getSiblingSelector() {
        return simpleSelector;
    }
}
