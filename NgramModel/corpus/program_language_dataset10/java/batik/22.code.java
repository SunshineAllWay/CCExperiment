package org.apache.batik.anim.timing;
public class InstanceTime implements Comparable {
    protected float time;
    protected TimingSpecifier creator;
    protected boolean clearOnReset;
    public InstanceTime(TimingSpecifier creator,
                        float time,
                        boolean clearOnReset) {
        this.creator = creator;
        this.time = time;
        this.clearOnReset = clearOnReset;
    }
    public boolean getClearOnReset() {
        return clearOnReset;
    }
    public float getTime() {
        return time;
    }
    float dependentUpdate(float newTime) {
        time = newTime;
        if (creator != null) {
            return creator.handleTimebaseUpdate(this, time);
        }
        return Float.POSITIVE_INFINITY;
    }
    public String toString() {
        return Float.toString(time);
    }
    public int compareTo(Object o) {
        InstanceTime it = (InstanceTime)o;
        if (time == it.time) return 0;
        if (time >  it.time) return 1;
        return -1;
    }
}
