package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
public abstract class AbstractCharDecoder implements CharDecoder {
    protected static final int BUFFER_SIZE = 8192;
    protected InputStream inputStream;
    protected byte[] buffer = new byte[BUFFER_SIZE];
    protected int position;
    protected int count;
    protected AbstractCharDecoder(InputStream is) {
        inputStream = is;
    }
    public void dispose() throws IOException {
        inputStream.close();
        inputStream = null;
    }
    protected void fillBuffer() throws IOException {
        count = inputStream.read(buffer, 0, BUFFER_SIZE);
        position = 0;
    }
    protected void charError(String encoding) throws IOException {
        throw new IOException
            (Messages.formatMessage("invalid.char",
                                    new Object[] { encoding }));
    }
    protected void endOfStreamError(String encoding) throws IOException {
        throw new IOException
            (Messages.formatMessage("end.of.stream",
                                    new Object[] { encoding }));
    }
}
