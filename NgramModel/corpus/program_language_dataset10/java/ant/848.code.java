package org.apache.tools.ant.util.regexp;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
public interface RegexpMatcher {
    int MATCH_DEFAULT          = 0x00000000;
    int MATCH_CASE_INSENSITIVE = 0x00000100;
    int MATCH_MULTILINE        = 0x00001000;
    int MATCH_SINGLELINE       = 0x00010000;
    void setPattern(String pattern) throws BuildException;
    String getPattern() throws BuildException;
    boolean matches(String argument) throws BuildException;
    Vector getGroups(String argument) throws BuildException;
    boolean matches(String input, int options) throws BuildException;
    Vector getGroups(String input, int options) throws BuildException;
}