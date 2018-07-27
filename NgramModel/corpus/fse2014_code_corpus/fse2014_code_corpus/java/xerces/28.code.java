package socket.io;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public class WrappedOutputStream
    extends FilterOutputStream {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    protected byte[] fBuffer;
    protected int fPosition;
    protected DataOutputStream fDataOutputStream;
    public WrappedOutputStream(OutputStream stream) {
        this(stream, DEFAULT_BUFFER_SIZE);
    } 
    public WrappedOutputStream(OutputStream stream, int bufferSize) {
        super(stream);
        fBuffer = new byte[bufferSize];
        fDataOutputStream = new DataOutputStream(stream);
    } 
    public void write(int b) throws IOException {
        fBuffer[fPosition++] = (byte)b;
        if (fPosition == fBuffer.length) {
            fPosition = 0;
            fDataOutputStream.writeInt(fBuffer.length);
            super.out.write(fBuffer, 0, fBuffer.length);
        }
    } 
    public void write(byte[] b, int offset, int length) 
        throws IOException {
        if (fPosition > 0) {
            flush0();
        }
        fDataOutputStream.writeInt(length);
        super.out.write(b, offset, length);
    } 
    public void flush() throws IOException {
        flush0();
        super.out.flush();
    } 
    public void close() throws IOException {
        flush0();
        fDataOutputStream.writeInt(0);
        super.out.flush();
    } 
    public void flush0() throws IOException {
        int length = fPosition;
        fPosition = 0;
        if (length > 0) {
            fDataOutputStream.writeInt(length);
            super.out.write(fBuffer, 0, length);
        }
    } 
} 
