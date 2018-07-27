package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.util.regexp.Regexp;
import org.apache.tools.ant.util.regexp.RegexpUtil;
public class FilenameSelector extends BaseExtendSelector {
    private String pattern = null;
    private String regex = null;
    private boolean casesensitive = true;
    private boolean negated = false;
    public static final String NAME_KEY = "name";
    public static final String CASE_KEY = "casesensitive";
    public static final String NEGATE_KEY = "negate";
    public static final String REGEX_KEY = "regex";
    private RegularExpression reg;
    private Regexp expression;
    public FilenameSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{filenameselector name: ");
        if (pattern != null) {
            buf.append(pattern);
        }
        if (regex != null) {
            buf.append(regex).append(" [as regular expression]");
        }
        buf.append(" negate: ").append(negated);
        buf.append(" casesensitive: ").append(casesensitive);
        buf.append("}");
        return buf.toString();
    }
    public void setName(String pattern) {
        pattern = pattern.replace('/', File.separatorChar).replace('\\',
                File.separatorChar);
        if (pattern.endsWith(File.separator)) {
            pattern += "**";
        }
        this.pattern = pattern;
    }
    public void setRegex(String pattern) {
        this.regex = pattern;
        this.reg = null;
    }
    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }
    public void setNegate(boolean negated) {
        this.negated = negated;
    }
    public void setParameters(Parameter[] parameters) {
        super.setParameters(parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String paramname = parameters[i].getName();
                if (NAME_KEY.equalsIgnoreCase(paramname)) {
                    setName(parameters[i].getValue());
                } else if (CASE_KEY.equalsIgnoreCase(paramname)) {
                    setCasesensitive(Project.toBoolean(
                            parameters[i].getValue()));
                } else if (NEGATE_KEY.equalsIgnoreCase(paramname)) {
                    setNegate(Project.toBoolean(parameters[i].getValue()));
                } else if (REGEX_KEY.equalsIgnoreCase(paramname)) {
                    setRegex(parameters[i].getValue());
                } else {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }
    public void verifySettings() {
        if (pattern == null && regex == null) {
            setError("The name or regex attribute is required");
        } else if (pattern != null && regex != null) {
            setError("Only one of name and regex attribute is allowed");
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        if (pattern != null) {
            return (SelectorUtils.matchPath(pattern, filename,
                                            casesensitive) == !(negated));
        } else {
            if (reg == null) {
                reg = new RegularExpression();
                reg.setPattern(regex);
                expression = reg.getRegexp(getProject());
            }
            int options = RegexpUtil.asOptions(casesensitive);
            return expression.matches(filename, options) == !negated;
        }
    }
}
