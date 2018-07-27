package org.apache.batik.util.io;
import java.io.IOException;
public class StringDecoder implements CharDecoder {
    protected String string;
    protected int length;
    protected int next;
    public StringDecoder(String s) {
        string = s;
        length = s.length();
    }
    public int readChar() throws IOException {
        if (next == length) {
            return END_OF_STREAM;
        }
        return string.charAt(next++);
    }
    public void dispose() throws IOException {
        string = null;
    }
}
