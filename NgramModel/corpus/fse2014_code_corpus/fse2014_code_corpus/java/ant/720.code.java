package org.apache.tools.ant.types.selectors;
import java.io.File;
import java.util.Enumeration;
public class AndSelector extends BaseSelectorContainer {
    public AndSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (hasSelectors()) {
            buf.append("{andselect: ");
            buf.append(super.toString());
            buf.append("}");
        }
        return buf.toString();
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        Enumeration e = selectorElements();
        boolean result;
        while (e.hasMoreElements()) {
            result = ((FileSelector) e.nextElement()).isSelected(basedir,
                    filename, file);
            if (!result) {
                return false;
            }
        }
        return true;
    }
}
