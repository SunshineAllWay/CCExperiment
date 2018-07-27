package org.apache.tools.ant.types.resources.selectors;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Comparison;
public class Size implements ResourceSelector {
    private long size = -1;
    private Comparison when = Comparison.EQUAL;
    public void setSize(long l) {
        size = l;
    }
    public long getSize() {
        return size;
    }
    public void setWhen(Comparison c) {
        when = c;
    }
    public Comparison getWhen() {
        return when;
    }
    public boolean isSelected(Resource r) {
        long diff = r.getSize() - size;
        return when.evaluate(diff == 0 ? 0 : (int) (diff / Math.abs(diff)));
    }
}
