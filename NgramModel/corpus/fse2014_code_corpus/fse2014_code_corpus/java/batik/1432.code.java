package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
public class ASCIIDecoder extends AbstractCharDecoder {
    public ASCIIDecoder(InputStream is) {
        super(is);
    }
    public int readChar() throws IOException {
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            return END_OF_STREAM;
        }
        int result = buffer[position++];
        if (result < 0) {
            charError("ASCII");
        }
        return result;
    }
}
