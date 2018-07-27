package org.apache.tools.ant.taskdefs.optional.junit;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
public class JUnitTest extends BaseTest implements Cloneable {
    private String name = null;
    private boolean methodsSpecified = false;
    private String methodsList = null;
    private String[] methods = null;
    private String outfile = null;
    private long runs, failures, errors;
    private long runTime;
    private Properties props = null;
    public JUnitTest() {
    }
    public JUnitTest(String name) {
        this.name  = name;
    }
    public JUnitTest(String name, boolean haltOnError, boolean haltOnFailure,
            boolean filtertrace) {
        this(name, haltOnError, haltOnFailure, filtertrace, null);
    }    
    public JUnitTest(String name, boolean haltOnError, boolean haltOnFailure,
                     boolean filtertrace, String[] methods) {
        this.name  = name;
        this.haltOnError = haltOnError;
        this.haltOnFail = haltOnFailure;
        this.filtertrace = filtertrace;
        this.methodsSpecified = methods != null;
        this.methods = methodsSpecified ? (String[]) methods.clone() : null;
    }
    public void setMethods(String value) {
        methodsList = value;
        methodsSpecified = (value != null);
        methods = null;
    }
    void setMethods(String[] value) {
        methods = value;
        methodsSpecified = (value != null);
        methodsList = null;
    }
    public void setName(String value) {
        name = value;
    }
    public void setOutfile(String value) {
        outfile = value;
    }
    boolean hasMethodsSpecified() {
        return methodsSpecified;
    }
    String[] getMethods() {
        if (methodsSpecified && (methods == null)) {
            resolveMethods();
        }
        return methods;
    }
    String getMethodsString() {
        if ((methodsList == null) && methodsSpecified) {
            if (methods.length == 0) {
                methodsList = "";
            } else if (methods.length == 1) {
                methodsList = methods[0];
            } else {
                StringBuffer buf = new StringBuffer(methods.length * 16);
                buf.append(methods[0]);
                for (int i = 1; i < methods.length; i++) {
                    buf.append(',').append(methods[i]);
                }
                methodsList = buf.toString();
            }
        }
        return methodsList;
    }
    void resolveMethods() {
        if ((methods == null) && methodsSpecified) {
            try {
                methods = parseTestMethodNamesList(methodsList);
            } catch (IllegalArgumentException ex) {
                throw new BuildException(
                        "Invalid specification of test methods: \""
                            + methodsList
                            + "\"; expected: comma-separated list of valid Java identifiers",
                        ex);
            }
        }
    }
    public static String[] parseTestMethodNamesList(String methodNames)
                                            throws IllegalArgumentException {
        if (methodNames == null) {
            throw new IllegalArgumentException("methodNames is <null>");
        }
        methodNames = methodNames.trim();
        int length = methodNames.length();
        if (length == 0) {
            return new String[0];
        }
        if (methodNames.charAt(length - 1) == ',') {
            methodNames = methodNames.substring(0, length - 1).trim();
            length = methodNames.length();
            if (length == 0) {
                throw new IllegalArgumentException("Empty method name");
            }
        }
        final char[] chars = methodNames.toCharArray();
        if (chars[0] == ',') {
            throw new IllegalArgumentException("Empty method name");
        }
        int wordCount = 1;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == ',') {
                wordCount++;
            }
        }
        String[] result = new String[wordCount];
        final int stateBeforeWord = 1;
        final int stateInsideWord = 2;
        final int stateAfterWord = 3;
        int state = stateBeforeWord;
        int wordStartIndex = -1;
        int wordIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {
                case stateBeforeWord:
                    if (c == ',') {
                        throw new IllegalArgumentException("Empty method name");
                    } else if (c == ' ') {
                    } else if (Character.isJavaIdentifierStart(c)) {
                        wordStartIndex = i;
                        state = stateInsideWord;
                    } else {
                        throw new IllegalArgumentException("Illegal start of method name: " + c);
                    }
                    break;
                case stateInsideWord:
                    if (c == ',') {
                        result[wordIndex++] = methodNames.substring(wordStartIndex, i);
                        state = stateBeforeWord;
                    } else if (c == ' ') {
                        result[wordIndex++] = methodNames.substring(wordStartIndex, i);
                        state = stateAfterWord;
                    } else if (Character.isJavaIdentifierPart(c)) {
                    } else {
                        throw new IllegalArgumentException("Illegal character in method name: " + c);
                    }
                    break;
                case stateAfterWord:
                    if (c == ',') {
                        state = stateBeforeWord;
                    } else if (c == ' ') {
                    } else {
                        throw new IllegalArgumentException("Space in method name");
                    }
                    break;
                default:
            }
        }
        switch (state) {
            case stateBeforeWord:
            case stateAfterWord:
                break;
            case stateInsideWord:
                result[wordIndex++] = methodNames.substring(wordStartIndex, chars.length);
                break;
            default:
        }
        return result;
    }
    public String getName() {
        return name;
    }
    public String getOutfile() {
        return outfile;
    }
    public void setCounts(long runs, long failures, long errors) {
        this.runs = runs;
        this.failures = failures;
        this.errors = errors;
    }
    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }
    public long runCount() {
        return runs;
    }
    public long failureCount() {
        return failures;
    }
    public long errorCount() {
        return errors;
    }
    public long getRunTime() {
        return runTime;
    }
    public Properties getProperties() {
        return props;
    }
    public void setProperties(Hashtable p) {
        props = new Properties();
        for (Enumeration e = p.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            props.put(key, p.get(key));
        }
    }
    public boolean shouldRun(Project p) {
        PropertyHelper ph = PropertyHelper.getPropertyHelper(p);
        return ph.testIfCondition(getIfCondition())
            && ph.testUnlessCondition(getUnlessCondition());
    }
    public FormatterElement[] getFormatters() {
        FormatterElement[] fes = new FormatterElement[formatters.size()];
        formatters.copyInto(fes);
        return fes;
    }
    void addFormattersTo(Vector v) {
        final int count = formatters.size();
        for (int i = 0; i < count; i++) {
            v.addElement(formatters.elementAt(i));
        }
    }
    public Object clone() {
        try {
            JUnitTest t = (JUnitTest) super.clone();
            t.props = props == null ? null : (Properties) props.clone();
            t.formatters = (Vector) formatters.clone();
            return t;
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }
}
