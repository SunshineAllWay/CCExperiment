package org.apache.tools.ant;
import java.lang.reflect.Method;
import org.apache.tools.ant.dispatch.Dispatchable;
import org.apache.tools.ant.dispatch.DispatchUtils;
public class TaskAdapter extends Task implements TypeAdapter {
    private Object proxy;
    public TaskAdapter() {
    }
    public TaskAdapter(Object proxy) {
        this();
        setProxy(proxy);
    }
    public static void checkTaskClass(final Class taskClass,
                                      final Project project) {
        if (!Dispatchable.class.isAssignableFrom(taskClass)) {
            try {
                final Method executeM = taskClass.getMethod("execute", (Class[]) null);
                if (!Void.TYPE.equals(executeM.getReturnType())) {
                    final String message = "return type of execute() should be "
                        + "void but was \"" + executeM.getReturnType() + "\" in "
                        + taskClass;
                    project.log(message, Project.MSG_WARN);
                }
            } catch (NoSuchMethodException e) {
                final String message = "No public execute() in " + taskClass;
                project.log(message, Project.MSG_ERR);
                throw new BuildException(message);
            } catch (LinkageError e) {
                String message = "Could not load " + taskClass + ": " + e;
                project.log(message, Project.MSG_ERR);
                throw new BuildException(message, e);
            }
        }
    }
    public void checkProxyClass(Class proxyClass) {
        checkTaskClass(proxyClass, getProject());
    }
    public void execute() throws BuildException {
        try {
            Method setLocationM = proxy.getClass().getMethod(
                "setLocation", new Class[] {Location.class});
            if (setLocationM != null) {
                setLocationM.invoke(proxy, new Object[] {getLocation()});
            }
        } catch (NoSuchMethodException e) {
        } catch (Exception ex) {
            log("Error setting location in " + proxy.getClass(),
                Project.MSG_ERR);
            throw new BuildException(ex);
        }
        try {
            Method setProjectM = proxy.getClass().getMethod(
                "setProject", new Class[] {Project.class});
            if (setProjectM != null) {
                setProjectM.invoke(proxy, new Object[] {getProject()});
            }
        } catch (NoSuchMethodException e) {
        } catch (Exception ex) {
            log("Error setting project in " + proxy.getClass(),
                Project.MSG_ERR);
            throw new BuildException(ex);
        }
        try {
            DispatchUtils.execute(proxy);
        } catch (BuildException be) {
            throw be;
        } catch (Exception ex) {
            log("Error in " + proxy.getClass(), Project.MSG_VERBOSE);
            throw new BuildException(ex);
        }
    }
    public void setProxy(Object o) {
        this.proxy = o;
    }
    public Object getProxy() {
        return proxy;
    }
}
