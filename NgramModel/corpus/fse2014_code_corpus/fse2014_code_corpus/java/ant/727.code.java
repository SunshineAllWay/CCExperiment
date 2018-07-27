package org.apache.tools.ant.types.selectors;
import java.io.File;
public class DependSelector extends MappingSelector {
    public DependSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{dependselector targetdir: ");
        if (targetdir == null) {
            buf.append("NOT YET SET");
        } else {
            buf.append(targetdir.getName());
        }
        buf.append(" granularity: ");
        buf.append(granularity);
        if (map != null) {
            buf.append(" mapper: ");
            buf.append(map.toString());
        } else if (mapperElement != null) {
            buf.append(" mapper: ");
            buf.append(mapperElement.toString());
        }
        buf.append("}");
        return buf.toString();
    }
    public boolean selectionTest(File srcfile, File destfile) {
        boolean selected = SelectorUtils.isOutOfDate(srcfile, destfile,
                granularity);
        return selected;
    }
}
