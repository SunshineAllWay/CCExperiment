package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.util.LineTokenizer;
import org.apache.tools.ant.types.Parameter;
public final class HeadFilter extends BaseParamFilterReader
    implements ChainableReader {
    private static final String LINES_KEY = "lines";
    private static final String SKIP_KEY = "skip";
    private long linesRead = 0;
    private static final int DEFAULT_NUM_LINES = 10;
    private long lines = DEFAULT_NUM_LINES;
    private long skip = 0;
    private LineTokenizer lineTokenizer = null;
    private String    line      = null;
    private int       linePos   = 0;
    private boolean eof;
    public HeadFilter() {
        super();
    }
    public HeadFilter(final Reader in) {
        super(in);
        lineTokenizer = new LineTokenizer();
        lineTokenizer.setIncludeDelims(true);
    }
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        while (line == null || line.length() == 0) {
            line = lineTokenizer.getToken(in);
            if (line == null) {
                return -1;
            }
            line = headFilter(line);
            if (eof) {
                return -1;
            }
            linePos = 0;
        }
        int ch = line.charAt(linePos);
        linePos++;
        if (linePos == line.length()) {
            line = null;
        }
        return ch;
    }
    public void setLines(final long lines) {
        this.lines = lines;
    }
    private long getLines() {
        return lines;
    }
    public void setSkip(final long skip) {
        this.skip = skip;
    }
    private long getSkip() {
        return skip;
    }
    public Reader chain(final Reader rdr) {
        HeadFilter newFilter = new HeadFilter(rdr);
        newFilter.setLines(getLines());
        newFilter.setSkip(getSkip());
        newFilter.setInitialized(true);
        return newFilter;
    }
    private void initialize() {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (LINES_KEY.equals(params[i].getName())) {
                    lines = Long.parseLong(params[i].getValue());
                    continue;
                }
                if (SKIP_KEY.equals(params[i].getName())) {
                    skip = Long.parseLong(params[i].getValue());
                    continue;
                }
            }
        }
    }
    private String headFilter(String line) {
        linesRead++;
        if (skip > 0) {
            if ((linesRead - 1) < skip) {
                return null;
            }
        }
        if (lines > 0) {
            if (linesRead > (lines + skip)) {
                eof = true;
                return null;
            }
        }
        return line;
    }
}
