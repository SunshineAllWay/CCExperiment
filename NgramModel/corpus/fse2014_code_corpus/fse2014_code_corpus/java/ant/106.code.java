package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.util.regexp.Regexp;
import org.apache.tools.ant.util.regexp.RegexpUtil;
public final class LineContainsRegExp
    extends BaseParamFilterReader
    implements ChainableReader {
    private static final String REGEXP_KEY = "regexp";
    private static final String NEGATE_KEY = "negate";
    private static final String CS_KEY = "casesensitive";
    private Vector regexps = new Vector();
    private String line = null;
    private boolean negate = false;
    private int regexpOptions = Regexp.MATCH_DEFAULT;
    public LineContainsRegExp() {
        super();
    }
    public LineContainsRegExp(final Reader in) {
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
            final int regexpsSize = regexps.size();
            for (line = readLine(); line != null; line = readLine()) {
                boolean matches = true;
                for (int i = 0; matches && i < regexpsSize; i++) {
                    RegularExpression regexp
                        = (RegularExpression) regexps.elementAt(i);
                    Regexp re = regexp.getRegexp(getProject());
                    matches = re.matches(line, regexpOptions);
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
    public void addConfiguredRegexp(final RegularExpression regExp) {
        this.regexps.addElement(regExp);
    }
    private void setRegexps(final Vector regexps) {
        this.regexps = regexps;
    }
    private Vector getRegexps() {
        return regexps;
    }
    public Reader chain(final Reader rdr) {
        LineContainsRegExp newFilter = new LineContainsRegExp(rdr);
        newFilter.setRegexps(getRegexps());
        newFilter.setNegate(isNegated());
        newFilter
            .setCaseSensitive(!RegexpUtil.hasFlag(regexpOptions,
                                                  Regexp.MATCH_CASE_INSENSITIVE)
                              );
        return newFilter;
    }
    public void setNegate(boolean b) {
        negate = b;
    }
    public void setCaseSensitive(boolean b) {
        regexpOptions = RegexpUtil.asOptions(b);
    }
    public boolean isNegated() {
        return negate;
    }
    private void initialize() {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (REGEXP_KEY.equals(params[i].getType())) {
                    String pattern = params[i].getValue();
                    RegularExpression regexp = new RegularExpression();
                    regexp.setPattern(pattern);
                    regexps.addElement(regexp);
                } else if (NEGATE_KEY.equals(params[i].getType())) {
                    setNegate(Project.toBoolean(params[i].getValue()));
                } else if (CS_KEY.equals(params[i].getType())) {
                    setCaseSensitive(Project.toBoolean(params[i].getValue()));
                }
            }
        }
    }
}
