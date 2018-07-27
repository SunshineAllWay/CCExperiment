package org.apache.tools.ant.taskdefs.rmic;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
public class SunRmic extends DefaultRmicAdapter {
    public static final String RMIC_CLASSNAME = "sun.rmi.rmic.Main";
    public static final String COMPILER_NAME = "sun";
    public static final String RMIC_EXECUTABLE = "rmic";
    public static final String ERROR_NO_RMIC_ON_CLASSPATH = "Cannot use SUN rmic, as it is not "
                                         + "available.  A common solution is to "
                                         + "set the environment variable "
                                         + "JAVA_HOME";
    public static final String ERROR_RMIC_FAILED = "Error starting SUN rmic: ";
    public boolean execute() throws BuildException {
        getRmic().log("Using SUN rmic compiler", Project.MSG_VERBOSE);
        Commandline cmd = setupRmicCommand();
        LogOutputStream logstr = new LogOutputStream(getRmic(),
                                                     Project.MSG_WARN);
        try {
            Class c = Class.forName(RMIC_CLASSNAME);
            Constructor cons
                = c.getConstructor(new Class[]  {OutputStream.class, String.class});
            Object rmic = cons.newInstance(new Object[] {logstr, "rmic"});
            Method doRmic = c.getMethod("compile",
                                        new Class [] {String[].class});
            Boolean ok =
                (Boolean) doRmic.invoke(rmic,
                                       (new Object[] {cmd.getArguments()}));
            return ok.booleanValue();
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ERROR_NO_RMIC_ON_CLASSPATH,
                                     getRmic().getLocation());
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            } else {
                throw new BuildException(ERROR_RMIC_FAILED,
                                         ex, getRmic().getLocation());
            }
        } finally {
            try {
                logstr.close();
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }
    protected String[] preprocessCompilerArgs(String[] compilerArgs) {
        return filterJvmCompilerArgs(compilerArgs);
    }
}
