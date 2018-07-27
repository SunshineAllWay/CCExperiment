package socket.io;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
public class WrappedInputStream
    extends FilterInputStream {
    protected int fPacketCount;
    protected DataInputStream fDataInputStream;
    protected boolean fClosed;
    public WrappedInputStream(InputStream stream) {
        super(stream);
        fDataInputStream = new DataInputStream(stream);
    } 
    public int read() throws IOException {
        if (fClosed) {
            return -1;
        }
        if (fPacketCount == 0) {
            fPacketCount = fDataInputStream.readInt() & 0x7FFFFFFF;
            if (fPacketCount == 0) {
                fClosed = true;
                return -1;
            }
        }
        fPacketCount--;
        return super.in.read();
    } 
    public int read(byte[] b, int offset, int length) throws IOException {
        if (fClosed) {
            return -1;
        }
        if (fPacketCount == 0) {
            fPacketCount = fDataInputStream.readInt() & 0x7FFFFFFF;
            if (fPacketCount == 0) {
                fClosed = true;
                return -1;
            }
        }
        if (length > fPacketCount) {
            length = fPacketCount;
        }
        int count = super.in.read(b, offset, length);
        if (count == -1) {
            fClosed = true;
            return -1;
        }
        fPacketCount -= count;
        return count;
    } 
    public long skip(long n) throws IOException {
        if (!fClosed) {
            for (long i = 0; i < n; i++) {
                int b = read();
                if (b == -1) {
                    return i + 1;
                }
            }
            return n;
        }
        return 0;
    } 
    public void close() throws IOException {
        if (!fClosed) {
            fClosed = true;
            do {
                super.in.skip(fPacketCount);
                fPacketCount = fDataInputStream.readInt() & 0x7FFFFFFF;
            } while (fPacketCount > 0);
        }
    } 
} 
