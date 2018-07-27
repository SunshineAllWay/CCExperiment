package org.apache.cassandra.io.util;
import java.io.IOError;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.utils.Pair;
public abstract class SegmentedFile
{
    public final String path;
    public final long length;
    SegmentedFile(String path, long length)
    {
        this.path = path;
        this.length = length;
    }
    public static Builder getBuilder(Config.DiskAccessMode mode)
    {
        return mode == Config.DiskAccessMode.mmap
               ? new MmappedSegmentedFile.Builder()
               : new BufferedSegmentedFile.Builder();
    }
    public abstract FileDataInput getSegment(long position, int bufferSize);
    public Iterator<FileDataInput> iterator(long position, int bufferSize)
    {
        return new SegmentIterator(position, bufferSize);
    }
    public static abstract class Builder
    {
        public abstract void addPotentialBoundary(long boundary);
        public abstract SegmentedFile complete(String path);
    }
    static final class Segment extends Pair<Long, MappedByteBuffer> implements Comparable<Segment>
    {
        public Segment(long offset, MappedByteBuffer segment)
        {
            super(offset, segment);
        }
        public final int compareTo(Segment that)
        {
            return (int)Math.signum(this.left - that.left);
        }
    }
    final class SegmentIterator implements Iterator<FileDataInput>
    {
        private long nextpos;
        private final int bufferSize;
        public SegmentIterator(long position, int bufferSize)
        {
            this.nextpos = position;
            this.bufferSize = bufferSize;
        }
        public boolean hasNext()
        {
            return nextpos < length;
        }
        public FileDataInput next()
        {
            long position = nextpos;
            if (position >= length)
                throw new NoSuchElementException();
            FileDataInput segment = getSegment(nextpos, bufferSize);
            try
            {
                nextpos = nextpos + segment.bytesRemaining();
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
            return segment;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }
}
