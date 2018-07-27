package org.apache.tools.ant.taskdefs.rmic;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.ClasspathUtils;
public final class RmicAdapterFactory {
    public static final String ERROR_UNKNOWN_COMPILER = "Class not found: ";
    public static final String ERROR_NOT_RMIC_ADAPTER = "Class of unexpected Type: ";
    public static final String DEFAULT_COMPILER = "default";
    private RmicAdapterFactory() {
    }
    public static RmicAdapter getRmic(String rmicType, Task task)
        throws BuildException {
        return getRmic(rmicType, task, null);
    }
    public static RmicAdapter getRmic(String rmicType, Task task,
                                      Path classpath)
        throws BuildException {
        if (DEFAULT_COMPILER.equalsIgnoreCase(rmicType) || rmicType.length() == 0) {
            rmicType = KaffeRmic.isAvailable()
                ? KaffeRmic.COMPILER_NAME
                : SunRmic.COMPILER_NAME;
        }
        if (SunRmic.COMPILER_NAME.equalsIgnoreCase(rmicType)) {
            return new SunRmic();
        } else if (KaffeRmic.COMPILER_NAME.equalsIgnoreCase(rmicType)) {
            return new KaffeRmic();
        } else if (WLRmic.COMPILER_NAME.equalsIgnoreCase(rmicType)) {
            return new WLRmic();
        } else if (ForkingSunRmic.COMPILER_NAME.equalsIgnoreCase(rmicType)) {
            return new ForkingSunRmic();
        } else if (XNewRmic.COMPILER_NAME.equalsIgnoreCase(rmicType)) {
            return new XNewRmic();
        }
        return resolveClassName(rmicType,
                                task.getProject().createClassLoader(classpath));
    }
    private static RmicAdapter resolveClassName(String className,
                                                ClassLoader loader)
            throws BuildException {
        return (RmicAdapter) ClasspathUtils.newInstance(className,
                loader != null ? loader :
                RmicAdapterFactory.class.getClassLoader(), RmicAdapter.class);
    }
}
