package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
public final class StripJavaComments
    extends BaseFilterReader
    implements ChainableReader {
    private int readAheadCh = -1;
    private boolean inString = false;
    private boolean quoted = false;
    public StripJavaComments() {
        super();
    }
    public StripJavaComments(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        int ch = -1;
        if (readAheadCh != -1) {
            ch = readAheadCh;
            readAheadCh = -1;
        } else {
            ch = in.read();
            if (ch == '"' && !quoted) {
                inString = !inString;
                quoted = false;
            } else if (ch == '\\') {
                quoted = !quoted;
            } else {
                quoted = false;
                if (!inString) {
                    if (ch == '/') {
                        ch = in.read();
                        if (ch == '/') {
                            while (ch != '\n' && ch != -1 && ch != '\r') {
                                ch = in.read();
                            }
                        } else if (ch == '*') {
                            while (ch != -1) {
                                ch = in.read();
                                if (ch == '*') {
                                    ch = in.read();
                                    while (ch == '*') {
                                        ch = in.read();
                                    }
                                    if (ch == '/') {
                                        ch = read();
                                        break;
                                    }
                                }
                            }
                        } else {
                            readAheadCh = ch;
                            ch = '/';
                        }
                    }
                }
            }
        }
        return ch;
    }
    public Reader chain(final Reader rdr) {
        StripJavaComments newFilter = new StripJavaComments(rdr);
        return newFilter;
    }
}
