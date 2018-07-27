package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public class WebsphereDeploymentTool extends GenericDeploymentTool {
    public static final String PUBLICID_EJB11
         = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
    public static final String PUBLICID_EJB20
         = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
    protected static final String SCHEMA_DIR = "Schema/";
    protected static final String WAS_EXT = "ibm-ejb-jar-ext.xmi";
    protected static final String WAS_BND = "ibm-ejb-jar-bnd.xmi";
    protected static final String WAS_CMP_MAP = "Map.mapxmi";
    protected static final String WAS_CMP_SCHEMA = "Schema.dbxmi";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String jarSuffix = ".jar";
    private String ejb11DTD;
    private boolean keepGeneric = false;
    private boolean alwaysRebuild = true;
    private boolean ejbdeploy = true;
    private boolean newCMP = false;
    private Path wasClasspath = null;
    private String dbVendor;
    private String dbName;
    private String dbSchema;
    private boolean codegen;
    private boolean quiet = true;
    private boolean novalidate;
    private boolean nowarn;
    private boolean noinform;
    private boolean trace;
    private String rmicOptions;
    private boolean use35MappingRules;
    private String tempdir = "_ejbdeploy_temp";
    private File websphereHome;
    public Path createWASClasspath() {
        if (wasClasspath == null) {
            wasClasspath = new Path(getTask().getProject());
        }
        return wasClasspath.createPath();
    }
    public void setWASClasspath(Path wasClasspath) {
        this.wasClasspath = wasClasspath;
    }
    public void setDbvendor(String dbvendor) {
        this.dbVendor = dbvendor;
    }
    public void setDbname(String dbName) {
        this.dbName = dbName;
    }
    public void setDbschema(String dbSchema) {
        this.dbSchema = dbSchema;
    }
    public void setCodegen(boolean codegen) {
        this.codegen = codegen;
    }
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
    public void setNovalidate(boolean novalidate) {
        this.novalidate = novalidate;
    }
    public void setNowarn(boolean nowarn) {
        this.nowarn = nowarn;
    }
    public void setNoinform(boolean noinform) {
        this.noinform = noinform;
    }
    public void setTrace(boolean trace) {
        this.trace = trace;
    }
    public void setRmicoptions(String options) {
        this.rmicOptions = options;
    }
    public void setUse35(boolean attr) {
        use35MappingRules = attr;
    }
    public void setRebuild(boolean rebuild) {
        this.alwaysRebuild = rebuild;
    }
    public void setSuffix(String inString) {
        this.jarSuffix = inString;
    }
    public void setKeepgeneric(boolean inValue) {
        this.keepGeneric = inValue;
    }
    public void setEjbdeploy(boolean ejbdeploy) {
        this.ejbdeploy = ejbdeploy;
    }
    public void setEJBdtd(String inString) {
        this.ejb11DTD = inString;
    }
    public void setOldCMP(boolean oldCMP) {
        this.newCMP = !oldCMP;
    }
    public void setNewCMP(boolean newCMP) {
        this.newCMP = newCMP;
    }
    public void setTempdir(String tempdir) {
        this.tempdir = tempdir;
    }
    protected DescriptorHandler getDescriptorHandler(File srcDir) {
        DescriptorHandler handler = new DescriptorHandler(getTask(), srcDir);
        handler.registerDTD(PUBLICID_EJB11, ejb11DTD);
        for (Iterator i = getConfig().dtdLocations.iterator(); i.hasNext();) {
            EjbJar.DTDLocation dtdLocation = (EjbJar.DTDLocation) i.next();
            handler.registerDTD(dtdLocation.getPublicId(), dtdLocation.getLocation());
        }
        return handler;
    }
    protected DescriptorHandler getWebsphereDescriptorHandler(final File srcDir) {
        DescriptorHandler handler =
            new DescriptorHandler(getTask(), srcDir) {
                protected void processElement() {
                }
            };
        for (Iterator i = getConfig().dtdLocations.iterator(); i.hasNext();) {
            EjbJar.DTDLocation dtdLocation = (EjbJar.DTDLocation) i.next();
            handler.registerDTD(dtdLocation.getPublicId(), dtdLocation.getLocation());
        }
        return handler;
    }
    protected void addVendorFiles(Hashtable ejbFiles, String baseName) {
        String ddPrefix = (usingBaseJarName() ? "" : baseName);
        String dbPrefix = (dbVendor == null) ? "" : dbVendor + "-";
        File websphereEXT = new File(getConfig().descriptorDir, ddPrefix + WAS_EXT);
        if (websphereEXT.exists()) {
            ejbFiles.put(META_DIR + WAS_EXT,
                websphereEXT);
        } else {
            log("Unable to locate websphere extensions. "
                + "It was expected to be in "
                + websphereEXT.getPath(), Project.MSG_VERBOSE);
        }
        File websphereBND = new File(getConfig().descriptorDir, ddPrefix + WAS_BND);
        if (websphereBND.exists()) {
            ejbFiles.put(META_DIR + WAS_BND,
                websphereBND);
        } else {
            log("Unable to locate websphere bindings. "
                + "It was expected to be in "
                + websphereBND.getPath(), Project.MSG_VERBOSE);
        }
        if (!newCMP) {
            log("The old method for locating CMP files has been DEPRECATED.",
                Project.MSG_VERBOSE);
            log("Please adjust your websphere descriptor and set "
                + "newCMP=\"true\" to use the new CMP descriptor "
                + "inclusion mechanism. ", Project.MSG_VERBOSE);
        } else {
            try {
                File websphereMAP = new File(getConfig().descriptorDir,
                    ddPrefix + dbPrefix + WAS_CMP_MAP);
                if (websphereMAP.exists()) {
                    ejbFiles.put(META_DIR + WAS_CMP_MAP,
                        websphereMAP);
                } else {
                    log("Unable to locate the websphere Map: "
                        + websphereMAP.getPath(), Project.MSG_VERBOSE);
                }
                File websphereSchema = new File(getConfig().descriptorDir,
                    ddPrefix + dbPrefix + WAS_CMP_SCHEMA);
                if (websphereSchema.exists()) {
                    ejbFiles.put(META_DIR + SCHEMA_DIR + WAS_CMP_SCHEMA,
                        websphereSchema);
                } else {
                    log("Unable to locate the websphere Schema: "
                        + websphereSchema.getPath(), Project.MSG_VERBOSE);
                }
            } catch (Exception e) {
                String msg = "Exception while adding Vendor specific files: "
                    + e.toString();
                throw new BuildException(msg, e);
            }
        }
    }
    File getVendorOutputJarFile(String baseName) {
        return new File(getDestDir(), baseName + jarSuffix);
    }
    protected String getOptions() {
        StringBuffer options = new StringBuffer();
        if (dbVendor != null) {
            options.append(" -dbvendor ").append(dbVendor);
        }
        if (dbName != null) {
            options.append(" -dbname \"").append(dbName).append("\"");
        }
        if (dbSchema != null) {
            options.append(" -dbschema \"").append(dbSchema).append("\"");
        }
        if (codegen) {
            options.append(" -codegen");
        }
        if (quiet) {
            options.append(" -quiet");
        }
        if (novalidate) {
            options.append(" -novalidate");
        }
        if (nowarn) {
            options.append(" -nowarn");
        }
        if (noinform) {
            options.append(" -noinform");
        }
        if (trace) {
            options.append(" -trace");
        }
        if (use35MappingRules) {
            options.append(" -35");
        }
        if (rmicOptions != null) {
            options.append(" -rmic \"").append(rmicOptions).append("\"");
        }
        return options.toString();
    }
    private void buildWebsphereJar(File sourceJar, File destJar) {
        try {
            if (ejbdeploy) {
                Java javaTask = new Java(getTask());
                javaTask.createJvmarg().setValue("-Xms64m");
                javaTask.createJvmarg().setValue("-Xmx128m");
                Environment.Variable var = new Environment.Variable();
                var.setKey("websphere.lib.dir");
                File libdir = new File(websphereHome, "lib");
                var.setValue(libdir.getAbsolutePath());
                javaTask.addSysproperty(var);
                javaTask.setDir(websphereHome);
                javaTask.setTaskName("ejbdeploy");
                javaTask.setClassname("com.ibm.etools.ejbdeploy.EJBDeploy");
                javaTask.createArg().setValue(sourceJar.getPath());
                javaTask.createArg().setValue(tempdir);
                javaTask.createArg().setValue(destJar.getPath());
                javaTask.createArg().setLine(getOptions());
                if (getCombinedClasspath() != null
                    && getCombinedClasspath().toString().length() > 0) {
                    javaTask.createArg().setValue("-cp");
                    javaTask.createArg().setValue(getCombinedClasspath().toString());
                }
                Path classpath = wasClasspath;
                if (classpath == null) {
                    classpath = getCombinedClasspath();
                }
                javaTask.setFork(true);
                if (classpath != null) {
                    javaTask.setClasspath(classpath);
                }
                log("Calling websphere.ejbdeploy for " + sourceJar.toString(),
                    Project.MSG_VERBOSE);
                javaTask.execute();
            }
        } catch (Exception e) {
            String msg = "Exception while calling ejbdeploy. Details: " + e.toString();
            throw new BuildException(msg, e);
        }
    }
    protected void writeJar(String baseName, File jarFile, Hashtable files, String publicId)
         throws BuildException {
        if (ejbdeploy) {
            File genericJarFile = super.getVendorOutputJarFile(baseName);
            super.writeJar(baseName, genericJarFile, files, publicId);
            if (alwaysRebuild || isRebuildRequired(genericJarFile, jarFile)) {
                buildWebsphereJar(genericJarFile, jarFile);
            }
            if (!keepGeneric) {
                log("deleting generic jar " + genericJarFile.toString(),
                    Project.MSG_VERBOSE);
                genericJarFile.delete();
            }
        } else {
            super.writeJar(baseName, jarFile, files, publicId);
        }
    }
    public void validateConfigured() throws BuildException {
        super.validateConfigured();
        if (ejbdeploy) {
            String home = getTask().getProject().getProperty("websphere.home");
            if (home == null) {
                throw new BuildException("The 'websphere.home' property must "
                    + "be set when 'ejbdeploy=true'");
            }
            websphereHome = getTask().getProject().resolveFile(home);
        }
    }
    protected boolean isRebuildRequired(File genericJarFile, File websphereJarFile) {
        boolean rebuild = false;
        JarFile genericJar = null;
        JarFile wasJar = null;
        File newwasJarFile = null;
        JarOutputStream newJarStream = null;
        ClassLoader genericLoader = null;
        try {
            log("Checking if websphere Jar needs to be rebuilt for jar "
                + websphereJarFile.getName(), Project.MSG_VERBOSE);
            if (genericJarFile.exists() && genericJarFile.isFile()
                 && websphereJarFile.exists() && websphereJarFile.isFile()) {
                genericJar = new JarFile(genericJarFile);
                wasJar = new JarFile(websphereJarFile);
                Hashtable genericEntries = new Hashtable();
                Hashtable wasEntries = new Hashtable();
                Hashtable replaceEntries = new Hashtable();
                for (Enumeration e = genericJar.entries(); e.hasMoreElements();) {
                    JarEntry je = (JarEntry) e.nextElement();
                    genericEntries.put(je.getName().replace('\\', '/'), je);
                }
                for (Enumeration e = wasJar.entries(); e.hasMoreElements();) {
                    JarEntry je = (JarEntry) e.nextElement();
                    wasEntries.put(je.getName(), je);
                }
                genericLoader = getClassLoaderFromJar(genericJarFile);
                for (Enumeration e = genericEntries.keys(); e.hasMoreElements();) {
                    String filepath = (String) e.nextElement();
                    if (wasEntries.containsKey(filepath)) {
                        JarEntry genericEntry = (JarEntry) genericEntries.get(filepath);
                        JarEntry wasEntry = (JarEntry) wasEntries.get(filepath);
                        if ((genericEntry.getCrc() != wasEntry.getCrc())
                            || (genericEntry.getSize() != wasEntry.getSize())) {
                            if (genericEntry.getName().endsWith(".class")) {
                                String classname
                                    = genericEntry.getName().replace(File.separatorChar, '.');
                                classname = classname.substring(0, classname.lastIndexOf(".class"));
                                Class genclass = genericLoader.loadClass(classname);
                                if (genclass.isInterface()) {
                                    log("Interface " + genclass.getName()
                                        + " has changed", Project.MSG_VERBOSE);
                                    rebuild = true;
                                    break;
                                } else {
                                    replaceEntries.put(filepath, genericEntry);
                                }
                            } else {
                                if (!genericEntry.getName().equals("META-INF/MANIFEST.MF")) {
                                    log("Non class file " + genericEntry.getName()
                                        + " has changed", Project.MSG_VERBOSE);
                                    rebuild = true;
                                }
                                break;
                            }
                        }
                    } else {
                        log("File " + filepath + " not present in websphere jar",
                            Project.MSG_VERBOSE);
                        rebuild = true;
                        break;
                    }
                }
                if (!rebuild) {
                    log("No rebuild needed - updating jar", Project.MSG_VERBOSE);
                    newwasJarFile = new File(websphereJarFile.getAbsolutePath() + ".temp");
                    if (newwasJarFile.exists()) {
                        newwasJarFile.delete();
                    }
                    newJarStream = new JarOutputStream(new FileOutputStream(newwasJarFile));
                    newJarStream.setLevel(0);
                    for (Enumeration e = wasEntries.elements(); e.hasMoreElements();) {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int bytesRead;
                        InputStream is;
                        JarEntry je = (JarEntry) e.nextElement();
                        if (je.getCompressedSize() == -1
                            || je.getCompressedSize() == je.getSize()) {
                            newJarStream.setLevel(0);
                        } else {
                            newJarStream.setLevel(JAR_COMPRESS_LEVEL);
                        }
                        if (replaceEntries.containsKey(je.getName())) {
                            log("Updating Bean class from generic Jar " + je.getName(),
                                Project.MSG_VERBOSE);
                            je = (JarEntry) replaceEntries.get(je.getName());
                            is = genericJar.getInputStream(je);
                        } else {
                            is = wasJar.getInputStream(je);
                        }
                        newJarStream.putNextEntry(new JarEntry(je.getName()));
                        while ((bytesRead = is.read(buffer)) != -1) {
                            newJarStream.write(buffer, 0, bytesRead);
                        }
                        is.close();
                    }
                } else {
                    log("websphere Jar rebuild needed due to changed "
                        + "interface or XML", Project.MSG_VERBOSE);
                }
            } else {
                rebuild = true;
            }
        } catch (ClassNotFoundException cnfe) {
            String cnfmsg = "ClassNotFoundException while processing ejb-jar file"
                 + ". Details: "
                 + cnfe.getMessage();
            throw new BuildException(cnfmsg, cnfe);
        } catch (IOException ioe) {
            String msg = "IOException while processing ejb-jar file "
                 + ". Details: "
                 + ioe.getMessage();
            throw new BuildException(msg, ioe);
        } finally {
            if (genericJar != null) {
                try {
                    genericJar.close();
                } catch (IOException closeException) {
                }
            }
            if (wasJar != null) {
                try {
                    wasJar.close();
                } catch (IOException closeException) {
                }
            }
            if (newJarStream != null) {
                try {
                    newJarStream.close();
                } catch (IOException closeException) {
                }
                try {
                    FILE_UTILS.rename(newwasJarFile, websphereJarFile);
                } catch (IOException renameException) {
                    log(renameException.getMessage(), Project.MSG_WARN);
                    rebuild = true;
                }
            }
            if (genericLoader != null
                && genericLoader instanceof AntClassLoader) {
                AntClassLoader loader = (AntClassLoader) genericLoader;
                loader.cleanup();
            }
        }
        return rebuild;
    }
    protected ClassLoader getClassLoaderFromJar(File classjar) throws IOException {
        Path lookupPath = new Path(getTask().getProject());
        lookupPath.setLocation(classjar);
        Path classpath = getCombinedClasspath();
        if (classpath != null) {
            lookupPath.append(classpath);
        }
        return getTask().getProject().createClassLoader(lookupPath);
    }
}
