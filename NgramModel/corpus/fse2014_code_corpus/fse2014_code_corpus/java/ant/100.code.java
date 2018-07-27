package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.tools.ant.types.Parameter;
public final class ConcatFilter extends BaseParamFilterReader
    implements ChainableReader {
    private File prepend;
    private File append;
    private Reader prependReader = null;
    private Reader appendReader = null;
    public ConcatFilter() {
        super();
    }
    public ConcatFilter(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = -1;
        if (prependReader != null) {
            ch = prependReader.read();
            if (ch == -1) {
                prependReader.close();
                prependReader = null;
            }
        }
        if (ch == -1) {
            ch = super.read();
        }
        if (ch == -1) {
            if (appendReader != null) {
                ch = appendReader.read();
                if (ch == -1) {
                    appendReader.close();
                    appendReader = null;
                }
            }
        }
        return ch;
    }
    public void setPrepend(final File prepend) {
        this.prepend = prepend;
    }
    public File getPrepend() {
        return prepend;
    }
    public void setAppend(final File append) {
        this.append = append;
    }
    public File getAppend() {
        return append;
    }
    public Reader chain(final Reader rdr) {
        ConcatFilter newFilter = new ConcatFilter(rdr);
        newFilter.setPrepend(getPrepend());
        newFilter.setAppend(getAppend());
        return newFilter;
    }
    private void initialize() throws IOException {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if ("prepend".equals(params[i].getName())) {
                    setPrepend(new File(params[i].getValue()));
                    continue;
                }
                if ("append".equals(params[i].getName())) {
                    setAppend(new File(params[i].getValue()));
                    continue;
                }
            }
        }
        if (prepend != null) {
            if (!prepend.isAbsolute()) {
                prepend = new File(getProject().getBaseDir(), prepend.getPath());
            }
            prependReader = new BufferedReader(new FileReader(prepend));
        }
        if (append != null) {
            if (!append.isAbsolute()) {
                append = new File(getProject().getBaseDir(), append.getPath());
            }
            appendReader = new BufferedReader(new FileReader(append));
        }
   }
}
