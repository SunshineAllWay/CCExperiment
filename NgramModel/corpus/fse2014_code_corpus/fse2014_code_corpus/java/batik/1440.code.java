package org.apache.batik.util.io;
import java.io.IOException;
public class StringNormalizingReader extends NormalizingReader {
    protected String string;
    protected int length;
    protected int next;
    protected int line = 1;
    protected int column;
    public StringNormalizingReader(String s) {
        string = s;
        length = s.length();
    }
    public int read() throws IOException {
        int result = (length == next) ? -1 : string.charAt(next++);
        if (result <= 13) {
            switch (result) {
            case 13:
                column = 0;
                line++;
                int c = (length == next) ? -1 : string.charAt(next);
                if (c == 10) {
                    next++;
                }
                return 10;
            case 10:
                column = 0;
                line++;
            }
        }
        return result;
    }
    public int getLine() {
        return line;
    }
    public int getColumn() {
        return column;
    }
    public void close() throws IOException {
        string = null;
    }
}
