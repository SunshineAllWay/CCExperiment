package org.apache.tools.ant.types.selectors;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;
public abstract class AbstractSelectorContainer extends DataType
    implements Cloneable, SelectorContainer {
    private Vector selectorsList = new Vector();
    public boolean hasSelectors() {
        if (isReference()) {
            return ((AbstractSelectorContainer) getCheckedRef()).hasSelectors();
        }
        dieOnCircularReference();
        return !(selectorsList.isEmpty());
    }
    public int selectorCount() {
        if (isReference()) {
            return ((AbstractSelectorContainer) getCheckedRef()).selectorCount();
        }
        dieOnCircularReference();
        return selectorsList.size();
    }
    public FileSelector[] getSelectors(Project p) {
        if (isReference()) {
            return ((AbstractSelectorContainer) getCheckedRef(p))
                .getSelectors(p);
        }
        dieOnCircularReference(p);
        FileSelector[] result = new FileSelector[selectorsList.size()];
        selectorsList.copyInto(result);
        return result;
    }
    public Enumeration selectorElements() {
        if (isReference()) {
            return ((AbstractSelectorContainer) getCheckedRef())
                .selectorElements();
        }
        dieOnCircularReference();
        return selectorsList.elements();
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Enumeration e = selectorElements();
        if (e.hasMoreElements()) {
            while (e.hasMoreElements()) {
                buf.append(e.nextElement().toString());
                if (e.hasMoreElements()) {
                    buf.append(", ");
                }
            }
        }
        return buf.toString();
    }
    public void appendSelector(FileSelector selector) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        selectorsList.addElement(selector);
        setChecked(false);
    }
    public void validate() {
        if (isReference()) {
            ((AbstractSelectorContainer) getCheckedRef()).validate();
        }
        dieOnCircularReference();
        Enumeration e = selectorElements();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof BaseSelector) {
                ((BaseSelector) o).validate();
            }
        }
    }
    public void addSelector(SelectSelector selector) {
        appendSelector(selector);
    }
    public void addAnd(AndSelector selector) {
        appendSelector(selector);
    }
    public void addOr(OrSelector selector) {
        appendSelector(selector);
    }
    public void addNot(NotSelector selector) {
        appendSelector(selector);
    }
    public void addNone(NoneSelector selector) {
        appendSelector(selector);
    }
    public void addMajority(MajoritySelector selector) {
        appendSelector(selector);
    }
    public void addDate(DateSelector selector) {
        appendSelector(selector);
    }
    public void addSize(SizeSelector selector) {
        appendSelector(selector);
    }
    public void addFilename(FilenameSelector selector) {
        appendSelector(selector);
    }
    public void addCustom(ExtendSelector selector) {
        appendSelector(selector);
    }
    public void addContains(ContainsSelector selector) {
        appendSelector(selector);
    }
    public void addPresent(PresentSelector selector) {
        appendSelector(selector);
    }
    public void addDepth(DepthSelector selector) {
        appendSelector(selector);
    }
    public void addDepend(DependSelector selector) {
        appendSelector(selector);
    }
    public void addDifferent(DifferentSelector selector) {
        appendSelector(selector);
    }
    public void addType(TypeSelector selector) {
        appendSelector(selector);
    }
    public void addContainsRegexp(ContainsRegexpSelector selector) {
        appendSelector(selector);
    }
    public void addModified(ModifiedSelector selector) {
        appendSelector(selector);
    }
    public void addReadable(ReadableSelector r) {
        appendSelector(r);
    }
    public void addWritable(WritableSelector w) {
        appendSelector(w);
    }
    public void add(FileSelector selector) {
        appendSelector(selector);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p) {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = selectorsList.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            setChecked(true);
        }
    }
    public synchronized Object clone() {
        if (isReference()) {
            return ((AbstractSelectorContainer) getCheckedRef()).clone();
        }
        try {
            AbstractSelectorContainer sc =
                (AbstractSelectorContainer) super.clone();
            sc.selectorsList = new Vector(selectorsList);
            return sc;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
}
