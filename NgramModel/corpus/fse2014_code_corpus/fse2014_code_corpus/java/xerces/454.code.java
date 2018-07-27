package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.xs.ShortList;
public interface ValueStore {
    public void addValue(Field field, boolean mayMatch, Object actualValue, short valueType, ShortList itemValueType);
    public void reportError(String key, Object[] args);
} 
