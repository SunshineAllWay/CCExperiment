package org.apache.tools.ant.util.optional;
import java.security.Permission;
import org.apache.tools.ant.ExitException;
public class NoExitSecurityManager extends SecurityManager {
    public void checkExit(int status) {
        throw new ExitException(status);
    }
    public void checkPermission(Permission perm) {
    }
}
