package org.apache.tools.ant.types.selectors;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.util.FileUtils;
public class ContainsSelector extends BaseExtendSelector implements ResourceSelector {
    private String contains = null;
    private boolean casesensitive = true;
    private boolean ignorewhitespace = false;
    public static final String EXPRESSION_KEY = "expression";
    public static final String CONTAINS_KEY = "text";
    public static final String CASE_KEY = "casesensitive";
    public static final String WHITESPACE_KEY = "ignorewhitespace";
    public ContainsSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{containsselector text: ");
        buf.append('"').append(contains).append('"');
        buf.append(" casesensitive: ");
        buf.append(casesensitive ? "true" : "false");
        buf.append(" ignorewhitespace: ");
        buf.append(ignorewhitespace ? "true" : "false");
        buf.append("}");
        return buf.toString();
    }
    public void setText(String contains) {
        this.contains = contains;
    }
    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }
    public void setIgnorewhitespace(boolean ignorewhitespace) {
        this.ignorewhitespace = ignorewhitespace;
    }
    public void setParameters(Parameter[] parameters) {
        super.setParameters(parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String paramname = parameters[i].getName();
                if (CONTAINS_KEY.equalsIgnoreCase(paramname)) {
                    setText(parameters[i].getValue());
                } else if (CASE_KEY.equalsIgnoreCase(paramname)) {
                    setCasesensitive(Project.toBoolean(
                            parameters[i].getValue()));
                } else if (WHITESPACE_KEY.equalsIgnoreCase(paramname)) {
                    setIgnorewhitespace(Project.toBoolean(
                            parameters[i].getValue()));
                } else {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }
    public void verifySettings() {
        if (contains == null) {
            setError("The text attribute is required");
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        return isSelected(new FileResource(file));
    }
    public boolean isSelected(Resource r) {
        validate();
        if (r.isDirectory() || contains.length() == 0) {
            return true;
        }
        String userstr = contains;
        if (!casesensitive) {
            userstr = contains.toLowerCase();
        }
        if (ignorewhitespace) {
            userstr = SelectorUtils.removeWhitespace(userstr);
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(r.getInputStream()));
        } catch (Exception e) {
            throw new BuildException("Could not get InputStream from "
                    + r.toLongString(), e);
        }
        try {
            String teststr = in.readLine();
            while (teststr != null) {
                if (!casesensitive) {
                    teststr = teststr.toLowerCase();
                }
                if (ignorewhitespace) {
                    teststr = SelectorUtils.removeWhitespace(teststr);
                }
                if (teststr.indexOf(userstr) > -1) {
                    return true;
                }
                teststr = in.readLine();
            }
            return false;
        } catch (IOException ioe) {
            throw new BuildException("Could not read " + r.toLongString());
        } finally {
            FileUtils.close(in);
        }
    }
}
