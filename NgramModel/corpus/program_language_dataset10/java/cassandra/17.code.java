package org.apache.cassandra.contrib.stress.util;
import java.util.Iterator;
public class Range implements Iterable<Integer>, Iterator<Integer>
{
    private int begin, current, limit;
    public Range(int end)
    {
        this(0, end);
    }
    public Range(int start, int end)
    {
        begin = current = start;
        limit = end;
    }
    public boolean hasNext()
    {
        return (current < limit);
    }
    public Integer next()
    {
        return current++;
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    public Iterator<Integer> iterator()
    {
        return this;
    }
    public int begins()
    {
        return begin;
    }
    public int limit()
    {
        return limit;
    }
    public int size()
    {
        return limit - begin;
    }
}
