package org.apache.tools.tar;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;
public class TarOutputStream extends FilterOutputStream {
    public static final int LONGFILE_ERROR = 0;
    public static final int LONGFILE_TRUNCATE = 1;
    public static final int LONGFILE_GNU = 2;
    protected boolean   debug;
    protected long      currSize;
    protected String    currName;
    protected long      currBytes;
    protected byte[]    oneBuf;
    protected byte[]    recordBuf;
    protected int       assemLen;
    protected byte[]    assemBuf;
    protected TarBuffer buffer;
    protected int       longFileMode = LONGFILE_ERROR;
    private boolean closed = false;
    public TarOutputStream(OutputStream os) {
        this(os, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarOutputStream(OutputStream os, int blockSize) {
        this(os, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarOutputStream(OutputStream os, int blockSize, int recordSize) {
        super(os);
        this.buffer = new TarBuffer(os, blockSize, recordSize);
        this.debug = false;
        this.assemLen = 0;
        this.assemBuf = new byte[recordSize];
        this.recordBuf = new byte[recordSize];
        this.oneBuf = new byte[1];
    }
    public void setLongFileMode(int longFileMode) {
        this.longFileMode = longFileMode;
    }
    public void setDebug(boolean debugF) {
        this.debug = debugF;
    }
    public void setBufferDebug(boolean debug) {
        buffer.setDebug(debug);
    }
    public void finish() throws IOException {
        writeEOFRecord();
        writeEOFRecord();
        buffer.flushBlock();
    }
    public void close() throws IOException {
        if (!closed) {
            finish();
            buffer.close();
            out.close();
            closed = true;
        }
    }
    public int getRecordSize() {
        return buffer.getRecordSize();
    }
    public void putNextEntry(TarEntry entry) throws IOException {
        if (entry.getName().length() >= TarConstants.NAMELEN) {
            if (longFileMode == LONGFILE_GNU) {
                TarEntry longLinkEntry = new TarEntry(TarConstants.GNU_LONGLINK,
                                                      TarConstants.LF_GNUTYPE_LONGNAME);
                longLinkEntry.setSize(entry.getName().length() + 1);
                putNextEntry(longLinkEntry);
                write(entry.getName().getBytes());
                write(0);
                closeEntry();
            } else if (longFileMode != LONGFILE_TRUNCATE) {
                throw new RuntimeException("file name '" + entry.getName()
                                             + "' is too long ( > "
                                             + TarConstants.NAMELEN + " bytes)");
            }
        }
        entry.writeEntryHeader(recordBuf);
        buffer.writeRecord(recordBuf);
        currBytes = 0;
        if (entry.isDirectory()) {
            currSize = 0;
        } else {
            currSize = entry.getSize();
        }
        currName = entry.getName();
    }
    public void closeEntry() throws IOException {
        if (assemLen > 0) {
            for (int i = assemLen; i < assemBuf.length; ++i) {
                assemBuf[i] = 0;
            }
            buffer.writeRecord(assemBuf);
            currBytes += assemLen;
            assemLen = 0;
        }
        if (currBytes < currSize) {
            throw new IOException("entry '" + currName + "' closed at '"
                                  + currBytes
                                  + "' before the '" + currSize
                                  + "' bytes specified in the header were written");
        }
    }
    public void write(int b) throws IOException {
        oneBuf[0] = (byte) b;
        write(oneBuf, 0, 1);
    }
    public void write(byte[] wBuf) throws IOException {
        write(wBuf, 0, wBuf.length);
    }
    public void write(byte[] wBuf, int wOffset, int numToWrite) throws IOException {
        if ((currBytes + numToWrite) > currSize) {
            throw new IOException("request to write '" + numToWrite
                                  + "' bytes exceeds size in header of '"
                                  + currSize + "' bytes for entry '"
                                  + currName + "'");
        }
        if (assemLen > 0) {
            if ((assemLen + numToWrite) >= recordBuf.length) {
                int aLen = recordBuf.length - assemLen;
                System.arraycopy(assemBuf, 0, recordBuf, 0,
                                 assemLen);
                System.arraycopy(wBuf, wOffset, recordBuf,
                                 assemLen, aLen);
                buffer.writeRecord(recordBuf);
                currBytes += recordBuf.length;
                wOffset += aLen;
                numToWrite -= aLen;
                assemLen = 0;
            } else {
                System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                                 numToWrite);
                wOffset += numToWrite;
                assemLen += numToWrite;
                numToWrite = 0;
            }
        }
        while (numToWrite > 0) {
            if (numToWrite < recordBuf.length) {
                System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                                 numToWrite);
                assemLen += numToWrite;
                break;
            }
            buffer.writeRecord(wBuf, wOffset);
            int num = recordBuf.length;
            currBytes += num;
            numToWrite -= num;
            wOffset += num;
        }
    }
    private void writeEOFRecord() throws IOException {
        for (int i = 0; i < recordBuf.length; ++i) {
            recordBuf[i] = 0;
        }
        buffer.writeRecord(recordBuf);
    }
}
