package org.apache.cassandra.io.sstable;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.plaf.basic.BasicButtonListener;
import com.google.common.base.Function;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.cache.JMXInstrumentedCache;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.utils.Pair;
public class SSTableTracker implements Iterable<SSTableReader>
{
    private static final Logger logger = LoggerFactory.getLogger(SSTableTracker.class);
    private volatile Set<SSTableReader> sstables;
    private final AtomicLong liveSize = new AtomicLong();
    private final AtomicLong totalSize = new AtomicLong();
    private final String ksname;
    private final String cfname;
    private final JMXInstrumentedCache<Pair<Descriptor,DecoratedKey>,Long> keyCache;
    private final JMXInstrumentedCache<DecoratedKey, ColumnFamily> rowCache;
    public SSTableTracker(String ksname, String cfname)
    {
        this.ksname = ksname;
        this.cfname = cfname;
        sstables = Collections.emptySet();
        keyCache = new JMXInstrumentedCache<Pair<Descriptor,DecoratedKey>,Long>(ksname, cfname + "KeyCache", 0);
        rowCache = new JMXInstrumentedCache<DecoratedKey, ColumnFamily>(ksname, cfname + "RowCache", 3);
    }
    protected class CacheWriter<K, V>
    {
        public void saveCache(JMXInstrumentedCache<K, V> cache, File savedCachePath, Function<K, ByteBuffer> converter) throws IOException
        {
            long start = System.currentTimeMillis();
            String msgSuffix = savedCachePath.getName() + " for " + cfname + " of " + ksname;
            logger.info("saving " + msgSuffix);
            int count = 0;
            File tmpFile = File.createTempFile(savedCachePath.getName(), null, savedCachePath.getParentFile());
            FileOutputStream fout = null;
            ObjectOutputStream out = null;
            try
            {
                fout = new FileOutputStream(tmpFile);
                out = new ObjectOutputStream(new BufferedOutputStream(fout));
                FileDescriptor fd = fout.getFD();
                for (K key : cache.getKeySet())
                {
                    ByteBuffer bytes = converter.apply(key);
                    ByteBufferUtil.writeWithLength(bytes, out);
                    ++count;
                }
                out.flush();
                fd.sync();
            }
            finally
            {
                FileUtils.closeQuietly(out);
                FileUtils.closeQuietly(fout);
            }
            if (!tmpFile.renameTo(savedCachePath))
                throw new IOException("Unable to rename cache to " + savedCachePath);
            if (logger.isDebugEnabled())
                logger.debug("saved " + count + " keys in " + (System.currentTimeMillis() - start) + " ms from " + msgSuffix);
        }
    }
    public void saveKeyCache() throws IOException
    {
        Function<Pair<Descriptor, DecoratedKey>, ByteBuffer> function = new Function<Pair<Descriptor, DecoratedKey>, ByteBuffer>()
        {
            public ByteBuffer apply(Pair<Descriptor, DecoratedKey> key)
            {
                return key.right.key;
            }
        };
        CacheWriter<Pair<Descriptor, DecoratedKey>, Long> writer = new CacheWriter<Pair<Descriptor, DecoratedKey>, Long>();
        writer.saveCache(keyCache, DatabaseDescriptor.getSerializedKeyCachePath(ksname, cfname), function);
    }
    public void saveRowCache() throws IOException
    {
        Function<DecoratedKey, ByteBuffer> function = new Function<DecoratedKey, ByteBuffer>()
        {
            public ByteBuffer apply(DecoratedKey key)
            {
                return key.key;
            }
        };
        CacheWriter<DecoratedKey, ColumnFamily> writer = new CacheWriter<DecoratedKey, ColumnFamily>();
        writer.saveCache(rowCache, DatabaseDescriptor.getSerializedRowCachePath(ksname, cfname), function);
    }
    public synchronized void replace(Collection<SSTableReader> oldSSTables, Iterable<SSTableReader> replacements)
    {
        Set<SSTableReader> sstablesNew = new HashSet<SSTableReader>(sstables);
        for (SSTableReader sstable : replacements)
        {
            assert sstable.getKeySamples() != null;
            if (logger.isDebugEnabled())
                logger.debug(String.format("adding %s to list of files tracked for %s.%s",
                                           sstable.descriptor, ksname, cfname));
            sstablesNew.add(sstable);
            long size = sstable.bytesOnDisk();
            liveSize.addAndGet(size);
            totalSize.addAndGet(size);
            sstable.setTrackedBy(this);
        }
        long maxDataAge = -1;
        for (SSTableReader sstable : oldSSTables)
        {
            if (logger.isDebugEnabled())
                logger.debug(String.format("removing %s from list of files tracked for %s.%s",
                                           sstable.descriptor, ksname, cfname));
            boolean removed = sstablesNew.remove(sstable);
            assert removed;
            sstable.markCompacted();
            maxDataAge = Math.max(maxDataAge, sstable.maxDataAge);
            liveSize.addAndGet(-sstable.bytesOnDisk());
        }
        sstables = Collections.unmodifiableSet(sstablesNew);
        updateCacheSizes();
    }
    public synchronized void add(Iterable<SSTableReader> sstables)
    {
        assert sstables != null;
        replace(Collections.<SSTableReader>emptyList(), sstables);
    }
    public synchronized void markCompacted(Collection<SSTableReader> compacted)
    {
        replace(compacted, Collections.<SSTableReader>emptyList());
    }
    public synchronized void updateCacheSizes()
    {
        long keys = estimatedKeys();
        if (!keyCache.isCapacitySetManually())
        {
            int keyCacheSize = DatabaseDescriptor.getKeysCachedFor(ksname, cfname, keys);
            if (keyCacheSize != keyCache.getCapacity())
            {
                if (logger.isDebugEnabled())
                    logger.debug("key cache capacity for " + cfname + " is " + keyCacheSize);
                keyCache.updateCapacity(keyCacheSize);
            }
        }
        if (!rowCache.isCapacitySetManually())
        {
            int rowCacheSize = DatabaseDescriptor.getRowsCachedFor(ksname, cfname, keys);
            if (rowCacheSize != rowCache.getCapacity())
            {
                if (logger.isDebugEnabled())
                    logger.debug("row cache capacity for " + cfname + " is " + rowCacheSize);
                rowCache.updateCapacity(rowCacheSize);
            }
        }
    }
    public Set<SSTableReader> getSSTables()
    {
        return sstables;
    }
    public int size()
    {
        return sstables.size();
    }
    public Iterator<SSTableReader> iterator()
    {
        return sstables.iterator();
    }
    public synchronized void clearUnsafe()
    {
        sstables = Collections.emptySet();
    }
    public JMXInstrumentedCache<DecoratedKey, ColumnFamily> getRowCache()
    {
        return rowCache;
    }
    public long estimatedKeys()
    {
        long n = 0;
        for (SSTableReader sstable : this)
        {
            n += sstable.estimatedKeys();
        }
        return n;
    }
    public long getLiveSize()
    {
        return liveSize.get();
    }
    public long getTotalSize()
    {
        return totalSize.get();
    }
    public void spaceReclaimed(long size)
    {
        totalSize.addAndGet(-size);
    }
    public JMXInstrumentedCache<Pair<Descriptor, DecoratedKey>, Long> getKeyCache()
    {
        return keyCache;
    }
}
