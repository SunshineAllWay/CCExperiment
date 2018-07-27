package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
public class ISO_8859_1Decoder extends AbstractCharDecoder {
    public ISO_8859_1Decoder(InputStream is) {
        super(is);
    }
    public int readChar() throws IOException {
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            return -1;
        }
        return buffer[position++] & 0xff;
    }
}
