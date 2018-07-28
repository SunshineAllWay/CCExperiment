package org.apache.tools.ant.types.selectors;
import java.util.Comparator;
public class MockComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        return 0;
    }
    public String toString() {
        return "MockComparator";
    }
}
