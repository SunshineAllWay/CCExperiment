package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
public class UTF8Decoder extends AbstractCharDecoder {
    protected static final byte[] UTF8_BYTES = {
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
        3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,
    };
    protected int nextChar = -1;
    public UTF8Decoder(InputStream is) {
        super(is);
    }
    public int readChar() throws IOException {
        if (nextChar != -1) {
            int result = nextChar;
            nextChar = -1;
            return result;
        }
        if (position == count) {
            fillBuffer();
        }
        if (count == -1) {
            return END_OF_STREAM;
        }
        int b1 = buffer[position++] & 0xff;
        switch (UTF8_BYTES[b1]) {
        default:
            charError("UTF-8");
        case 1:
            return b1;
        case 2:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            return ((b1 & 0x1f) << 6) | (buffer[position++] & 0x3f);
        case 3:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b2 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b3 = buffer[position++];
            if ((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80) {
                charError("UTF-8");
            }
            return ((b1 & 0x1f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x1f);
        case 4:
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            b2 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            b3 = buffer[position++];
            if (position == count) {
                fillBuffer();
            }
            if (count == -1) {
                endOfStreamError("UTF-8");
            }
            int b4 = buffer[position++];
            if ((b2 & 0xc0) != 0x80 ||
                (b3 & 0xc0) != 0x80 ||
                (b4 & 0xc0) != 0x80) {
                charError("UTF-8");
            }
            int c = ((b1 & 0x1f) << 18)
                | ((b2 & 0x3f) << 12)
                | ((b3 & 0x1f) << 6)
                | (b4 & 0x1f);
            nextChar = (c - 0x10000) % 0x400 + 0xdc00;
            return (c - 0x10000) / 0x400 + 0xd800;
        }
    }
}
