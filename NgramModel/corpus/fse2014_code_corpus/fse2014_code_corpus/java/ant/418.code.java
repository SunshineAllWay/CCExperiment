package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class IPlanetEjbc {
    private static final int MIN_NUM_ARGS = 2;
    private static final int MAX_NUM_ARGS = 8;
    private static final int NUM_CLASSES_WITH_IIOP = 15;
    private static final int NUM_CLASSES_WITHOUT_IIOP = 9;
    private static final String ENTITY_BEAN       = "entity";
    private static final String STATELESS_SESSION = "stateless";
    private static final String STATEFUL_SESSION  = "stateful";
    private File        stdDescriptor;
    private File        iasDescriptor;
    private File        destDirectory;
    private String      classpath;
    private String[]    classpathElements;
    private boolean     retainSource = false;
    private boolean     debugOutput  = false;
    private File        iasHomeDir;
    private SAXParser   parser;
    private EjbcHandler handler = new EjbcHandler();
    private Hashtable   ejbFiles     = new Hashtable();
    private String      displayName;
    public IPlanetEjbc(File stdDescriptor,
                       File iasDescriptor,
                       File destDirectory,
                       String classpath,
                       SAXParser parser) {
        this.stdDescriptor = stdDescriptor;
        this.iasDescriptor      = iasDescriptor;
        this.destDirectory      = destDirectory;
        this.classpath          = classpath;
        this.parser             = parser;
        List elements = new ArrayList();
        if (classpath != null) {
            StringTokenizer st = new StringTokenizer(classpath,
                                                        File.pathSeparator);
            while (st.hasMoreTokens()) {
                elements.add(st.nextToken());
            }
            classpathElements
                    = (String[]) elements.toArray(new String[elements.size()]);
        }
    }
    public void setRetainSource(boolean retainSource) {
        this.retainSource = retainSource;
    }
    public void setDebugOutput(boolean debugOutput) {
        this.debugOutput = debugOutput;
    }
    public void registerDTD(String publicID, String location) {
        handler.registerDTD(publicID, location);
    }
    public void setIasHomeDir(File iasHomeDir) {
        this.iasHomeDir = iasHomeDir;
    }
    public Hashtable getEjbFiles() {
        return ejbFiles;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String[] getCmpDescriptors() {
        List returnList = new ArrayList();
        EjbInfo[] ejbs = handler.getEjbs();
        for (int i = 0; i < ejbs.length; i++) {
            List descriptors = (List) ejbs[i].getCmpDescriptors();
            returnList.addAll(descriptors);
        }
        return (String[]) returnList.toArray(new String[returnList.size()]);
    }
    public static void main(String[] args) {
        File        stdDescriptor;
        File        iasDescriptor;
        File        destDirectory = null;
        String      classpath     = null;
        SAXParser   parser        = null;
        boolean     debug         = false;
        boolean     retainSource  = false;
        IPlanetEjbc ejbc;
        if ((args.length < MIN_NUM_ARGS) || (args.length > MAX_NUM_ARGS)) {
            usage();
            return;
        }
        stdDescriptor = new File(args[args.length - 2]);
        iasDescriptor = new File(args[args.length - 1]);
        for (int i = 0; i < args.length - 2; i++) {
            if (args[i].equals("-classpath")) {
                classpath = args[++i];
            } else if (args[i].equals("-d")) {
                destDirectory = new File(args[++i]);
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else if (args[i].equals("-keepsource")) {
                retainSource = true;
            } else {
                usage();
                return;
            }
        }
        if (classpath == null) {
            Properties props = System.getProperties();
            classpath = props.getProperty("java.class.path");
        }
        if (destDirectory == null) {
            Properties props = System.getProperties();
            destDirectory = new File(props.getProperty("user.dir"));
        }
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(true);
        try {
            parser = parserFactory.newSAXParser();
        } catch (Exception e) {
            System.out.println("An exception was generated while trying to ");
            System.out.println("create a new SAXParser.");
            e.printStackTrace();
            return;
        }
        ejbc = new IPlanetEjbc(stdDescriptor, iasDescriptor, destDirectory,
                                classpath, parser);
        ejbc.setDebugOutput(debug);
        ejbc.setRetainSource(retainSource);
        try {
            ejbc.execute();
        } catch (IOException e) {
            System.out.println("An IOException has occurred while reading the "
                    + "XML descriptors (" + e.getMessage() + ").");
            return;
        } catch (SAXException e) {
            System.out.println("A SAXException has occurred while reading the "
                    + "XML descriptors (" + e.getMessage() + ").");
            return;
        } catch (IPlanetEjbc.EjbcException e) {
            System.out.println("An error has occurred while executing the ejbc "
                    + "utility (" + e.getMessage() + ").");
            return;
        }
    }
    private static void usage() {
        System.out.println("java org.apache.tools.ant.taskdefs.optional.ejb.IPlanetEjbc \\");
        System.out.println("  [OPTIONS] [EJB 1.1 descriptor] [iAS EJB descriptor]");
        System.out.println("");
        System.out.println("Where OPTIONS are:");
        System.out.println("  -debug -- for additional debugging output");
        System.out.println("  -keepsource -- to retain Java source files generated");
        System.out.println("  -classpath [classpath] -- classpath used for compilation");
        System.out.println("  -d [destination directory] -- directory for compiled classes");
        System.out.println("");
        System.out.println("If a classpath is not specified, the system classpath");
        System.out.println("will be used.  If a destination directory is not specified,");
        System.out.println("the current working directory will be used (classes will");
        System.out.println("still be placed in subfolders which correspond to their");
        System.out.println("package name).");
        System.out.println("");
        System.out.println("The EJB home interface, remote interface, and implementation");
        System.out.println("class must be found in the destination directory.  In");
        System.out.println("addition, the destination will look for the stubs and skeletons");
        System.out.println("in the destination directory to ensure they are up to date.");
    }
    public void execute() throws EjbcException, IOException, SAXException {
        checkConfiguration();   
        EjbInfo[] ejbs = getEjbs(); 
        for (int i = 0; i < ejbs.length; i++) {
            log("EJBInfo...");
            log(ejbs[i].toString());
        }
        for (int i = 0; i < ejbs.length; i++) {
            EjbInfo ejb = ejbs[i];
            ejb.checkConfiguration(destDirectory);  
            if (ejb.mustBeRecompiled(destDirectory)) {
                log(ejb.getName() + " must be recompiled using ejbc.");
                String[] arguments = buildArgumentList(ejb);
                callEjbc(arguments);
            } else {
                log(ejb.getName() + " is up to date.");
            }
        }
    }
    private void callEjbc(String[] arguments) {
        StringBuffer args = new StringBuffer();
        for (int i = 0; i < arguments.length; i++) {
            args.append(arguments[i]).append(" ");
        }
        String command;
        if (iasHomeDir == null) {
            command = "";
        } else {
            command = iasHomeDir.toString() + File.separator + "bin"
                                                        + File.separator;
        }
        command += "ejbc ";
        log(command + args);
        try {
            Process p = Runtime.getRuntime().exec(command + args);
            RedirectOutput output = new RedirectOutput(p.getInputStream());
            RedirectOutput error  = new RedirectOutput(p.getErrorStream());
            output.start();
            error.start();
            p.waitFor();
            p.destroy();
        } catch (IOException e) {
            log("An IOException has occurred while trying to execute ejbc.");
            e.printStackTrace();
        } catch (InterruptedException e) {
        }
    }
    protected void checkConfiguration() throws EjbcException {
        String msg = "";
        if (stdDescriptor == null) {
            msg += "A standard XML descriptor file must be specified.  ";
        }
        if (iasDescriptor == null) {
            msg += "An iAS-specific XML descriptor file must be specified.  ";
        }
        if (classpath == null) {
            msg += "A classpath must be specified.    ";
        }
        if (parser == null) {
            msg += "An XML parser must be specified.    ";
        }
        if (destDirectory == null) {
            msg += "A destination directory must be specified.  ";
        } else if (!destDirectory.exists()) {
            msg += "The destination directory specified does not exist.  ";
        } else if (!destDirectory.isDirectory()) {
            msg += "The destination specified is not a directory.  ";
        }
        if (msg.length() > 0) {
            throw new EjbcException(msg);
        }
    }
    private EjbInfo[] getEjbs() throws IOException, SAXException {
        EjbInfo[] ejbs = null;
        parser.parse(stdDescriptor, handler);
        parser.parse(iasDescriptor, handler);
        ejbs = handler.getEjbs();
        return ejbs;
    }
    private String[] buildArgumentList(EjbInfo ejb) {
        List arguments = new ArrayList();
        if (debugOutput) {
            arguments.add("-debug");
        }
        if (ejb.getBeantype().equals(STATELESS_SESSION)) {
            arguments.add("-sl");
        } else if (ejb.getBeantype().equals(STATEFUL_SESSION)) {
            arguments.add("-sf");
        }
        if (ejb.getIiop()) {
            arguments.add("-iiop");
        }
        if (ejb.getCmp()) {
            arguments.add("-cmp");
        }
        if (retainSource) {
            arguments.add("-gs");
        }
        if (ejb.getHasession()) {
            arguments.add("-fo");
        }
        arguments.add("-classpath");
        arguments.add(classpath);
        arguments.add("-d");
        arguments.add(destDirectory.toString());
        arguments.add(ejb.getHome().getQualifiedClassName());
        arguments.add(ejb.getRemote().getQualifiedClassName());
        arguments.add(ejb.getImplementation().getQualifiedClassName());
        return (String[]) arguments.toArray(new String[arguments.size()]);
    }
    private void log(String msg) {
        if (debugOutput) {
            System.out.println(msg);
        }
    }
    public class EjbcException extends Exception {
        public EjbcException(String msg) {
            super(msg);
        }
    }  
    private class EjbcHandler extends HandlerBase {
        private static final String PUBLICID_EJB11 =
            "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
        private static final String PUBLICID_IPLANET_EJB_60 =
            "-//Sun Microsystems, Inc.//DTD iAS Enterprise JavaBeans 1.0//EN";
        private static final String DEFAULT_IAS60_EJB11_DTD_LOCATION =
            "ejb-jar_1_1.dtd";
        private static final String DEFAULT_IAS60_DTD_LOCATION =
            "IASEjb_jar_1_0.dtd";
        private Map       resourceDtds = new HashMap();
        private Map       fileDtds = new HashMap();
        private Map       ejbs = new HashMap();      
        private EjbInfo   currentEjb;             
        private boolean   iasDescriptor = false;  
        private String    currentLoc = "";        
        private String    currentText;            
        private String    ejbType;                
        public EjbcHandler() {
            registerDTD(PUBLICID_EJB11, DEFAULT_IAS60_EJB11_DTD_LOCATION);
            registerDTD(PUBLICID_IPLANET_EJB_60, DEFAULT_IAS60_DTD_LOCATION);
        }
        public EjbInfo[] getEjbs() {
            return (EjbInfo[]) ejbs.values().toArray(new EjbInfo[ejbs.size()]);
        }
        public String getDisplayName() {
            return displayName;
        }
        public void registerDTD(String publicID, String location) {
            log("Registering: " + location);
            if ((publicID == null) || (location == null)) {
                return;
            }
            if (ClassLoader.getSystemResource(location) != null) {
                log("Found resource: " + location);
                resourceDtds.put(publicID, location);
            } else {
                File dtdFile = new File(location);
                if (dtdFile.exists() && dtdFile.isFile()) {
                    log("Found file: " + location);
                    fileDtds.put(publicID, location);
                }
            }
        }
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException {
            InputStream inputStream = null;
            try {
                String location = (String) resourceDtds.get(publicId);
                if (location != null) {
                    inputStream
                        = ClassLoader.getSystemResource(location).openStream();
                } else {
                    location = (String) fileDtds.get(publicId);
                    if (location != null) {
                        inputStream = new FileInputStream(location);
                    }
                }
            } catch (IOException e) {
                return super.resolveEntity(publicId, systemId);
            }
            if (inputStream == null) {
                return super.resolveEntity(publicId, systemId);
            } else {
                return new InputSource(inputStream);
            }
        }
        public void startElement(String name, AttributeList atts)
                throws SAXException {
            currentLoc += "\\" + name;
            currentText = "";
            if (currentLoc.equals("\\ejb-jar")) {
                iasDescriptor = false;
            } else if (currentLoc.equals("\\ias-ejb-jar")) {
                iasDescriptor = true;
            }
            if ((name.equals("session")) || (name.equals("entity"))) {
                ejbType = name;
            }
        }
        public void characters(char[] ch, int start, int len)
                throws SAXException {
            currentText += new String(ch).substring(start, start + len);
        }
        public void endElement(String name) throws SAXException {
            if (iasDescriptor) {
                iasCharacters(currentText);
            } else {
                stdCharacters(currentText);
            }
            int nameLength = name.length() + 1; 
            int locLength  = currentLoc.length();
            currentLoc = currentLoc.substring(0, locLength - nameLength);
        }
        private void stdCharacters(String value) {
            if (currentLoc.equals("\\ejb-jar\\display-name")) {
                displayName = value;
                return;
            }
            String base = "\\ejb-jar\\enterprise-beans\\" + ejbType;
            if (currentLoc.equals(base + "\\ejb-name")) {
                currentEjb = (EjbInfo) ejbs.get(value);
                if (currentEjb == null) {
                    currentEjb = new EjbInfo(value);
                    ejbs.put(value, currentEjb);
                }
            } else if (currentLoc.equals(base + "\\home")) {
                currentEjb.setHome(value);
            } else if (currentLoc.equals(base + "\\remote")) {
                currentEjb.setRemote(value);
            } else if (currentLoc.equals(base + "\\ejb-class")) {
                currentEjb.setImplementation(value);
            } else if (currentLoc.equals(base + "\\prim-key-class")) {
                currentEjb.setPrimaryKey(value);
            } else if (currentLoc.equals(base + "\\session-type")) {
                currentEjb.setBeantype(value);
            } else if (currentLoc.equals(base + "\\persistence-type")) {
                currentEjb.setCmp(value);
            }
        }
        private void iasCharacters(String value) {
            String base = "\\ias-ejb-jar\\enterprise-beans\\" + ejbType;
            if (currentLoc.equals(base + "\\ejb-name")) {
                currentEjb = (EjbInfo) ejbs.get(value);
                if (currentEjb == null) {
                    currentEjb = new EjbInfo(value);
                    ejbs.put(value, currentEjb);
                }
            } else if (currentLoc.equals(base + "\\iiop")) {
                currentEjb.setIiop(value);
            } else if (currentLoc.equals(base + "\\failover-required")) {
                currentEjb.setHasession(value);
            } else if (currentLoc.equals(base + "\\persistence-manager"
                                              + "\\properties-file-location")) {
                currentEjb.addCmpDescriptor(value);
            }
        }
    }  
    private class EjbInfo {
        private String     name;              
        private Classname  home;              
        private Classname  remote;            
        private Classname  implementation;      
        private Classname  primaryKey;        
        private String  beantype = "entity";  
        private boolean cmp       = false;      
        private boolean iiop      = false;      
        private boolean hasession = false;      
        private List cmpDescriptors = new ArrayList();  
        public EjbInfo(String name) {
            this.name = name;
        }
        public String getName() {
            if (name == null) {
                if (implementation == null) {
                    return "[unnamed]";
                } else {
                    return implementation.getClassName();
                }
            }
            return name;
        }
        public void setHome(String home) {
            setHome(new Classname(home));
        }
        public void setHome(Classname home) {
            this.home = home;
        }
        public Classname getHome() {
            return home;
        }
        public void setRemote(String remote) {
            setRemote(new Classname(remote));
        }
        public void setRemote(Classname remote) {
            this.remote = remote;
        }
        public Classname getRemote() {
            return remote;
        }
        public void setImplementation(String implementation) {
            setImplementation(new Classname(implementation));
        }
        public void setImplementation(Classname implementation) {
            this.implementation = implementation;
        }
        public Classname getImplementation() {
            return implementation;
        }
        public void setPrimaryKey(String primaryKey) {
            setPrimaryKey(new Classname(primaryKey));
        }
        public void setPrimaryKey(Classname primaryKey) {
            this.primaryKey = primaryKey;
        }
        public Classname getPrimaryKey() {
            return primaryKey;
        }
        public void setBeantype(String beantype) {
            this.beantype = beantype.toLowerCase();
        }
        public String getBeantype() {
            return beantype;
        }
        public void setCmp(boolean cmp) {
            this.cmp = cmp;
        }
        public void setCmp(String cmp) {
            setCmp(cmp.equals("Container"));
        }
        public boolean getCmp() {
            return cmp;
        }
        public void setIiop(boolean iiop) {
            this.iiop = iiop;
        }
        public void setIiop(String iiop) {
            setIiop(iiop.equals("true"));
        }
        public boolean getIiop() {
            return iiop;
        }
        public void setHasession(boolean hasession) {
            this.hasession = hasession;
        }
        public void setHasession(String hasession) {
            setHasession(hasession.equals("true"));
        }
        public boolean getHasession() {
            return hasession;
        }
        public void addCmpDescriptor(String descriptor) {
            cmpDescriptors.add(descriptor);
        }
        public List getCmpDescriptors() {
            return cmpDescriptors;
        }
        private void checkConfiguration(File buildDir) throws EjbcException  {
            if (home == null) {
                throw new EjbcException("A home interface was not found "
                            + "for the " + name + " EJB.");
            }
            if (remote == null) {
                throw new EjbcException("A remote interface was not found "
                            + "for the " + name + " EJB.");
            }
            if (implementation == null) {
                throw new EjbcException("An EJB implementation class was not "
                            + "found for the " + name + " EJB.");
            }
            if ((!beantype.equals(ENTITY_BEAN))
                        && (!beantype.equals(STATELESS_SESSION))
                        && (!beantype.equals(STATEFUL_SESSION))) {
                throw new EjbcException("The beantype found (" + beantype + ") "
                            + "isn't valid in the " + name + " EJB.");
            }
            if (cmp && (!beantype.equals(ENTITY_BEAN))) {
                System.out.println("CMP stubs and skeletons may not be generated"
                    + " for a Session Bean -- the \"cmp\" attribute will be"
                    + " ignoredfor the " + name + " EJB.");
            }
            if (hasession && (!beantype.equals(STATEFUL_SESSION))) {
                System.out.println("Highly available stubs and skeletons may "
                    + "only be generated for a Stateful Session Bean -- the "
                    + "\"hasession\" attribute will be ignored for the "
                    + name + " EJB.");
            }
            if (!remote.getClassFile(buildDir).exists()) {
                throw new EjbcException("The remote interface "
                            + remote.getQualifiedClassName() + " could not be "
                            + "found.");
            }
            if (!home.getClassFile(buildDir).exists()) {
                throw new EjbcException("The home interface "
                            + home.getQualifiedClassName() + " could not be "
                            + "found.");
            }
            if (!implementation.getClassFile(buildDir).exists()) {
                throw new EjbcException("The EJB implementation class "
                            + implementation.getQualifiedClassName() + " could "
                            + "not be found.");
            }
        }
        public boolean mustBeRecompiled(File destDir) {
            long sourceModified = sourceClassesModified(destDir);
            long destModified = destClassesModified(destDir);
            return (destModified < sourceModified);
        }
        private long sourceClassesModified(File buildDir) {
            long latestModified; 
            long modified;       
            File remoteFile;     
            File homeFile;       
            File implFile;       
            File pkFile;         
            remoteFile = remote.getClassFile(buildDir);
            modified = remoteFile.lastModified();
            if (modified == -1) {
                System.out.println("The class "
                                + remote.getQualifiedClassName() + " couldn't "
                                + "be found on the classpath");
                return -1;
            }
            latestModified = modified;
            homeFile = home.getClassFile(buildDir);
            modified = homeFile.lastModified();
            if (modified == -1) {
                System.out.println("The class "
                                + home.getQualifiedClassName() + " couldn't be "
                                + "found on the classpath");
                return -1;
            }
            latestModified = Math.max(latestModified, modified);
            if (primaryKey != null) {
                pkFile = primaryKey.getClassFile(buildDir);
                modified = pkFile.lastModified();
                if (modified == -1) {
                    System.out.println("The class "
                                    + primaryKey.getQualifiedClassName() + "couldn't be "
                                    + "found on the classpath");
                    return -1;
                }
                latestModified = Math.max(latestModified, modified);
            } else {
                pkFile = null;
            }
            implFile = implementation.getClassFile(buildDir);
            modified = implFile.lastModified();
            if (modified == -1) {
                System.out.println("The class "
                                + implementation.getQualifiedClassName()
                                + " couldn't be found on the classpath");
                return -1;
            }
            String pathToFile = remote.getQualifiedClassName();
            pathToFile = pathToFile.replace('.', File.separatorChar) + ".class";
            ejbFiles.put(pathToFile, remoteFile);
            pathToFile = home.getQualifiedClassName();
            pathToFile = pathToFile.replace('.', File.separatorChar) + ".class";
            ejbFiles.put(pathToFile, homeFile);
            pathToFile = implementation.getQualifiedClassName();
            pathToFile = pathToFile.replace('.', File.separatorChar) + ".class";
            ejbFiles.put(pathToFile, implFile);
            if (pkFile != null) {
                pathToFile = primaryKey.getQualifiedClassName();
                pathToFile = pathToFile.replace('.', File.separatorChar) + ".class";
                ejbFiles.put(pathToFile, pkFile);
            }
            return latestModified;
        }
        private long destClassesModified(File destDir) {
            String[] classnames = classesToGenerate(); 
            long destClassesModified = new Date().getTime(); 
            boolean allClassesFound  = true;           
            for (int i = 0; i < classnames.length; i++) {
                String pathToClass =
                        classnames[i].replace('.', File.separatorChar) + ".class";
                File classFile = new File(destDir, pathToClass);
                ejbFiles.put(pathToClass, classFile);
                allClassesFound = allClassesFound && classFile.exists();
                if (allClassesFound) {
                    long fileMod = classFile.lastModified();
                    destClassesModified = Math.min(destClassesModified, fileMod);
                }
            }
            return (allClassesFound) ? destClassesModified : -1;
        }
        private String[] classesToGenerate() {
            String[] classnames = (iiop)
                ? new String[NUM_CLASSES_WITH_IIOP]
                : new String[NUM_CLASSES_WITHOUT_IIOP];
            final String remotePkg     = remote.getPackageName() + ".";
            final String remoteClass   = remote.getClassName();
            final String homePkg       = home.getPackageName() + ".";
            final String homeClass     = home.getClassName();
            final String implPkg       = implementation.getPackageName() + ".";
            final String implFullClass = implementation.getQualifiedWithUnderscores();
            int index = 0;
            classnames[index++] = implPkg + "ejb_fac_" + implFullClass;
            classnames[index++] = implPkg + "ejb_home_" + implFullClass;
            classnames[index++] = implPkg + "ejb_skel_" + implFullClass;
            classnames[index++] = remotePkg + "ejb_kcp_skel_" + remoteClass;
            classnames[index++] = homePkg + "ejb_kcp_skel_" + homeClass;
            classnames[index++] = remotePkg + "ejb_kcp_stub_" + remoteClass;
            classnames[index++] = homePkg + "ejb_kcp_stub_" + homeClass;
            classnames[index++] = remotePkg + "ejb_stub_" + remoteClass;
            classnames[index++] = homePkg + "ejb_stub_" + homeClass;
            if (!iiop) {
                return classnames;
            }
            classnames[index++] = "org.omg.stub." + remotePkg + "_"
                                    + remoteClass + "_Stub";
            classnames[index++] = "org.omg.stub." + homePkg + "_"
                                    + homeClass + "_Stub";
            classnames[index++] = "org.omg.stub." + remotePkg
                                    + "_ejb_RmiCorbaBridge_"
                                    + remoteClass + "_Tie";
            classnames[index++] = "org.omg.stub." + homePkg
                                    + "_ejb_RmiCorbaBridge_"
                                    + homeClass + "_Tie";
            classnames[index++] = remotePkg + "ejb_RmiCorbaBridge_"
                                                        + remoteClass;
            classnames[index++] = homePkg + "ejb_RmiCorbaBridge_" + homeClass;
            return classnames;
        }
        public String toString() {
            String s = "EJB name: " + name
                        + "\n\r              home:      " + home
                        + "\n\r              remote:    " + remote
                        + "\n\r              impl:      " + implementation
                        + "\n\r              primaryKey: " + primaryKey
                        + "\n\r              beantype:  " + beantype
                        + "\n\r              cmp:       " + cmp
                        + "\n\r              iiop:      " + iiop
                        + "\n\r              hasession: " + hasession;
            Iterator i = cmpDescriptors.iterator();
            while (i.hasNext()) {
                s += "\n\r              CMP Descriptor: " + i.next();
            }
            return s;
        }
    } 
    private static class Classname {
        private String qualifiedName;  
        private String packageName;    
        private String className;      
        public Classname(String qualifiedName) {
            if (qualifiedName == null) {
                return;
            }
            this.qualifiedName = qualifiedName;
            int index = qualifiedName.lastIndexOf('.');
            if (index == -1) {
                className = qualifiedName;
                packageName = "";
            } else {
                packageName = qualifiedName.substring(0, index);
                className   = qualifiedName.substring(index + 1);
            }
        }
        public String getQualifiedClassName() {
            return qualifiedName;
        }
        public String getPackageName() {
            return packageName;
        }
        public String getClassName() {
            return className;
        }
        public String getQualifiedWithUnderscores() {
            return qualifiedName.replace('.', '_');
        }
        public File getClassFile(File directory) {
            String pathToFile = qualifiedName.replace('.', File.separatorChar)
                                            + ".class";
            return new File(directory, pathToFile);
        }
        public String toString() {
            return getQualifiedClassName();
        }
    }  
    private static class RedirectOutput extends Thread {
        private InputStream stream;  
        public RedirectOutput(InputStream stream) {
            this.stream = stream;
        }
        public void run() {
            BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(stream));
            String text;
            try {
                while ((text = reader.readLine()) != null) {
                    System.out.println(text);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }  
}
