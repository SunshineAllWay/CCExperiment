package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import org.apache.cassandra.db.IColumn;
public abstract class AbstractType implements Comparator<ByteBuffer>
{
    public abstract String getString(ByteBuffer bytes);
    public ByteBuffer fromString(String source)
    {
        throw new UnsupportedOperationException();
    }
    public abstract void validate(ByteBuffer bytes) throws MarshalException;
    public Comparator<ByteBuffer> getReverseComparator()
    {
        return new Comparator<ByteBuffer>()
        {
            public int compare(ByteBuffer o1, ByteBuffer o2)
            {
                if (o1.remaining() == 0)
                {
                    return o2.remaining() == 0 ? 0 : -1;
                }
                if (o2.remaining() == 0)
                {
                    return 1;
                }
                return -AbstractType.this.compare(o1, o2);
            }
        };
    }
    public String getString(Collection<ByteBuffer> names)
    {
        StringBuilder builder = new StringBuilder();
        for (ByteBuffer name : names)
        {
            builder.append(getString(name)).append(",");
        }
        return builder.toString();
    }
    public String getColumnsString(Collection<IColumn> columns)
    {
        StringBuilder builder = new StringBuilder();
        for (IColumn column : columns)
        {
            builder.append(column.getString(this)).append(",");
        }
        return builder.toString();
    }
    public boolean isCommutative()
    {
        return false;
    }
}
