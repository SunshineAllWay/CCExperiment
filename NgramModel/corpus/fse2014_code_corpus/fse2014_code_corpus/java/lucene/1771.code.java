package org.apache.lucene.store;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.lang.reflect.Method;
import org.apache.lucene.util.Constants;
public class MMapDirectory extends FSDirectory {
  public MMapDirectory(File path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
  }
  public MMapDirectory(File path) throws IOException {
    super(path, null);
  }
  private boolean useUnmapHack = false;
  private int maxBBuf = Constants.JRE_IS_64BIT ? Integer.MAX_VALUE : (256*1024*1024);
  public static final boolean UNMAP_SUPPORTED;
  static {
    boolean v;
    try {
      Class.forName("sun.misc.Cleaner");
      Class.forName("java.nio.DirectByteBuffer")
        .getMethod("cleaner");
      v = true;
    } catch (Exception e) {
      v = false;
    }
    UNMAP_SUPPORTED = v;
  }
  public void setUseUnmap(final boolean useUnmapHack) {
    if (useUnmapHack && !UNMAP_SUPPORTED)
      throw new IllegalArgumentException("Unmap hack not supported on this platform!");
    this.useUnmapHack=useUnmapHack;
  }
  public boolean getUseUnmap() {
    return useUnmapHack;
  }
  final void cleanMapping(final ByteBuffer buffer) throws IOException {
    if (useUnmapHack) {
      try {
        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
          public Object run() throws Exception {
            final Method getCleanerMethod = buffer.getClass()
              .getMethod("cleaner");
            getCleanerMethod.setAccessible(true);
            final Object cleaner = getCleanerMethod.invoke(buffer);
            if (cleaner != null) {
              cleaner.getClass().getMethod("clean")
                .invoke(cleaner);
            }
            return null;
          }
        });
      } catch (PrivilegedActionException e) {
        final IOException ioe = new IOException("unable to unmap the mapped buffer");
        ioe.initCause(e.getCause());
        throw ioe;
      }
    }
  }
  public void setMaxChunkSize(final int maxBBuf) {
    if (maxBBuf<=0)
      throw new IllegalArgumentException("Maximum chunk size for mmap must be >0");
    this.maxBBuf=maxBBuf;
  }
  public int getMaxChunkSize() {
    return maxBBuf;
  } 
  private class MMapIndexInput extends IndexInput {
    private ByteBuffer buffer;
    private final long length;
    private boolean isClone = false;
    private MMapIndexInput(RandomAccessFile raf) throws IOException {
        this.length = raf.length();
        this.buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, length);
    }
    @Override
    public byte readByte() throws IOException {
      try {
        return buffer.get();
      } catch (BufferUnderflowException e) {
        throw new IOException("read past EOF");
      }
    }
    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
      try {
        buffer.get(b, offset, len);
      } catch (BufferUnderflowException e) {
        throw new IOException("read past EOF");
      }
    }
    @Override
    public long getFilePointer() {
      return buffer.position();
    }
    @Override
    public void seek(long pos) throws IOException {
      buffer.position((int)pos);
    }
    @Override
    public long length() {
      return length;
    }
    @Override
    public Object clone() {
      MMapIndexInput clone = (MMapIndexInput)super.clone();
      clone.isClone = true;
      clone.buffer = buffer.duplicate();
      return clone;
    }
    @Override
    public void close() throws IOException {
      if (isClone || buffer == null) return;
      try {
        cleanMapping(buffer);
      } finally {
        buffer = null;
      }
    }
  }
  private class MultiMMapIndexInput extends IndexInput {
    private ByteBuffer[] buffers;
    private int[] bufSizes; 
    private final long length;
    private int curBufIndex;
    private final int maxBufSize;
    private ByteBuffer curBuf; 
    private int curAvail; 
    private boolean isClone = false;
    public MultiMMapIndexInput(RandomAccessFile raf, int maxBufSize)
      throws IOException {
      this.length = raf.length();
      this.maxBufSize = maxBufSize;
      if (maxBufSize <= 0)
        throw new IllegalArgumentException("Non positive maxBufSize: "
                                           + maxBufSize);
      if ((length / maxBufSize) > Integer.MAX_VALUE)
        throw new IllegalArgumentException
          ("RandomAccessFile too big for maximum buffer size: "
           + raf.toString());
      int nrBuffers = (int) (length / maxBufSize);
      if (((long) nrBuffers * maxBufSize) < length) nrBuffers++;
      this.buffers = new ByteBuffer[nrBuffers];
      this.bufSizes = new int[nrBuffers];
      long bufferStart = 0;
      FileChannel rafc = raf.getChannel();
      for (int bufNr = 0; bufNr < nrBuffers; bufNr++) { 
        int bufSize = (length > (bufferStart + maxBufSize))
          ? maxBufSize
          : (int) (length - bufferStart);
        this.buffers[bufNr] = rafc.map(MapMode.READ_ONLY,bufferStart,bufSize);
        this.bufSizes[bufNr] = bufSize;
        bufferStart += bufSize;
      }
      seek(0L);
    }
    @Override
    public byte readByte() throws IOException {
      if (curAvail == 0) {
        curBufIndex++;
        if (curBufIndex >= buffers.length)
          throw new IOException("read past EOF");
        curBuf = buffers[curBufIndex];
        curBuf.position(0);
        curAvail = bufSizes[curBufIndex];
      }
      curAvail--;
      return curBuf.get();
    }
    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
      while (len > curAvail) {
        curBuf.get(b, offset, curAvail);
        len -= curAvail;
        offset += curAvail;
        curBufIndex++;
        if (curBufIndex >= buffers.length)
          throw new IOException("read past EOF");
        curBuf = buffers[curBufIndex];
        curBuf.position(0);
        curAvail = bufSizes[curBufIndex];
      }
      curBuf.get(b, offset, len);
      curAvail -= len;
    }
    @Override
    public long getFilePointer() {
      return ((long) curBufIndex * maxBufSize) + curBuf.position();
    }
    @Override
    public void seek(long pos) throws IOException {
      curBufIndex = (int) (pos / maxBufSize);
      curBuf = buffers[curBufIndex];
      int bufOffset = (int) (pos - ((long) curBufIndex * maxBufSize));
      curBuf.position(bufOffset);
      curAvail = bufSizes[curBufIndex] - bufOffset;
    }
    @Override
    public long length() {
      return length;
    }
    @Override
    public Object clone() {
      MultiMMapIndexInput clone = (MultiMMapIndexInput)super.clone();
      clone.isClone = true;
      clone.buffers = new ByteBuffer[buffers.length];
      for (int bufNr = 0; bufNr < buffers.length; bufNr++) {
        clone.buffers[bufNr] = buffers[bufNr].duplicate();
      }
      try {
        clone.seek(getFilePointer());
      } catch(IOException ioe) {
        RuntimeException newException = new RuntimeException(ioe);
        newException.initCause(ioe);
        throw newException;
      }
      return clone;
    }
    @Override
    public void close() throws IOException {
      if (isClone || buffers == null) return;
      try {
        for (int bufNr = 0; bufNr < buffers.length; bufNr++) {
          try {
            cleanMapping(buffers[bufNr]);
          } finally {
            buffers[bufNr] = null;
          }
        }
      } finally {
        buffers = null;
      }
    }
  }
  @Override
  public IndexInput openInput(String name, int bufferSize) throws IOException {
    ensureOpen();
    File f =  new File(getDirectory(), name);
    RandomAccessFile raf = new RandomAccessFile(f, "r");
    try {
      return (raf.length() <= maxBBuf)
             ? (IndexInput) new MMapIndexInput(raf)
             : (IndexInput) new MultiMMapIndexInput(raf, maxBBuf);
    } finally {
      raf.close();
    }
  }
  @Override
  public IndexOutput createOutput(String name) throws IOException {
    initOutput(name);
    return new SimpleFSDirectory.SimpleFSIndexOutput(new File(directory, name));
  }
}
