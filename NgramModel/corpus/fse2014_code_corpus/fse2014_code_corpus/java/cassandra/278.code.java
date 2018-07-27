package org.apache.cassandra.io.sstable;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
public class IndexSummary
{
    private ArrayList<KeyPosition> indexPositions;
    private long keysWritten = 0;
    public IndexSummary(long expectedKeys)
    {
        long expectedEntries = expectedKeys / DatabaseDescriptor.getIndexInterval();
        if (expectedEntries > Integer.MAX_VALUE)
            throw new RuntimeException("Cannot use index_interval of " + DatabaseDescriptor.getIndexInterval() + " with " + expectedKeys + " (expected) keys.");
        indexPositions = new ArrayList<KeyPosition>((int)expectedEntries);
    }
    public void incrementRowid()
    {
        keysWritten++;
    }
    public boolean shouldAddEntry()
    {
        return keysWritten % DatabaseDescriptor.getIndexInterval() == 0;
    }
    public void addEntry(DecoratedKey decoratedKey, long indexPosition)
    {
        indexPositions.add(new KeyPosition(decoratedKey, indexPosition));
    }
    public void maybeAddEntry(DecoratedKey decoratedKey, long indexPosition)
    {
        if (shouldAddEntry())
            addEntry(decoratedKey, indexPosition);
        incrementRowid();
    }
    public List<KeyPosition> getIndexPositions()
    {
        return indexPositions;
    }
    public void complete()
    {
        indexPositions.trimToSize();
    }
    public static final class KeyPosition implements Comparable<KeyPosition>
    {
        public final DecoratedKey key;
        public final long indexPosition;
        public KeyPosition(DecoratedKey key, long indexPosition)
        {
            this.key = key;
            this.indexPosition = indexPosition;
        }
        public int compareTo(KeyPosition kp)
        {
            return key.compareTo(kp.key);
        }
        public String toString()
        {
            return key + ":" + indexPosition;
        }
    }
}
