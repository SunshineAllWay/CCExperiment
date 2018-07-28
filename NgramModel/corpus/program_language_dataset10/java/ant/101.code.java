package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.util.UnicodeUtil;
public class EscapeUnicode
    extends BaseParamFilterReader
    implements ChainableReader {
    private StringBuffer unicodeBuf;
    public EscapeUnicode() {
        super();
        unicodeBuf = new StringBuffer();
    }
    public EscapeUnicode(final Reader in) {
        super(in);
        unicodeBuf = new StringBuffer();
    }
    public final int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = -1;
        if (unicodeBuf.length() == 0) {
            ch = in.read();
            if (ch != -1) {
                char achar = (char) ch;
                if (achar >= '\u0080') {
                    unicodeBuf = UnicodeUtil.EscapeUnicode(achar);
                    ch = '\\';
                }
            }
        } else {
            ch = (int) unicodeBuf.charAt(0);
            unicodeBuf.deleteCharAt(0);
        }
        return ch;
    }
    public final Reader chain(final Reader rdr) {
        EscapeUnicode newFilter = new EscapeUnicode(rdr);
        newFilter.setInitialized(true);
        return newFilter;
    }
    private void initialize() {
    }
}
