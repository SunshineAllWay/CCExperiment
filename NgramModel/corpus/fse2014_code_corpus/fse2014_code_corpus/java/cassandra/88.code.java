package org.apache.cassandra.concurrent;
import java.util.HashMap;
import java.util.Map;
public class Context
{
    private Map<Object, Object> ht_;
    public Context()
    {
        ht_ = new HashMap<Object, Object>();
    }
    public Object put(Object key, Object value)
    {
        return ht_.put(key, value);
    }
    public Object get(Object key)
    {
        return ht_.get(key);
    }
    public void remove(Object key)
    {
        ht_.remove(key);
    }
}
