package org.apache.cassandra.db;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.ICompactSerializer2;
import org.apache.cassandra.io.util.ColumnSortedMap;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class SuperColumn implements IColumn, IColumnContainer
{
    private static Logger logger_ = LoggerFactory.getLogger(SuperColumn.class);
    public static SuperColumnSerializer serializer(AbstractType comparator)
    {
        return new SuperColumnSerializer(comparator);
    }
    private ByteBuffer name_;
    private ConcurrentSkipListMap<ByteBuffer, IColumn> columns_;
    private AtomicInteger localDeletionTime = new AtomicInteger(Integer.MIN_VALUE);
    private AtomicLong markedForDeleteAt = new AtomicLong(Long.MIN_VALUE);
    public SuperColumn(ByteBuffer name, AbstractType comparator)
    {
        this(name, new ConcurrentSkipListMap<ByteBuffer, IColumn>(comparator));
    }
    SuperColumn(ByteBuffer name, ConcurrentSkipListMap<ByteBuffer, IColumn> columns)
    {
        assert name != null;
        assert name.remaining() <= IColumn.MAX_NAME_LENGTH;
        name_ = name;
        columns_ = columns;
    }
    public AbstractType getComparator()
    {
        return (AbstractType)columns_.comparator();
    }
    public SuperColumn cloneMeShallow()
    {
        SuperColumn sc = new SuperColumn(name_, getComparator());
        sc.markForDeleteAt(localDeletionTime.get(), markedForDeleteAt.get());
        return sc;
    }
    public IColumn cloneMe()
    {
        SuperColumn sc = new SuperColumn(name_, new ConcurrentSkipListMap<ByteBuffer, IColumn>(columns_));
        sc.markForDeleteAt(localDeletionTime.get(), markedForDeleteAt.get());
        return sc;
    }
	public boolean isMarkedForDelete()
	{
        return markedForDeleteAt.get() > Long.MIN_VALUE;
	}
    public ByteBuffer name()
    {
    	return name_;
    }
    public Collection<IColumn> getSubColumns()
    {
    	return columns_.values();
    }
    public Collection<IColumn> getSortedColumns()
    {
        return getSubColumns();
    }
    public IColumn getSubColumn(ByteBuffer columnName)
    {
        IColumn column = columns_.get(columnName);
        assert column == null || column instanceof Column;
        return column;
    }
    public int size()
    {
        int size = 0;
        for (IColumn subColumn : getSubColumns())
        {
            size += subColumn.serializedSize();
        }
        return size;
    }
    public int serializedSize()
    {
      return DBConstants.shortSize_ + name_.remaining() + DBConstants.intSize_ + DBConstants.longSize_ + DBConstants.intSize_ + size();
    }
    public void remove(ByteBuffer columnName)
    {
    	columns_.remove(columnName);
    }
    public long timestamp()
    {
    	throw new UnsupportedOperationException("This operation is not supported for Super Columns.");
    }
    public long mostRecentLiveChangeAt()
    {
        long max = Long.MIN_VALUE;
        for (IColumn column : columns_.values())
        {
            if (!column.isMarkedForDelete() && column.timestamp() > max)
            {
                max = column.timestamp();
            }
        }
        return max;
    }
    public ByteBuffer value()
    {
    	throw new UnsupportedOperationException("This operation is not supported for Super Columns.");
    }
    public void addColumn(IColumn column)
    {
        assert column instanceof Column : "A super column can only contain simple columns";
        ByteBuffer name = column.name();
        IColumn oldColumn = columns_.putIfAbsent(name, column);
        if (oldColumn != null)
        {
            IColumn reconciledColumn = column.reconcile(oldColumn);
            while (!columns_.replace(name, oldColumn, reconciledColumn))
            {
                oldColumn = columns_.get(name);
                reconciledColumn = column.reconcile(oldColumn);
            }
    	}
    }
    public void putColumn(IColumn column)
    {
        assert column instanceof SuperColumn;
        for (IColumn subColumn : column.getSubColumns())
        {
        	addColumn(subColumn);
        }
        FBUtilities.atomicSetMax(localDeletionTime, column.getLocalDeletionTime()); 
        FBUtilities.atomicSetMax(markedForDeleteAt, column.getMarkedForDeleteAt());
    }
    public long getMarkedForDeleteAt()
    {
        return markedForDeleteAt.get();
    }
    public IColumn diff(IColumn columnNew)
    {
    	IColumn columnDiff = new SuperColumn(columnNew.name(), ((SuperColumn)columnNew).getComparator());
        if (columnNew.getMarkedForDeleteAt() > getMarkedForDeleteAt())
        {
            ((SuperColumn)columnDiff).markForDeleteAt(columnNew.getLocalDeletionTime(), columnNew.getMarkedForDeleteAt());
        }
        for (IColumn subColumn : columnNew.getSubColumns())
        {
        	IColumn columnInternal = columns_.get(subColumn.name());
        	if(columnInternal == null )
        	{
        		columnDiff.addColumn(subColumn);
        	}
        	else
        	{
            	IColumn subColumnDiff = columnInternal.diff(subColumn);
        		if(subColumnDiff != null)
        		{
            		columnDiff.addColumn(subColumnDiff);
        		}
        	}
        }
        if (!columnDiff.getSubColumns().isEmpty() || columnNew.isMarkedForDelete())
        	return columnDiff;
        else
        	return null;
    }
    public void updateDigest(MessageDigest digest)
    {
        assert name_ != null;
        digest.update(name_.duplicate());
        DataOutputBuffer buffer = new DataOutputBuffer();
        try
        {
            buffer.writeLong(markedForDeleteAt.get());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        digest.update(buffer.getData(), 0, buffer.getLength());
        for (IColumn column : columns_.values())
        {
            column.updateDigest(digest);
        }
    }
    public String getString(AbstractType comparator)
    {
    	StringBuilder sb = new StringBuilder();
        sb.append("SuperColumn(");
    	sb.append(comparator.getString(name_));
        if (isMarkedForDelete()) {
            sb.append(" -delete at ").append(getMarkedForDeleteAt()).append("-");
        }
        sb.append(" [");
        sb.append(getComparator().getColumnsString(columns_.values()));
        sb.append("])");
        return sb.toString();
    }
    public boolean isLive()
    {
        return mostRecentLiveChangeAt() > markedForDeleteAt.get();
    }
    public int getLocalDeletionTime()
    {
        return localDeletionTime.get();
    }
    @Deprecated 
    public void markForDeleteAt(int localDeleteTime, long timestamp)
    {
        this.localDeletionTime.set(localDeleteTime);
        this.markedForDeleteAt.set(timestamp);
    }
    public IColumn deepCopy()
    {
        SuperColumn sc = new SuperColumn(ByteBufferUtil.clone(name_), this.getComparator());
        sc.localDeletionTime = localDeletionTime;
        sc.markedForDeleteAt = markedForDeleteAt;
        for(Map.Entry<ByteBuffer, IColumn> c : columns_.entrySet())
        {
            sc.addColumn(c.getValue().deepCopy());
        }
        return sc;
    }
    public IColumn reconcile(IColumn c)
    {
        throw new UnsupportedOperationException("This operation is unsupported on super columns.");
    }
}
class SuperColumnSerializer implements ICompactSerializer2<IColumn>
{
    private static Logger logger = LoggerFactory.getLogger(SuperColumnSerializer.class);
    private AbstractType comparator;
    public SuperColumnSerializer(AbstractType comparator)
    {
        this.comparator = comparator;
    }
    public AbstractType getComparator()
    {
        return comparator;
    }
    public void serialize(IColumn column, DataOutput dos)
    {
        SuperColumn superColumn = (SuperColumn)column;
        ByteBufferUtil.writeWithShortLength(column.name(), dos);
        try
        {
            dos.writeInt(superColumn.getLocalDeletionTime());
            dos.writeLong(superColumn.getMarkedForDeleteAt());
            Collection<IColumn> columns = column.getSubColumns();
            dos.writeInt(columns.size());
            for (IColumn subColumn : columns)
            {
                Column.serializer().serialize(subColumn, dos);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public IColumn deserialize(DataInput dis) throws IOException
    {
        ByteBuffer name = ByteBufferUtil.readWithShortLength(dis);
        int localDeleteTime = dis.readInt();
        if (localDeleteTime != Integer.MIN_VALUE && localDeleteTime <= 0)
        {
            throw new IOException("Invalid localDeleteTime read: " + localDeleteTime);
        }
        long markedForDeleteAt = dis.readLong();
        int size = dis.readInt();
        ColumnSerializer serializer = Column.serializer();
        ColumnSortedMap preSortedMap = new ColumnSortedMap(comparator, serializer, dis, size);
        SuperColumn superColumn = new SuperColumn(name, new ConcurrentSkipListMap<ByteBuffer,IColumn>(preSortedMap));
        if (localDeleteTime != Integer.MIN_VALUE && localDeleteTime <= 0)
        {
            throw new IOException("Invalid localDeleteTime read: " + localDeleteTime);
        }
        superColumn.markForDeleteAt(localDeleteTime, markedForDeleteAt);
        return superColumn;
    }
}
