package org.apache.tools.ant.launch;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
public class Launcher {
    public static final String ANTHOME_PROPERTY = "ant.home";
    public static final String ANTLIBDIR_PROPERTY = "ant.library.dir";
    public static final String ANT_PRIVATEDIR = ".ant";
    public static final String ANT_PRIVATELIB = "lib";
    public static boolean launchDiag = false;
    public static final String USER_LIBDIR =
        ANT_PRIVATEDIR + File.separatorChar + ANT_PRIVATELIB;
    public static final String MAIN_CLASS = "org.apache.tools.ant.Main";
    public static final String USER_HOMEDIR = "user.home";
    private static final String JAVA_CLASS_PATH = "java.class.path";
    protected static final int EXIT_CODE_ERROR = 2;
    public static void main(String[] args) {
        int exitCode;
        try {
            Launcher launcher = new Launcher();
            exitCode = launcher.run(args);
        } catch (LaunchException e) {
            exitCode = EXIT_CODE_ERROR;
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            exitCode = EXIT_CODE_ERROR;
            t.printStackTrace(System.err);
        }
        if (exitCode != 0) {
            if (launchDiag) {
                System.out.println("Exit code: "+exitCode);
            }
            System.exit(exitCode);
        }
    }
    private void addPath(String path, boolean getJars, List libPathURLs)
            throws MalformedURLException {
        StringTokenizer tokenizer = new StringTokenizer(path, File.pathSeparator);
        while (tokenizer.hasMoreElements()) {
            String elementName = tokenizer.nextToken();
            File element = new File(elementName);
            if (elementName.indexOf('%') != -1 && !element.exists()) {
                continue;
            }
            if (getJars && element.isDirectory()) {
                URL[] dirURLs = Locator.getLocationURLs(element);
                for (int j = 0; j < dirURLs.length; ++j) {
                    if (launchDiag) { System.out.println("adding library JAR: " + dirURLs[j]);}
                    libPathURLs.add(dirURLs[j]);
                }
            }
            URL url = Locator.fileToURL(element);
            if (launchDiag) { System.out.println("adding library URL: " + url) ;}
            libPathURLs.add(url);
        }
    }
    private int run(String[] args)
            throws LaunchException, MalformedURLException {
        String antHomeProperty = System.getProperty(ANTHOME_PROPERTY);
        File antHome = null;
        File sourceJar = Locator.getClassSource(getClass());
        File jarDir = sourceJar.getParentFile();
        String mainClassname = MAIN_CLASS;
        if (antHomeProperty != null) {
            antHome = new File(antHomeProperty);
        }
        if (antHome == null || !antHome.exists()) {
            antHome = jarDir.getParentFile();
            setProperty(ANTHOME_PROPERTY, antHome.getAbsolutePath());
        }
        if (!antHome.exists()) {
            throw new LaunchException("Ant home is set incorrectly or "
                + "ant could not be located (estimated value="+antHome.getAbsolutePath()+")");
        }
        List libPaths = new ArrayList();
        String cpString = null;
        List argList = new ArrayList();
        String[] newArgs;
        boolean  noUserLib = false;
        boolean  noClassPath = false;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-lib")) {
                if (i == args.length - 1) {
                    throw new LaunchException("The -lib argument must "
                        + "be followed by a library location");
                }
                libPaths.add(args[++i]);
            } else if (args[i].equals("-cp")) {
                if (i == args.length - 1) {
                    throw new LaunchException("The -cp argument must "
                        + "be followed by a classpath expression");
                }
                if (cpString != null) {
                    throw new LaunchException("The -cp argument must "
                        + "not be repeated");
                }
                cpString = args[++i];
            } else if (args[i].equals("--nouserlib") || args[i].equals("-nouserlib")) {
                noUserLib = true;
            } else if (args[i].equals("--launchdiag")) {
                launchDiag = true;
            } else if (args[i].equals("--noclasspath") || args[i].equals("-noclasspath")) {
                noClassPath = true;
            } else if (args[i].equals("-main")) {
                if (i == args.length - 1) {
                    throw new LaunchException("The -main argument must "
                            + "be followed by a library location");
                }
                mainClassname = args[++i];
            } else {
                argList.add(args[i]);
            }
        }
        logPath("Launcher JAR",sourceJar);
        logPath("Launcher JAR directory", sourceJar.getParentFile());
        logPath("java.home", new File(System.getProperty("java.home")));
        if (argList.size() == args.length) {
            newArgs = args;
        } else {
            newArgs = (String[]) argList.toArray(new String[argList.size()]);
        }
        URL[] libURLs    = getLibPathURLs(
            noClassPath ? null : cpString, libPaths);
        URL[] systemURLs = getSystemURLs(jarDir);
        URL[] userURLs   = noUserLib ? new URL[0] : getUserURLs();
        File toolsJAR = Locator.getToolsJar();
        logPath("tools.jar",toolsJAR);
        URL[] jars = getJarArray(
            libURLs, userURLs, systemURLs, toolsJAR);
        StringBuffer baseClassPath
            = new StringBuffer(System.getProperty(JAVA_CLASS_PATH));
        if (baseClassPath.charAt(baseClassPath.length() - 1)
                == File.pathSeparatorChar) {
            baseClassPath.setLength(baseClassPath.length() - 1);
        }
        for (int i = 0; i < jars.length; ++i) {
            baseClassPath.append(File.pathSeparatorChar);
            baseClassPath.append(Locator.fromURI(jars[i].toString()));
        }
        setProperty(JAVA_CLASS_PATH, baseClassPath.toString());
        URLClassLoader loader = new URLClassLoader(jars);
        Thread.currentThread().setContextClassLoader(loader);
        Class mainClass = null;
        int exitCode = 0;
        Throwable thrown=null;
        try {
            mainClass = loader.loadClass(mainClassname);
            AntMain main = (AntMain) mainClass.newInstance();
            main.startAnt(newArgs, null, null);
        } catch (InstantiationException ex) {
            System.err.println(
                "Incompatible version of " + mainClassname + " detected");
            File mainJar = Locator.getClassSource(mainClass);
            System.err.println(
                "Location of this class " + mainJar);
            thrown = ex;
        } catch (ClassNotFoundException cnfe) {
            System.err.println(
                    "Failed to locate" + mainClassname);
            thrown = cnfe;
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            thrown=t;
        }
        if(thrown!=null) {
            System.err.println(ANTHOME_PROPERTY+": "+antHome.getAbsolutePath());
            System.err.println("Classpath: " + baseClassPath.toString());
            System.err.println("Launcher JAR: " + sourceJar.getAbsolutePath());
            System.err.println("Launcher Directory: " + jarDir.getAbsolutePath());
            exitCode = EXIT_CODE_ERROR;
        }
        return exitCode;
    }
    private URL[] getLibPathURLs(String cpString, List libPaths)
        throws MalformedURLException {
        List libPathURLs = new ArrayList();
        if (cpString != null) {
            addPath(cpString, false, libPathURLs);
        }
        for (Iterator i = libPaths.iterator(); i.hasNext();) {
            String libPath = (String) i.next();
            addPath(libPath, true, libPathURLs);
        }
        return  (URL[]) libPathURLs.toArray(new URL[libPathURLs.size()]);
    }
    private URL[] getSystemURLs(File antLauncherDir) throws MalformedURLException {
        File antLibDir = null;
        String antLibDirProperty = System.getProperty(ANTLIBDIR_PROPERTY);
        if (antLibDirProperty != null) {
            antLibDir = new File(antLibDirProperty);
        }
        if ((antLibDir == null) || !antLibDir.exists()) {
            antLibDir = antLauncherDir;
            setProperty(ANTLIBDIR_PROPERTY, antLibDir.getAbsolutePath());
        }
        return Locator.getLocationURLs(antLibDir);
    }
    private URL[] getUserURLs() throws MalformedURLException {
        File userLibDir
            = new File(System.getProperty(USER_HOMEDIR), USER_LIBDIR);
        return Locator.getLocationURLs(userLibDir);
    }
    private URL[] getJarArray (
        URL[] libJars, URL[] userJars, URL[] systemJars, File toolsJar)
        throws MalformedURLException {
        int numJars = libJars.length + userJars.length + systemJars.length;
        if (toolsJar != null) {
            numJars++;
        }
        URL[] jars = new URL[numJars];
        System.arraycopy(libJars, 0, jars, 0, libJars.length);
        System.arraycopy(userJars, 0, jars, libJars.length, userJars.length);
        System.arraycopy(systemJars, 0, jars, userJars.length + libJars.length,
            systemJars.length);
        if (toolsJar != null) {
            jars[jars.length - 1] = Locator.fileToURL(toolsJar);
        }
        return jars;
    }
    private void setProperty(String name, String value) {
        if (launchDiag) {
            System.out.println("Setting \"" + name + "\" to \"" + value + "\"");
        }
        System.setProperty(name, value);
    }
    private void logPath(String name,File path) {
        if(launchDiag) {
            System.out.println(name+"= \""+path+"\"");
        }
    }
}
