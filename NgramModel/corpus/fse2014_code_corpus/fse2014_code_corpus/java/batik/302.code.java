package org.apache.batik.css.engine.sac;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
public abstract class AbstractDescendantSelector
    implements DescendantSelector,
               ExtendedSelector {
    protected Selector ancestorSelector;
    protected SimpleSelector simpleSelector;
    protected AbstractDescendantSelector(Selector ancestor,
                                         SimpleSelector simple) {
        ancestorSelector = ancestor;
        simpleSelector = simple;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        AbstractDescendantSelector s = (AbstractDescendantSelector)obj;
        return s.simpleSelector.equals(simpleSelector);
    }
    public int getSpecificity() {
        return ((ExtendedSelector)ancestorSelector).getSpecificity() +
               ((ExtendedSelector)simpleSelector).getSpecificity();
    }
    public Selector getAncestorSelector() {
        return ancestorSelector;
    }
    public SimpleSelector getSimpleSelector() {
        return simpleSelector;
    }
}
