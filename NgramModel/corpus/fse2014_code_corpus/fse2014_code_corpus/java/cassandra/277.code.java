package org.apache.cassandra.io.sstable;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.io.util.FileMark;
import org.apache.cassandra.utils.*;
public class IndexHelper
{
    public static void skipBloomFilter(FileDataInput in) throws IOException
    {
        int size = in.readInt();
        int skipped = in.skipBytes(size);
        if (skipped != size)
            throw new EOFException("attempted to skip " + size + " bytes but only skipped " + skipped);
    }
	public static void skipIndex(FileDataInput file) throws IOException
	{
        int columnIndexSize = file.readInt();
        if (file.skipBytes(columnIndexSize) != columnIndexSize)
            throw new EOFException();
	}
	public static ArrayList<IndexInfo> deserializeIndex(FileDataInput in) throws IOException
	{
		int columnIndexSize = in.readInt();
        if (columnIndexSize == 0)
            return null;
        ArrayList<IndexInfo> indexList = new ArrayList<IndexInfo>();
        FileMark mark = in.mark();
        while (in.bytesPastMark(mark) < columnIndexSize)
        {
            indexList.add(IndexInfo.deserialize(in));
        }
        assert in.bytesPastMark(mark) == columnIndexSize;
        return indexList;
	}
    public static Filter defreezeBloomFilter(FileDataInput file, boolean useOldBuffer) throws IOException
    {
        int size = file.readInt();
        ByteBuffer bytes = file.readBytes(size);
        DataInputStream stream = new DataInputStream(ByteBufferUtil.inputStream(bytes));
        return useOldBuffer
                ? LegacyBloomFilter.serializer().deserialize(stream)
                : BloomFilter.serializer().deserialize(stream);
    }
    public static int indexFor(ByteBuffer name, List<IndexInfo> indexList, AbstractType comparator, boolean reversed)
    {
        if (name.remaining() == 0 && reversed)
            return indexList.size() - 1;
        IndexInfo target = new IndexInfo(name, name, 0, 0);
        int index = Collections.binarySearch(indexList, target, getComparator(comparator));
        return index < 0 ? -1 * (index + 1) : index;
    }
    public static Comparator<IndexInfo> getComparator(final AbstractType nameComparator)
    {
        return new Comparator<IndexInfo>()
        {
            public int compare(IndexInfo o1, IndexInfo o2)
            {
                return nameComparator.compare(o1.lastName, o2.lastName);
            }
        };
    }
    public static class IndexInfo
    {
        public final long width;
        public final ByteBuffer lastName;
        public final ByteBuffer firstName;
        public final long offset;
        public IndexInfo(ByteBuffer firstName, ByteBuffer lastName, long offset, long width)
        {
            this.firstName = firstName;
            this.lastName = lastName;
            this.offset = offset;
            this.width = width;
        }
        public void serialize(DataOutput dos) throws IOException
        {
            ByteBufferUtil.writeWithShortLength(firstName, dos);
            ByteBufferUtil.writeWithShortLength(lastName, dos);
            dos.writeLong(offset);
            dos.writeLong(width);
        }
        public int serializedSize()
        {
            return 2 + firstName.remaining() + 2 + lastName.remaining() + 8 + 8;
        }
        public static IndexInfo deserialize(FileDataInput dis) throws IOException
        {
            return new IndexInfo(ByteBufferUtil.readWithShortLength(dis), ByteBufferUtil.readWithShortLength(dis), dis.readLong(), dis.readLong());
        }
    }
}
