package org.apache.batik.util;
import java.io.IOException;
import java.io.InputStream;
public class Base64DecodeStream extends InputStream {
    InputStream src;
    public Base64DecodeStream(InputStream src) {
        this.src = src;
    }
    private static final byte[] pem_array = new byte[256];
    static {
        for (int i=0; i<pem_array.length; i++)
            pem_array[i] = -1;
        int idx = 0;
        for (char c='A'; c<='Z'; c++) {
            pem_array[c] = (byte)idx++;
        }
        for (char c='a'; c<='z'; c++) {
            pem_array[c] = (byte)idx++;
        }
        for (char c='0'; c<='9'; c++) {
            pem_array[c] = (byte)idx++;
        }
        pem_array['+'] = (byte)idx++;
        pem_array['/'] = (byte)idx++;
    }
    public boolean markSupported() { return false; }
    public void close()
        throws IOException {
        EOF = true;
    }
    public int available()
        throws IOException {
        return 3-out_offset;
    }
    byte[] decode_buffer = new byte[4];
    byte[] out_buffer = new byte[3];
    int  out_offset = 3;
    boolean EOF = false;
    public int read() throws IOException {
        if (out_offset == 3) {
            if (EOF || getNextAtom()) {
                EOF = true;
                return -1;
            }
        }
        return ((int)out_buffer[out_offset++])&0xFF;
    }
    public int read(byte []out, int offset, int len)
        throws IOException {
        int idx = 0;
        while (idx < len) {
            if (out_offset == 3) {
                if (EOF || getNextAtom()) {
                    EOF = true;
                    if (idx == 0) return -1;
                    else          return idx;
                }
            }
            out[offset+idx] = out_buffer[out_offset++];
            idx++;
        }
        return idx;
    }
    final boolean getNextAtom() throws IOException {
        int count, a, b, c, d;
        int off = 0;
        while(off != 4) {
            count = src.read(decode_buffer, off, 4-off);
            if (count == -1)
                return true;
            int in=off, out=off;
            while(in < off+count) {
                if ((decode_buffer[in] != '\n') &&
                    (decode_buffer[in] != '\r') &&
                    (decode_buffer[in] != ' '))
                    decode_buffer[out++] = decode_buffer[in];
                in++;
            }
            off = out;
        }
        a = pem_array[((int)decode_buffer[0])&0xFF];
        b = pem_array[((int)decode_buffer[1])&0xFF];
        c = pem_array[((int)decode_buffer[2])&0xFF];
        d = pem_array[((int)decode_buffer[3])&0xFF];
        out_buffer[0] = (byte)((a<<2) | (b>>>4));
        out_buffer[1] = (byte)((b<<4) | (c>>>2));
        out_buffer[2] = (byte)((c<<6) |  d     );
        if (decode_buffer[3] != '=') {
            out_offset=0;
        } else if (decode_buffer[2] == '=') {
            out_buffer[2] = out_buffer[0];
            out_offset = 2;
            EOF=true;
        } else {
            out_buffer[2] = out_buffer[1];
            out_buffer[1] = out_buffer[0];
            out_offset = 1;
            EOF=true;
        }
        return false;
    }
}
