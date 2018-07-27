package org.apache.tools.ant.types.selectors.modifiedselector;
import java.util.Comparator;
public class EqualComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 1;
            }
            return 0;
        }
        return (o1.equals(o2)) ? 0 : 1;
    }
    public String toString() {
        return "EqualComparator";
    }
}
