package org.apache.tools.ant.util.regexp;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.apache.tools.ant.BuildException;
public class JakartaOroRegexp extends JakartaOroMatcher implements Regexp {
    private static final int DECIMAL = 10;
    public JakartaOroRegexp() {
        super();
    }
    public String substitute(String input, String argument, int options)
        throws BuildException {
        StringBuffer subst = new StringBuffer();
        for (int i = 0; i < argument.length(); i++) {
            char c = argument.charAt(i);
            if (c == '$') {
                subst.append('\\');
                subst.append('$');
            } else if (c == '\\') {
                if (++i < argument.length()) {
                    c = argument.charAt(i);
                    int value = Character.digit(c, DECIMAL);
                    if (value > -1) {
                        subst.append("$").append(value);
                    } else {
                        subst.append(c);
                    }
                } else {
                    subst.append('\\');
                }
            } else {
                subst.append(c);
            }
        }
        Substitution s =
            new Perl5Substitution(subst.toString(),
                                  Perl5Substitution.INTERPOLATE_ALL);
        return Util.substitute(matcher,
                               getCompiledPattern(options),
                               s,
                               input,
                               getSubsOptions(options));
    }
    protected int getSubsOptions(int options) {
        boolean replaceAll = RegexpUtil.hasFlag(options, REPLACE_ALL);
        int subsOptions = 1;
        if (replaceAll) {
            subsOptions = Util.SUBSTITUTE_ALL;
        }
        return subsOptions;
    }
}