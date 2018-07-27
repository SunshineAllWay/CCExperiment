package org.apache.tools.ant.types.selectors;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;
public abstract class BaseSelectorContainer extends BaseSelector
        implements SelectorContainer {
    private Vector selectorsList = new Vector();
    public BaseSelectorContainer() {
    }
    public boolean hasSelectors() {
        dieOnCircularReference();
        return !(selectorsList.isEmpty());
    }
    public int selectorCount() {
        dieOnCircularReference();
        return selectorsList.size();
    }
    public FileSelector[] getSelectors(Project p) {
        dieOnCircularReference();
        FileSelector[] result = new FileSelector[selectorsList.size()];
        selectorsList.copyInto(result);
        return result;
    }
    public Enumeration selectorElements() {
        dieOnCircularReference();
        return selectorsList.elements();
    }
    public String toString() {
        dieOnCircularReference();
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
        selectorsList.addElement(selector);
        setChecked(false);
    }
    public void validate() {
        verifySettings();
        dieOnCircularReference();
        String errmsg = getError();
        if (errmsg != null) {
            throw new BuildException(errmsg);
        }
        Enumeration e = selectorElements();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof BaseSelector) {
                ((BaseSelector) o).validate();
            }
        }
    }
    public abstract boolean isSelected(File basedir, String filename,
                                       File file);
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
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = selectorsList.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            setChecked(true);
        }
    }
}
