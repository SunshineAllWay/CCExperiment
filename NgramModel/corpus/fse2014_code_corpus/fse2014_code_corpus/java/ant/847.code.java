package org.apache.tools.ant.util.regexp;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.ClasspathUtils;
public class RegexpFactory extends RegexpMatcherFactory {
    public RegexpFactory() {
    }
    public Regexp newRegexp() throws BuildException {
        return newRegexp(null);
    }
    public Regexp newRegexp(Project p) throws BuildException {
        String systemDefault = null;
        if (p == null) {
            systemDefault = System.getProperty(MagicNames.REGEXP_IMPL);
        } else {
            systemDefault = p.getProperty(MagicNames.REGEXP_IMPL);
        }
        if (systemDefault != null) {
            return createRegexpInstance(systemDefault);
        }
        return new Jdk14RegexpRegexp();
    }
    protected Regexp createRegexpInstance(String classname) throws BuildException {
        return (Regexp) ClasspathUtils.newInstance(classname, RegexpFactory.class.getClassLoader(),
                Regexp.class);
    }
}
