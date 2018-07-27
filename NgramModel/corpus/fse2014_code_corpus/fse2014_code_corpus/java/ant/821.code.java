package org.apache.tools.ant.util;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
public class UUEncoder {
    protected static final int DEFAULT_MODE = 644;
    private static final int MAX_CHARS_PER_LINE = 45;
    private static final int INPUT_BUFFER_SIZE = MAX_CHARS_PER_LINE * 100;
    private OutputStream out;
    private String name;
    public UUEncoder(String name) {
        this.name = name;
    }
    public void encode(InputStream is, OutputStream out)
        throws IOException {
        this.out = out;
        encodeBegin();
        byte[] buffer = new byte[INPUT_BUFFER_SIZE];
        int count;
        while ((count = is.read(buffer, 0, buffer.length)) != -1) {
            int pos = 0;
            while (count > 0) {
                int num = count > MAX_CHARS_PER_LINE
                    ? MAX_CHARS_PER_LINE
                    : count;
                encodeLine(buffer, pos, num, out);
                pos += num;
                count -= num;
            }
        }
        out.flush();
        encodeEnd();
    }
    private void encodeString(String n) throws IOException {
        PrintStream writer = new PrintStream(out);
        writer.print(n);
        writer.flush();
    }
    private void encodeBegin() throws IOException {
        encodeString("begin " + DEFAULT_MODE + " " + name + "\n");
    }
    private void encodeEnd() throws IOException {
        encodeString(" \nend\n");
    }
    private void encodeLine(
        byte[] data, int offset, int length, OutputStream out)
        throws IOException {
        out.write((byte) ((length & 0x3F) + ' '));
        byte a;
        byte b;
        byte c;
        for (int i = 0; i < length;) {
            b = 1;
            c = 1;
            a = data[offset + i++];
            if (i < length) {
                b = data[offset + i++];
                if (i < length) {
                    c = data[offset + i++];
                }
            }
            byte d1 = (byte) (((a >>> 2) & 0x3F) + ' ');
            byte d2 = (byte) ((((a << 4) & 0x30) | ((b >>> 4) & 0x0F)) + ' ');
            byte d3 = (byte) ((((b << 2) & 0x3C) | ((c >>> 6) & 0x3)) + ' ');
            byte d4 = (byte) ((c & 0x3F) + ' ');
            out.write(d1);
            out.write(d2);
            out.write(d3);
            out.write(d4);
        }
        out.write('\n');
    }
}
