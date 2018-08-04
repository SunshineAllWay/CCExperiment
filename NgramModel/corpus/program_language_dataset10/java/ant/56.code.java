package org.apache.tools.ant;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.WeakHashMap;
public class DemuxOutputStream extends OutputStream {
    private static class BufferInfo {
        private ByteArrayOutputStream buffer;
         private boolean crSeen = false;
    }
    private static final int MAX_SIZE = 1024;
    private static final int INTIAL_SIZE = 132;
    private static final int CR = 0x0d;
    private static final int LF = 0x0a;
    private WeakHashMap buffers = new WeakHashMap();
    private Project project;
    private boolean isErrorStream;
    public DemuxOutputStream(Project project, boolean isErrorStream) {
        this.project = project;
        this.isErrorStream = isErrorStream;
    }
    private BufferInfo getBufferInfo() {
        Thread current = Thread.currentThread();
        BufferInfo bufferInfo = (BufferInfo) buffers.get(current);
        if (bufferInfo == null) {
            bufferInfo = new BufferInfo();
            bufferInfo.buffer = new ByteArrayOutputStream(INTIAL_SIZE);
            bufferInfo.crSeen = false;
            buffers.put(current, bufferInfo);
        }
        return bufferInfo;
    }
    private void resetBufferInfo() {
        Thread current = Thread.currentThread();
        BufferInfo bufferInfo = (BufferInfo) buffers.get(current);
        try {
            bufferInfo.buffer.close();
        } catch (IOException e) {
        }
        bufferInfo.buffer = new ByteArrayOutputStream();
        bufferInfo.crSeen = false;
    }
    private void removeBuffer() {
        Thread current = Thread.currentThread();
        buffers.remove (current);
    }
    public void write(int cc) throws IOException {
        final byte c = (byte) cc;
        BufferInfo bufferInfo = getBufferInfo();
        if (c == '\n') {
            bufferInfo.buffer.write(cc);
            processBuffer(bufferInfo.buffer);
        } else {
            if (bufferInfo.crSeen) {
                processBuffer(bufferInfo.buffer);
            }
            bufferInfo.buffer.write(cc);
        }
        bufferInfo.crSeen = (c == '\r');
        if (!bufferInfo.crSeen && bufferInfo.buffer.size() > MAX_SIZE) {
            processBuffer(bufferInfo.buffer);
        }
    }
    protected void processBuffer(ByteArrayOutputStream buffer) {
        String output = buffer.toString();
        project.demuxOutput(output, isErrorStream);
        resetBufferInfo();
    }
    protected void processFlush(ByteArrayOutputStream buffer) {
        String output = buffer.toString();
        project.demuxFlush(output, isErrorStream);
        resetBufferInfo();
    }
    public void close() throws IOException {
        flush();
        removeBuffer();
    }
    public void flush() throws IOException {
        BufferInfo bufferInfo = getBufferInfo();
        if (bufferInfo.buffer.size() > 0) {
            processFlush(bufferInfo.buffer);
        }
    }
    public void write(byte[] b, int off, int len) throws IOException {
        int offset = off;
        int blockStartOffset = offset;
        int remaining = len;
        BufferInfo bufferInfo = getBufferInfo();
        while (remaining > 0) {
            while (remaining > 0 && b[offset] != LF && b[offset] != CR) {
                offset++;
                remaining--;
            }
            int blockLength = offset - blockStartOffset;
            if (blockLength > 0) {
                bufferInfo.buffer.write(b, blockStartOffset, blockLength);
            }
            while (remaining > 0 && (b[offset] == LF || b[offset] == CR)) {
                write(b[offset]);
                offset++;
                remaining--;
            }
            blockStartOffset = offset;
        }
    }
}