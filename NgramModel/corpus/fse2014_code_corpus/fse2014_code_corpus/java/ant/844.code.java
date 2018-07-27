package org.apache.tools.ant.util.regexp;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.tools.ant.BuildException;
public class Jdk14RegexpMatcher implements RegexpMatcher {
    private String pattern;
    public Jdk14RegexpMatcher() {
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    public String getPattern() {
        return pattern;
    }
    protected Pattern getCompiledPattern(int options)
        throws BuildException {
        int cOptions = getCompilerOptions(options);
        try {
            Pattern p = Pattern.compile(this.pattern, cOptions);
            return p;
        } catch (PatternSyntaxException e) {
            throw new BuildException(e);
        }
    }
    public boolean matches(String argument) throws BuildException {
        return matches(argument, MATCH_DEFAULT);
    }
    public boolean matches(String input, int options)
        throws BuildException {
        try {
            Pattern p = getCompiledPattern(options);
            return p.matcher(input).find();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    public Vector getGroups(String argument) throws BuildException {
        return getGroups(argument, MATCH_DEFAULT);
    }
    public Vector getGroups(String input, int options)
        throws BuildException {
        Pattern p = getCompiledPattern(options);
        Matcher matcher = p.matcher(input);
        if (!matcher.find()) {
            return null;
        }
        Vector v = new Vector();
        int cnt = matcher.groupCount();
        for (int i = 0; i <= cnt; i++) {
            String match = matcher.group(i);
            if (match == null) {
                match = "";
            }
            v.addElement(match);
        }
        return v;
    }
    protected int getCompilerOptions(int options) {
        int cOptions = Pattern.UNIX_LINES;
        if (RegexpUtil.hasFlag(options, MATCH_CASE_INSENSITIVE)) {
            cOptions |= Pattern.CASE_INSENSITIVE;
        }
        if (RegexpUtil.hasFlag(options, MATCH_MULTILINE)) {
            cOptions |= Pattern.MULTILINE;
        }
        if (RegexpUtil.hasFlag(options, MATCH_SINGLELINE)) {
            cOptions |= Pattern.DOTALL;
        }
        return cOptions;
    }
}
