package org.apache.tools.ant.taskdefs.condition;
import java.util.Locale;
import org.apache.tools.ant.BuildException;
public class Os implements Condition {
    private static final String OS_NAME =
        System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String OS_ARCH =
        System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
    private static final String OS_VERSION =
        System.getProperty("os.version").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP =
        System.getProperty("path.separator");
    private String family;
    private String name;
    private String version;
    private String arch;
    public static final String FAMILY_WINDOWS = "windows";
    public static final String FAMILY_9X = "win9x";
    public static final String FAMILY_NT = "winnt";
    public static final String FAMILY_OS2 = "os/2";
    public static final String FAMILY_NETWARE = "netware";
    public static final String FAMILY_DOS = "dos";
    public static final String FAMILY_MAC = "mac";
    public static final String FAMILY_TANDEM = "tandem";
    public static final String FAMILY_UNIX = "unix";
    public static final String FAMILY_VMS = "openvms";
    public static final String FAMILY_ZOS = "z/os";
    public static final String FAMILY_OS400 = "os/400";
    private static final String DARWIN = "darwin";
    public Os() {
    }
    public Os(String family) {
        setFamily(family);
    }
    public void setFamily(String f) {
        family = f.toLowerCase(Locale.ENGLISH);
    }
    public void setName(String name) {
        this.name = name.toLowerCase(Locale.ENGLISH);
    }
    public void setArch(String arch) {
        this.arch = arch.toLowerCase(Locale.ENGLISH);
    }
    public void setVersion(String version) {
        this.version = version.toLowerCase(Locale.ENGLISH);
    }
    public boolean eval() throws BuildException {
        return isOs(family, name, arch, version);
    }
    public static boolean isFamily(String family) {
        return isOs(family, null, null, null);
    }
    public static boolean isName(String name) {
        return isOs(null, name, null, null);
    }
    public static boolean isArch(String arch) {
        return isOs(null, null, arch, null);
    }
    public static boolean isVersion(String version) {
        return isOs(null, null, null, version);
    }
    public static boolean isOs(String family, String name, String arch,
                               String version) {
        boolean retValue = false;
        if (family != null || name != null || arch != null
            || version != null) {
            boolean isFamily = true;
            boolean isName = true;
            boolean isArch = true;
            boolean isVersion = true;
            if (family != null) {
                boolean isWindows = OS_NAME.indexOf(FAMILY_WINDOWS) > -1;
                boolean is9x = false;
                boolean isNT = false;
                if (isWindows) {
                    is9x = (OS_NAME.indexOf("95") >= 0
                            || OS_NAME.indexOf("98") >= 0
                            || OS_NAME.indexOf("me") >= 0
                            || OS_NAME.indexOf("ce") >= 0);
                    isNT = !is9x;
                }
                if (family.equals(FAMILY_WINDOWS)) {
                    isFamily = isWindows;
                } else if (family.equals(FAMILY_9X)) {
                    isFamily = isWindows && is9x;
                } else if (family.equals(FAMILY_NT)) {
                    isFamily = isWindows && isNT;
                } else if (family.equals(FAMILY_OS2)) {
                    isFamily = OS_NAME.indexOf(FAMILY_OS2) > -1;
                } else if (family.equals(FAMILY_NETWARE)) {
                    isFamily = OS_NAME.indexOf(FAMILY_NETWARE) > -1;
                } else if (family.equals(FAMILY_DOS)) {
                    isFamily = PATH_SEP.equals(";") && !isFamily(FAMILY_NETWARE);
                } else if (family.equals(FAMILY_MAC)) {
                    isFamily = OS_NAME.indexOf(FAMILY_MAC) > -1
                        || OS_NAME.indexOf(DARWIN) > -1;
                } else if (family.equals(FAMILY_TANDEM)) {
                    isFamily = OS_NAME.indexOf("nonstop_kernel") > -1;
                } else if (family.equals(FAMILY_UNIX)) {
                    isFamily = PATH_SEP.equals(":")
                        && !isFamily(FAMILY_VMS)
                        && (!isFamily(FAMILY_MAC) || OS_NAME.endsWith("x")
                            || OS_NAME.indexOf(DARWIN) > -1);
                } else if (family.equals(FAMILY_ZOS)) {
                    isFamily = OS_NAME.indexOf(FAMILY_ZOS) > -1
                        || OS_NAME.indexOf("os/390") > -1;
                } else if (family.equals(FAMILY_OS400)) {
                    isFamily = OS_NAME.indexOf(FAMILY_OS400) > -1;
                } else if (family.equals(FAMILY_VMS)) {
                    isFamily = OS_NAME.indexOf(FAMILY_VMS) > -1;
                } else {
                    throw new BuildException(
                        "Don\'t know how to detect os family \""
                        + family + "\"");
                }
            }
            if (name != null) {
                isName = name.equals(OS_NAME);
            }
            if (arch != null) {
                isArch = arch.equals(OS_ARCH);
            }
            if (version != null) {
                isVersion = version.equals(OS_VERSION);
            }
            retValue = isFamily && isName && isArch && isVersion;
        }
        return retValue;
    }
}
