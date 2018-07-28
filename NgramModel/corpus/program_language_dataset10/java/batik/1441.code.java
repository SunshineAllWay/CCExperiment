package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
public class UTF16Decoder extends AbstractCharDecoder {
    protected boolean bigEndian;
    public UTF16Decoder(InputStream is) throws IOException {
        super(is);
        int b1 = is.read();
        if (b1 == -1) {
            endOfStreamError("UTF-16");
        }
        int b2 = is.read();
        if (b2 == -1) {
            endOfStreamError("UTF-16");
        }
        int m = (((b1 & 0xff) << 8) | (b2 & 0xff));
        switch (m) {
        case 0xfeff:
            bigEndian = true;
            break;
        case 0xfffe:
            break;
        default:
            charError("UTF-16");
        }
    }
    public UTF16Decoder(InputStream is, boolean be) {
        super(is);
        bigEndian = be;
    }
    public int readChar() throws IOException {
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            return END_OF_STREAM;
        }
        byte b1 = buffer[position++];
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            endOfStreamError("UTF-16");
        }
        byte b2 = buffer[position++];
        int c = (bigEndian)
            ? (((b1 & 0xff) << 8) | (b2 & 0xff))
            : (((b2 & 0xff) << 8) | (b1 & 0xff));
        if (c == 0xfffe) {
            charError("UTF-16");
        }
        return c;
    }
}
