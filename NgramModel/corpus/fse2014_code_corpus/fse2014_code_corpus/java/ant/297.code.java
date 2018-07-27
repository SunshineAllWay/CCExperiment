package org.apache.tools.ant.taskdefs.compilers;
import java.lang.reflect.Method;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
public class Javac13 extends DefaultCompilerAdapter {
    private static final int MODERN_COMPILER_SUCCESS = 0;
    public boolean execute() throws BuildException {
        attributes.log("Using modern compiler", Project.MSG_VERBOSE);
        Commandline cmd = setupModernJavacCommand();
        try {
            Class c = Class.forName ("com.sun.tools.javac.Main");
            Object compiler = c.newInstance ();
            Method compile = c.getMethod ("compile",
                new Class [] {(new String [] {}).getClass ()});
            int result = ((Integer) compile.invoke
                          (compiler, new Object[] {cmd.getArguments()}))
                .intValue ();
            return (result == MODERN_COMPILER_SUCCESS);
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            } else {
                throw new BuildException("Error starting modern compiler",
                                         ex, location);
            }
        }
    }
}
