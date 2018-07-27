package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.types.Parameter;
public final class StripLineBreaks
    extends BaseParamFilterReader
    implements ChainableReader {
    private static final String DEFAULT_LINE_BREAKS = "\r\n";
    private static final String LINE_BREAKS_KEY = "linebreaks";
    private String lineBreaks = DEFAULT_LINE_BREAKS;
    public StripLineBreaks() {
        super();
    }
    public StripLineBreaks(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = in.read();
        while (ch != -1) {
            if (lineBreaks.indexOf(ch) == -1) {
                break;
            } else {
                ch = in.read();
            }
        }
        return ch;
    }
    public void setLineBreaks(final String lineBreaks) {
        this.lineBreaks = lineBreaks;
    }
    private String getLineBreaks() {
        return lineBreaks;
    }
    public Reader chain(final Reader rdr) {
        StripLineBreaks newFilter = new StripLineBreaks(rdr);
        newFilter.setLineBreaks(getLineBreaks());
        newFilter.setInitialized(true);
        return newFilter;
    }
    private void initialize() {
        String userDefinedLineBreaks = null;
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (LINE_BREAKS_KEY.equals(params[i].getName())) {
                    userDefinedLineBreaks = params[i].getValue();
                    break;
                }
            }
        }
        if (userDefinedLineBreaks != null) {
            lineBreaks = userDefinedLineBreaks;
        }
    }
}
