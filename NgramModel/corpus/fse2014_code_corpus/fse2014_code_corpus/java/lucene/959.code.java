package org.apache.lucene.store.db;
import java.io.IOException;
import org.apache.lucene.store.IndexOutput;
public class DbIndexOutput extends IndexOutput {
    static public final int BLOCK_SHIFT = 14;
    static public final int BLOCK_LEN = 1 << BLOCK_SHIFT;
    static public final int BLOCK_MASK = BLOCK_LEN - 1;
    protected long position = 0L, length = 0L;
    protected DbDirectory directory;
    protected Block block;
    protected File file;
    protected DbIndexOutput(DbDirectory directory, String name, boolean create)
        throws IOException
    {
        super();
        this.directory = directory;
        file = new File(directory, name, create);
        block = new Block(file);
        length = file.getLength();
        seek(length);
        block.get(directory);
        directory.openFiles.add(this);
    }
    @Override
    public void close()
        throws IOException
    {
        flush();
        file.modify(directory, length, System.currentTimeMillis());
        directory.openFiles.remove(this);
    }
    @Override
    public void flush()
        throws IOException
    {
        if (length > 0)
            block.put(directory);
    }
    @Override
    public void writeByte(byte b)
        throws IOException
    {
        int blockPos = (int) (position++ & BLOCK_MASK);
        block.getData()[blockPos] = b;
        if (blockPos + 1 == BLOCK_LEN)
        {
            block.put(directory);
            block.seek(position);
            block.get(directory);
        }
        if (position > length)
            length = position;
    }
    @Override
    public void writeBytes(byte[] b, int offset, int len)
        throws IOException
    {
        int blockPos = (int) (position & BLOCK_MASK);
        while (blockPos + len >= BLOCK_LEN) {
            int blockLen = BLOCK_LEN - blockPos;
            System.arraycopy(b, offset, block.getData(), blockPos, blockLen);
            block.put(directory);
            len -= blockLen;
            offset += blockLen;
            position += blockLen;
            block.seek(position);
            block.get(directory);
            blockPos = 0;
        }
        if (len > 0)
        {
            System.arraycopy(b, offset, block.getData(), blockPos, len);
            position += len;
        }
        if (position > length)
            length = position;
    }
    @Override
    public long length()
        throws IOException
    {
        return length;
    }
    @Override
    public void seek(long pos)
        throws IOException
    {
        if (pos > length)
            throw new IOException("seeking past end of file");
        if ((pos >>> BLOCK_SHIFT) == (position >>> BLOCK_SHIFT))
            position = pos;
        else
        {
            block.put(directory);
            block.seek(pos);
            block.get(directory);
            position = pos;
        }
    }
    @Override
    public long getFilePointer()
    {
        return position;
    }
}
