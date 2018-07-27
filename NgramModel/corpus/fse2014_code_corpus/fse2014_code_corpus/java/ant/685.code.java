package org.apache.tools.ant.types.resources;
import org.apache.tools.ant.BuildException;
public abstract class SizeLimitCollection extends BaseResourceCollectionWrapper {
    private static final String BAD_COUNT
        = "size-limited collection count should be set to an int >= 0";
    private int count = 1;
    public synchronized void setCount(int i) {
        checkAttributesAllowed();
        count = i;
    }
    public synchronized int getCount() {
        return count;
    }
    public synchronized int size() {
        int sz = getResourceCollection().size();
        int ct = getValidCount();
        return sz < ct ? sz : ct;
    }
    protected int getValidCount() {
        int ct = getCount();
        if (ct < 0) {
            throw new BuildException(BAD_COUNT);
        }
        return ct;
    }
}
