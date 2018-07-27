package org.apache.tools.ant.types;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.regexp.Regexp;
import org.apache.tools.ant.util.regexp.RegexpFactory;
public class RegularExpression extends DataType {
    public static final String DATA_TYPE_NAME = "regexp";
    private boolean alreadyInit = false;
    private static final RegexpFactory FACTORY = new RegexpFactory();
    private Regexp regexp = null;
    private String myPattern;
    private boolean setPatternPending = false;
    public RegularExpression() {
    }
    private void init(Project p) {
        if (!alreadyInit) {
            this.regexp = FACTORY.newRegexp(p);
            alreadyInit = true;
        }
    }
    private void setPattern() {
        if (setPatternPending) {
            regexp.setPattern(myPattern);
            setPatternPending = false;
        }
    }
    public void setPattern(String pattern) {
        if (regexp == null) {
            myPattern = pattern;
            setPatternPending = true;
        } else {
            regexp.setPattern(pattern);
        }
    }
    public String getPattern(Project p) {
        init(p);
        if (isReference()) {
            return getRef(p).getPattern(p);
        }
        setPattern();
        return regexp.getPattern();
    }
    public Regexp getRegexp(Project p) {
        init(p);
        if (isReference()) {
            return getRef(p).getRegexp(p);
        }
        setPattern();
        return this.regexp;
    }
    public RegularExpression getRef(Project p) {
        return (RegularExpression) getCheckedRef(p);
    }
}
