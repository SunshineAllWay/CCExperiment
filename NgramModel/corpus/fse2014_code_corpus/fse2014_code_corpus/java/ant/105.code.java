package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
public final class LineContains
    extends BaseParamFilterReader
    implements ChainableReader {
    private static final String CONTAINS_KEY = "contains";
    private static final String NEGATE_KEY = "negate";
    private Vector contains = new Vector();
    private String line = null;
    private boolean negate = false;
    public LineContains() {
        super();
    }
    public LineContains(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = -1;
        if (line != null) {
            ch = line.charAt(0);
            if (line.length() == 1) {
                line = null;
            } else {
                line = line.substring(1);
            }
        } else {
            final int containsSize = contains.size();
            for (line = readLine(); line != null; line = readLine()) {
                boolean matches = true;
                for (int i = 0; matches && i < containsSize; i++) {
                    String containsStr = (String) contains.elementAt(i);
                    matches = line.indexOf(containsStr) >= 0;
                }
                if (matches ^ isNegated()) {
                    break;
                }
            }
            if (line != null) {
                return read();
            }
        }
        return ch;
    }
    public void addConfiguredContains(final Contains contains) {
        this.contains.addElement(contains.getValue());
    }
    public void setNegate(boolean b) {
        negate = b;
    }
    public boolean isNegated() {
        return negate;
    }
    private void setContains(final Vector contains) {
        this.contains = contains;
    }
    private Vector getContains() {
        return contains;
    }
    public Reader chain(final Reader rdr) {
        LineContains newFilter = new LineContains(rdr);
        newFilter.setContains(getContains());
        newFilter.setNegate(isNegated());
        return newFilter;
    }
    private void initialize() {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (CONTAINS_KEY.equals(params[i].getType())) {
                    contains.addElement(params[i].getValue());
                } else if (NEGATE_KEY.equals(params[i].getType())) {
                    setNegate(Project.toBoolean(params[i].getValue()));
                }
            }
        }
    }
    public static class Contains {
        private String value;
        public final void setValue(String contains) {
            value = contains;
        }
        public final String getValue() {
            return value;
        }
    }
}
