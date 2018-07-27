package org.apache.tools.ant.util;
import java.lang.ref.WeakReference;
public class WeakishReference  {
    private WeakReference weakref;
    WeakishReference(Object reference) {
        this.weakref = new WeakReference(reference);
    }
    public Object get() {
        return weakref.get();
    }
    public static WeakishReference createReference(Object object) {
            return new WeakishReference(object);
    }
    public static class HardReference extends WeakishReference {
        public HardReference(Object object) {
            super(object);
        }
    }
}
