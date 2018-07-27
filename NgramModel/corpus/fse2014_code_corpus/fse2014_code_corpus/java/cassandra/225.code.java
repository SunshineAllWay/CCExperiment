package org.apache.cassandra.dht;
import java.util.Collections;
import java.util.List;
import org.apache.cassandra.service.StorageService;
public class Bounds extends AbstractBounds
{
    public Bounds(Token left, Token right)
    {
        this(left, right, StorageService.getPartitioner());
    }
    Bounds(Token left, Token right, IPartitioner partitioner)
    {
        super(left, right, partitioner);
        assert left.compareTo(right) <= 0 || right.equals(partitioner.getMinimumToken()) : "[" + left + "," + right + "]";
    }
    public boolean contains(Token token)
    {
        return Range.contains(left, right, token) || left.equals(token);
    }
    public AbstractBounds createFrom(Token token)
    {
        return new Bounds(left, token);
    }
    public List<AbstractBounds> unwrap()
    {
        return Collections.<AbstractBounds>singletonList(this);
    }
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Bounds))
            return false;
        Bounds rhs = (Bounds)o;
        return left.equals(rhs.left) && right.equals(rhs.right);
    }
    public String toString()
    {
        return "[" + left + "," + right + "]";
    }
}
