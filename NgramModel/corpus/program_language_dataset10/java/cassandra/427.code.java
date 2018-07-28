package org.apache.cassandra.utils;
import java.util.Iterator;
import com.google.common.collect.AbstractIterator;
public abstract class ReducingIterator<T1, T2> extends AbstractIterator<T2> implements Iterator<T2>, Iterable<T2>
{
    protected Iterator<T1> source;
    protected T1 last;
    public ReducingIterator(Iterator<T1> source)
    {
        this.source = source;
    }
    public abstract void reduce(T1 current);
    protected abstract T2 getReduced();
    protected boolean isEqual(T1 o1, T1 o2)
    {
        return o1.equals(o2);
    }
    protected T2 computeNext()
    {
        if (last == null && !source.hasNext())
            return endOfData();
        boolean keyChanged = false;
        while (!keyChanged)
        {
            if (last != null)
                reduce(last);
            if (!source.hasNext())
            {
                last = null;
                break;
            }
            T1 current = source.next();
            if (last != null && !isEqual(current, last))
                keyChanged = true;
            last = current;
        }
        return getReduced();
    }
    public Iterator<T2> iterator()
    {
        return this;
    }
}
