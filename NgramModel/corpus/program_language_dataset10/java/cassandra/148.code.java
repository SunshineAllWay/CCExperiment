package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import java.util.Collection;
import org.apache.cassandra.db.marshal.AbstractType;
public interface IColumnContainer
{
    public void addColumn(IColumn column);
    public void remove(ByteBuffer columnName);
    public boolean isMarkedForDelete();
    public long getMarkedForDeleteAt();
    public AbstractType getComparator();
    public Collection<IColumn> getSortedColumns();
}
