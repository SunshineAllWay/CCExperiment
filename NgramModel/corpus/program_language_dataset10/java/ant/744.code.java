package org.apache.tools.ant.types.selectors;
import java.util.Enumeration;
import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
public class SelectSelector extends BaseSelectorContainer {
    private Object ifCondition;
    private Object unlessCondition;
    public SelectSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (hasSelectors()) {
            buf.append("{select");
            if (ifCondition != null) {
                buf.append(" if: ");
                buf.append(ifCondition);
            }
            if (unlessCondition != null) {
                buf.append(" unless: ");
                buf.append(unlessCondition);
            }
            buf.append(" ");
            buf.append(super.toString());
            buf.append("}");
        }
        return buf.toString();
    }
    private SelectSelector getRef() {
        Object o = getCheckedRef(this.getClass(), "SelectSelector");
        return (SelectSelector) o;
    }
    public boolean hasSelectors() {
        if (isReference()) {
            return getRef().hasSelectors();
        }
        return super.hasSelectors();
    }
    public int selectorCount() {
        if (isReference()) {
            return getRef().selectorCount();
        }
        return super.selectorCount();
    }
    public FileSelector[] getSelectors(Project p) {
        if (isReference()) {
            return getRef().getSelectors(p);
        }
        return super.getSelectors(p);
    }
    public Enumeration selectorElements() {
        if (isReference()) {
            return getRef().selectorElements();
        }
        return super.selectorElements();
    }
    public void appendSelector(FileSelector selector) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        super.appendSelector(selector);
    }
    public void verifySettings() {
        int cnt = selectorCount();
        if (cnt < 0 || cnt > 1) {
            setError("Only one selector is allowed within the "
                + "<selector> tag");
        }
    }
    public boolean passesConditions() {
        PropertyHelper ph = PropertyHelper.getPropertyHelper(getProject());
        return ph.testIfCondition(ifCondition)
            && ph.testUnlessCondition(unlessCondition);
    }
    public void setIf(Object ifProperty) {
        this.ifCondition = ifProperty;
    }
    public void setIf(String ifProperty) {
        setIf((Object) ifProperty);
    }
    public void setUnless(Object unlessProperty) {
        this.unlessCondition = unlessProperty;
    }
    public void setUnless(String unlessProperty) {
        setUnless((Object) unlessProperty);
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        if (!(passesConditions())) {
            return false;
        }
        Enumeration e = selectorElements();
        if (!(e.hasMoreElements())) {
            return true;
        }
        FileSelector f = (FileSelector) e.nextElement();
        return f.isSelected(basedir, filename, file);
    }
}
