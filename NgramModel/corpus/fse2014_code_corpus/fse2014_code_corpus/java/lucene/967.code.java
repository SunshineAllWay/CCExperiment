package org.apache.lucene.store.je;
import java.io.IOException;
import org.apache.lucene.store.IndexInput;
public class JEIndexInput extends IndexInput {
    protected long position = 0L, length = 0L;
    protected JEDirectory directory;
    protected Block block;
    protected File file;
    protected JEIndexInput(JEDirectory directory, String name)
            throws IOException {
        super();
        this.directory = directory;
        this.file = new File(name);
        if (!file.exists(directory))
            throw new IOException("File does not exist: " + name);
        length = file.getLength();
        block = new Block(file);
        block.get(directory);
    }
    @Override
    public Object clone() {
        try {
            JEIndexInput clone = (JEIndexInput) super.clone();
            clone.block = new Block(file);
            clone.block.seek(position);
            clone.block.get(directory);
            return clone;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @Override
    public void close() throws IOException {
    }
    @Override
    public long length() {
        return length;
    }
    @Override
    public byte readByte() throws IOException {
        if (position + 1 > length)
            throw new IOException(file.getName() + ": Reading past end of file");
        int blockPos = (int) (position++ & JEIndexOutput.BLOCK_MASK);
        byte b = block.getData()[blockPos];
        if (blockPos + 1 == JEIndexOutput.BLOCK_LEN) {
            block.seek(position);
            block.get(directory);
        }
        return b;
    }
    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        if (position + len > length)
            throw new IOException("Reading past end of file");
        else {
            int blockPos = (int) (position & JEIndexOutput.BLOCK_MASK);
            while (blockPos + len >= JEIndexOutput.BLOCK_LEN) {
                int blockLen = JEIndexOutput.BLOCK_LEN - blockPos;
                System
                        .arraycopy(block.getData(), blockPos, b, offset,
                                blockLen);
                len -= blockLen;
                offset += blockLen;
                position += blockLen;
                block.seek(position);
                block.get(directory);
                blockPos = 0;
            }
            if (len > 0) {
                System.arraycopy(block.getData(), blockPos, b, offset, len);
                position += len;
            }
        }
    }
    @Override
    public void seek(long pos) throws IOException {
        if (pos > length)
            throw new IOException("seeking past end of file");
        if ((pos >>> JEIndexOutput.BLOCK_SHIFT) != (position >>> JEIndexOutput.BLOCK_SHIFT)) {
            block.seek(pos);
            block.get(directory);
        }
        position = pos;
    }
    @Override
    public long getFilePointer() {
        return position;
    }
}
