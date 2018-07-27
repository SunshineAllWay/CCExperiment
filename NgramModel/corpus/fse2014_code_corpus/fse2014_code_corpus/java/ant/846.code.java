package org.apache.tools.ant.util.regexp;
import org.apache.tools.ant.BuildException;
public interface Regexp extends RegexpMatcher {
    int REPLACE_FIRST          = 0x00000001;
    int REPLACE_ALL            = 0x00000010;
    String substitute(String input, String argument, int options)
        throws BuildException;
}
