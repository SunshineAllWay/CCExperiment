package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
public class IsReachable extends ProjectComponent implements Condition {
    private static final int SECOND = 1000; 
    private String host;
    private String url;
    public static final int DEFAULT_TIMEOUT = 30;
    private int timeout = DEFAULT_TIMEOUT;
    public static final String ERROR_NO_HOSTNAME = "No hostname defined";
    public static final String ERROR_BAD_TIMEOUT = "Invalid timeout value";
    private static final String WARN_UNKNOWN_HOST = "Unknown host: ";
    public static final String ERROR_ON_NETWORK = "network error to ";
    public static final String ERROR_BOTH_TARGETS
        = "Both url and host have been specified";
    public static final String MSG_NO_REACHABLE_TEST
        = "cannot do a proper reachability test on this Java version";
    public static final String ERROR_BAD_URL = "Bad URL ";
    public static final String ERROR_NO_HOST_IN_URL = "No hostname in URL ";
    public static final String METHOD_NAME = "isReachable";
    public void setHost(String host) {
        this.host = host;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    private boolean empty(String string) {
        return string == null || string.length() == 0;
    }
    private static Class[] parameterTypes = {Integer.TYPE};
    public boolean eval() throws BuildException {
        if (empty(host) && empty(url)) {
            throw new BuildException(ERROR_NO_HOSTNAME);
        }
        if (timeout < 0) {
            throw new BuildException(ERROR_BAD_TIMEOUT);
        }
        String target = host;
        if (!empty(url)) {
            if (!empty(host)) {
                throw new BuildException(ERROR_BOTH_TARGETS);
            }
            try {
                URL realURL = new URL(url);
                target = realURL.getHost();
                if (empty(target)) {
                    throw new BuildException(ERROR_NO_HOST_IN_URL + url);
                }
            } catch (MalformedURLException e) {
                throw new BuildException(ERROR_BAD_URL + url, e);
            }
        }
        log("Probing host " + target, Project.MSG_VERBOSE);
        InetAddress address;
        try {
            address = InetAddress.getByName(target);
        } catch (UnknownHostException e1) {
            log(WARN_UNKNOWN_HOST + target);
            return false;
        }
        log("Host address = " + address.getHostAddress(),
                Project.MSG_VERBOSE);
        boolean reachable;
        Method reachableMethod = null;
        try {
            reachableMethod = InetAddress.class.getMethod(METHOD_NAME,
                    parameterTypes);
            Object[] params = new Object[1];
            params[0] = new Integer(timeout * SECOND);
            try {
                reachable = ((Boolean) reachableMethod.invoke(address, params))
                        .booleanValue();
            } catch (IllegalAccessException e) {
                throw new BuildException("When calling " + reachableMethod);
            } catch (InvocationTargetException e) {
                Throwable nested = e.getTargetException();
                log(ERROR_ON_NETWORK + target + ": " + nested.toString());
                reachable = false;
            }
        } catch (NoSuchMethodException e) {
            log("Not found: InetAddress." + METHOD_NAME, Project.MSG_VERBOSE);
            log(MSG_NO_REACHABLE_TEST);
            reachable = true;
        }
        log("host is" + (reachable ? "" : " not") + " reachable", Project.MSG_VERBOSE);
        return reachable;
    }
}
