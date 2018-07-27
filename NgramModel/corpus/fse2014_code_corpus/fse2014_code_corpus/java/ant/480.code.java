package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.util.FileUtils;
public class FailureRecorder extends ProjectComponent implements JUnitResultFormatter, BuildListener {
    public static final String MAGIC_PROPERTY_CLASS_LOCATION
        = "ant.junit.failureCollector";
    public static final String DEFAULT_CLASS_LOCATION
        = System.getProperty("java.io.tmpdir") + "FailedTests";
    private static final String LOG_PREFIX = "    [junit]";
    private static SortedSet failedTests = new TreeSet();
    private BufferedWriter writer;
    private static String locationName;
    private String getLocationName() {
        if (locationName == null) {
            String syspropValue = System.getProperty(MAGIC_PROPERTY_CLASS_LOCATION);
            String antpropValue = getProject().getProperty(MAGIC_PROPERTY_CLASS_LOCATION);
            if (syspropValue != null) {
                locationName = syspropValue;
                verbose("System property '" + MAGIC_PROPERTY_CLASS_LOCATION + "' set, so use "
                        + "its value '" + syspropValue + "' as location for collector class.");
            } else if (antpropValue != null) {
                locationName = antpropValue;
                verbose("Ant property '" + MAGIC_PROPERTY_CLASS_LOCATION + "' set, so use "
                        + "its value '" + antpropValue + "' as location for collector class.");
            } else {
                locationName = DEFAULT_CLASS_LOCATION;
                verbose("System property '" + MAGIC_PROPERTY_CLASS_LOCATION + "' not set, so use "
                        + "value as location for collector class: '"
                        + DEFAULT_CLASS_LOCATION + "'");
            }
            File locationFile = new File(locationName);
            if (!locationFile.isAbsolute()) {
                File f = new File(getProject().getBaseDir(), locationName);
                locationName = f.getAbsolutePath();
                verbose("Location file is relative (" + locationFile + ")"
                        + " use absolute path instead (" + locationName + ")");
            }
        }
        return locationName;
    }
    public void setProject(Project project) {
        super.setProject(project);
        boolean alreadyRegistered = false;
        Vector allListeners = project.getBuildListeners();
        for (int i = 0; i < allListeners.size(); i++) {
            Object listener = allListeners.get(i);
            if (listener instanceof FailureRecorder) {
                alreadyRegistered = true;
                continue;
            }
        }
        if (!alreadyRegistered) {
            verbose("Register FailureRecorder (@" + this.hashCode() + ") as BuildListener");
            project.addBuildListener(this);
        }
    }
    public void endTestSuite(JUnitTest suite) throws BuildException {
    }
    public void addError(Test test, Throwable throwable) {
        failedTests.add(new TestInfos(test));
    }
    public void addFailure(Test test, AssertionFailedError error) {
        failedTests.add(new TestInfos(test));
    }
    public void setOutput(OutputStream out) {
        if (out != System.out) {
            FileUtils.close(out);
        }
    }
    public void setSystemError(String err) {
    }
    public void setSystemOutput(String out) {
    }
    public void startTestSuite(JUnitTest suite) throws BuildException {
    }
    public void endTest(Test test) {
    }
    public void startTest(Test test) {
    }
    private void writeJavaClass() {
        try {
            File sourceFile = new File((getLocationName() + ".java"));
            verbose("Write collector class to '" + sourceFile.getAbsolutePath() + "'");
            if (sourceFile.exists() && !sourceFile.delete()) {
                throw new IOException("could not delete " + sourceFile);
            }
            writer = new BufferedWriter(new FileWriter(sourceFile));
            createClassHeader();
            createSuiteMethod();
            createClassFooter();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.close(writer);
        }
    }
    private void createClassHeader() throws IOException {
        String className = getLocationName().replace('\\', '/');
        if (className.indexOf('/') > -1) {
            className = className.substring(className.lastIndexOf('/') + 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
        writer.write("// generated on: ");
        writer.write(sdf.format(new Date()));
        writer.newLine();
        writer.write("import junit.framework.*;");
        writer.newLine();
        writer.write("public class ");
        writer.write(className);
        writer.write(" extends TestCase {");
        writer.newLine();
        writer.write("    public ");
        writer.write(className);
        writer.write("(String testname) {");
        writer.newLine();
        writer.write("        super(testname);");
        writer.newLine();
        writer.write("    }");
        writer.newLine();
    }
    private void createSuiteMethod() throws IOException {
        writer.write("    public static Test suite() {");
        writer.newLine();
        writer.write("        TestSuite suite = new TestSuite();");
        writer.newLine();
        for (Iterator iter = failedTests.iterator(); iter.hasNext();) {
            TestInfos testInfos = (TestInfos) iter.next();
            writer.write("        suite.addTest(");
            writer.write(String.valueOf(testInfos));
            writer.write(");");
            writer.newLine();
        }
        writer.write("        return suite;");
        writer.newLine();
        writer.write("    }");
        writer.newLine();
    }
    private void createClassFooter() throws IOException {
        writer.write("}");
        writer.newLine();
    }
    public void log(String message) {
        getProject().log(LOG_PREFIX + " " + message, Project.MSG_INFO);
    }
    public void verbose(String message) {
        getProject().log(LOG_PREFIX + " " + message, Project.MSG_VERBOSE);
    }
    public static class TestInfos implements Comparable {
        private final String className;
        private final String methodName;
        public TestInfos(Test test) {
            className = test.getClass().getName();
            String _methodName = test.toString();
            methodName = _methodName.substring(0, _methodName.indexOf('('));
        }
        public String toString() {
            return "new " + className + "(\"" + methodName + "\")";
        }
        public int compareTo(Object other) {
            if (other instanceof TestInfos) {
                TestInfos otherInfos = (TestInfos) other;
                return toString().compareTo(otherInfos.toString());
            } else {
                return -1;
            }
        }
        public boolean equals(Object obj) {
            return obj instanceof TestInfos && toString().equals(obj.toString());
        }
        public int hashCode() {
            return toString().hashCode();
        }
    }
    public void buildFinished(BuildEvent event) {
    }
    public void buildStarted(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
    }
    public void targetFinished(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
        if (!failedTests.isEmpty()) {
            writeJavaClass();
        }
    }
    public void taskStarted(BuildEvent event) {
    }
}
