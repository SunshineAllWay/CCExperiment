package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.util.LineTokenizer;
public final class TailFilter extends BaseParamFilterReader
    implements ChainableReader {
    private static final String LINES_KEY = "lines";
    private static final String SKIP_KEY = "skip";
    private static final int DEFAULT_NUM_LINES = 10;
    private long lines = DEFAULT_NUM_LINES;
    private long skip = 0;
    private boolean completedReadAhead = false;
    private LineTokenizer lineTokenizer = null;
    private String    line      = null;
    private int       linePos   = 0;
    private LinkedList lineList = new LinkedList();
    public TailFilter() {
        super();
    }
    public TailFilter(final Reader in) {
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
            line = tailFilter(line);
            if (line == null) {
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
        TailFilter newFilter = new TailFilter(rdr);
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
                    setLines(Long.parseLong(params[i].getValue()));
                    continue;
                }
                if (SKIP_KEY.equals(params[i].getName())) {
                    skip = Long.parseLong(params[i].getValue());
                    continue;
                }
            }
        }
    }
    private String tailFilter(String line) {
        if (!completedReadAhead) {
            if (line != null) {
                lineList.add(line);
                if (lines == -1) {
                    if (lineList.size() > skip) {
                        return (String) lineList.removeFirst();
                    }
                } else {
                    long linesToKeep = lines + (skip > 0 ? skip : 0);
                    if (linesToKeep < lineList.size()) {
                        lineList.removeFirst();
                    }
                }
                return "";
            }
            completedReadAhead = true;
            if (skip > 0) {
                for (int i = 0; i < skip; ++i) {
                    lineList.removeLast();
                }
            }
            if (lines > -1) {
                while (lineList.size() > lines) {
                    lineList.removeFirst();
                }
            }
        }
        if (lineList.size() > 0) {
            return (String) lineList.removeFirst();
        }
        return null;
    }
}
