package org.apache.tools.ant.taskdefs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Manifest.Section;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.spi.Service;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.JarMarker;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipOutputStream;
public class Jar extends Zip {
    private static final String INDEX_NAME = "META-INF/INDEX.LIST";
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private List serviceList = new ArrayList();
    private Manifest configuredManifest;
    private Manifest savedConfiguredManifest;
    private Manifest filesetManifest;
    private Manifest originalManifest;
    private FilesetManifestConfig filesetManifestConfig;
    private boolean mergeManifestsMain = true;
    private Manifest manifest;
    private String manifestEncoding;
    private File manifestFile;
    private boolean index = false;
    private boolean indexMetaInf = false;
    private boolean createEmpty = false;
    private Vector rootEntries;
    private Path indexJars;
    private StrictMode strict = new StrictMode("ignore");
    private boolean mergeClassPaths = false;
    private boolean flattenClassPaths = false;
    private static final ZipExtraField[] JAR_MARKER = new ZipExtraField[] {
        JarMarker.getInstance()
    };
    public Jar() {
        super();
        archiveType = "jar";
        emptyBehavior = "create";
        setEncoding("UTF8");
        rootEntries = new Vector();
    }
    public void setWhenempty(WhenEmpty we) {
        log("JARs are never empty, they contain at least a manifest file",
            Project.MSG_WARN);
    }
    public void setWhenmanifestonly(WhenEmpty we) {
        emptyBehavior = we.getValue();
    }
    public void setStrict(StrictMode strict) {
        this.strict = strict;
    }
    public void setJarfile(File jarFile) {
        setDestFile(jarFile);
    }
    public void setIndex(boolean flag) {
        index = flag;
    }
    public void setIndexMetaInf(boolean flag) {
        indexMetaInf = flag;
    }
    public void setManifestEncoding(String manifestEncoding) {
        this.manifestEncoding = manifestEncoding;
    }
    public void addConfiguredManifest(Manifest newManifest)
        throws ManifestException {
        if (configuredManifest == null) {
            configuredManifest = newManifest;
        } else {
            configuredManifest.merge(newManifest, false, mergeClassPaths);
        }
        savedConfiguredManifest = configuredManifest;
    }
    public void setManifest(File manifestFile) {
        if (!manifestFile.exists()) {
            throw new BuildException("Manifest file: " + manifestFile
                                     + " does not exist.", getLocation());
        }
        this.manifestFile = manifestFile;
    }
    private Manifest getManifest(File manifestFile) {
        Manifest newManifest = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {
            fis = new FileInputStream(manifestFile);
            if (manifestEncoding == null) {
                isr = new InputStreamReader(fis);
            } else {
                isr = new InputStreamReader(fis, manifestEncoding);
            }
            newManifest = getManifest(isr);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Unsupported encoding while reading manifest: "
                                     + e.getMessage(), e);
        } catch (IOException e) {
            throw new BuildException("Unable to read manifest file: "
                                     + manifestFile
                                     + " (" + e.getMessage() + ")", e);
        } finally {
            FileUtils.close(isr);
        }
        return newManifest;
    }
    private Manifest getManifestFromJar(File jarFile) throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(jarFile);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.getName().equalsIgnoreCase(MANIFEST_NAME)) {
                    InputStreamReader isr =
                        new InputStreamReader(zf.getInputStream(ze), "UTF-8");
                    return getManifest(isr);
                }
            }
            return null;
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                }
            }
        }
    }
    private Manifest getManifest(Reader r) {
        Manifest newManifest = null;
        try {
            newManifest = new Manifest(r);
        } catch (ManifestException e) {
            log("Manifest is invalid: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Invalid Manifest: " + manifestFile,
                                     e, getLocation());
        } catch (IOException e) {
            throw new BuildException("Unable to read manifest file"
                                     + " (" + e.getMessage() + ")", e);
        }
        return newManifest;
    }
    private boolean jarHasIndex(File jarFile) throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(jarFile);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.getName().equalsIgnoreCase(INDEX_NAME)) {
                    return true;
                }
            }
            return false;
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public void setFilesetmanifest(FilesetManifestConfig config) {
        filesetManifestConfig = config;
        mergeManifestsMain = "merge".equals(config.getValue());
        if (filesetManifestConfig != null
            && !filesetManifestConfig.getValue().equals("skip")) {
            doubleFilePass = true;
        }
    }
    public void addMetainf(ZipFileSet fs) {
        fs.setPrefix("META-INF/");
        super.addFileset(fs);
    }
    public void addConfiguredIndexJars(Path p) {
        if (indexJars == null) {
            indexJars = new Path(getProject());
        }
        indexJars.append(p);
    }
    public void addConfiguredService(Service service) {
        service.check();
        serviceList.add(service);
    }
    private void writeServices(ZipOutputStream zOut) throws IOException {
        Iterator serviceIterator;
        Service service;
        serviceIterator = serviceList.iterator();
        while (serviceIterator.hasNext()) {
           service = (Service) serviceIterator.next();
           InputStream is = null;
           try {
               is = service.getAsStream();
               super.zipFile(is, zOut,
                             "META-INF/services/" + service.getType(),
                             System.currentTimeMillis(), null,
                             ZipFileSet.DEFAULT_FILE_MODE);
           } finally {
               FileUtils.close(is);
           }
        }
    }
    public void setMergeClassPathAttributes(boolean b) {
        mergeClassPaths = b;
    }
    public void setFlattenAttributes(boolean b) {
        flattenClassPaths = b;
    }
    protected void initZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {
        if (!skipWriting) {
            Manifest jarManifest = createManifest();
            writeManifest(zOut, jarManifest);
            writeServices(zOut);
        }
    }
    private Manifest createManifest()
        throws BuildException {
        try {
            Manifest finalManifest = Manifest.getDefaultManifest();
            if (manifest == null) {
                if (manifestFile != null) {
                    manifest = getManifest(manifestFile);
                }
            }
            if (isInUpdateMode()) {
                finalManifest.merge(originalManifest, false, mergeClassPaths);
            }
            finalManifest.merge(filesetManifest, false, mergeClassPaths);
            finalManifest.merge(configuredManifest, !mergeManifestsMain,
                                mergeClassPaths);
            finalManifest.merge(manifest, !mergeManifestsMain,
                                mergeClassPaths);
            return finalManifest;
        } catch (ManifestException e) {
            log("Manifest is invalid: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Invalid Manifest", e, getLocation());
        }
    }
    private void writeManifest(ZipOutputStream zOut, Manifest manifest)
        throws IOException {
        for (Enumeration e = manifest.getWarnings();
             e.hasMoreElements();) {
            log("Manifest warning: " + e.nextElement(),
                Project.MSG_WARN);
        }
        zipDir((Resource) null, zOut, "META-INF/", ZipFileSet.DEFAULT_DIR_MODE,
               JAR_MARKER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos, Manifest.JAR_ENCODING);
        PrintWriter writer = new PrintWriter(osw);
        manifest.write(writer, flattenClassPaths);
        if (writer.checkError()) {
            throw new IOException("Encountered an error writing the manifest");
        }
        writer.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        try {
            super.zipFile(bais, zOut, MANIFEST_NAME,
                          System.currentTimeMillis(), null,
                          ZipFileSet.DEFAULT_FILE_MODE);
        } finally {
            FileUtils.close(bais);
        }
        super.initZipOutputStream(zOut);
    }
    protected void finalizeZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {
        if (index) {
            createIndexList(zOut);
        }
    }
    private void createIndexList(ZipOutputStream zOut) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos,
                                                                    "UTF8"));
        writer.println("JarIndex-Version: 1.0");
        writer.println();
        writer.println(zipFile.getName());
        writeIndexLikeList(new ArrayList(addedDirs.keySet()),
                           rootEntries, writer);
        writer.println();
        if (indexJars != null) {
            Manifest mf = createManifest();
            Manifest.Attribute classpath =
                mf.getMainSection().getAttribute(Manifest.ATTRIBUTE_CLASSPATH);
            String[] cpEntries = null;
            if (classpath != null && classpath.getValue() != null) {
                StringTokenizer tok = new StringTokenizer(classpath.getValue(),
                                                          " ");
                cpEntries = new String[tok.countTokens()];
                int c = 0;
                while (tok.hasMoreTokens()) {
                    cpEntries[c++] = tok.nextToken();
                }
            }
            String[] indexJarEntries = indexJars.list();
            for (int i = 0; i < indexJarEntries.length; i++) {
                String name = findJarName(indexJarEntries[i], cpEntries);
                if (name != null) {
                    ArrayList dirs = new ArrayList();
                    ArrayList files = new ArrayList();
                    grabFilesAndDirs(indexJarEntries[i], dirs, files);
                    if (dirs.size() + files.size() > 0) {
                        writer.println(name);
                        writeIndexLikeList(dirs, files, writer);
                        writer.println();
                    }
                }
            }
        }
        if (writer.checkError()) {
            throw new IOException("Encountered an error writing jar index");
        }
        writer.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        try {
            super.zipFile(bais, zOut, INDEX_NAME, System.currentTimeMillis(),
                          null, ZipFileSet.DEFAULT_FILE_MODE);
        } finally {
            FileUtils.close(bais);
        }
    }
    protected void zipFile(InputStream is, ZipOutputStream zOut, String vPath,
                           long lastModified, File fromArchive, int mode)
        throws IOException {
        if (MANIFEST_NAME.equalsIgnoreCase(vPath))  {
            if (isFirstPass()) {
                filesetManifest(fromArchive, is);
            }
        } else if (INDEX_NAME.equalsIgnoreCase(vPath) && index) {
            logWhenWriting("Warning: selected " + archiveType
                           + " files include a " + INDEX_NAME + " which will"
                           + " be replaced by a newly generated one.",
                           Project.MSG_WARN);
        } else {
            if (index && vPath.indexOf("/") == -1) {
                rootEntries.addElement(vPath);
            }
            super.zipFile(is, zOut, vPath, lastModified, fromArchive, mode);
        }
    }
    private void filesetManifest(File file, InputStream is) throws IOException {
        if (manifestFile != null && manifestFile.equals(file)) {
            log("Found manifest " + file, Project.MSG_VERBOSE);
            try {
                if (is != null) {
                    InputStreamReader isr;
                    if (manifestEncoding == null) {
                        isr = new InputStreamReader(is);
                    } else {
                        isr = new InputStreamReader(is, manifestEncoding);
                    }
                    manifest = getManifest(isr);
                } else {
                    manifest = getManifest(file);
                }
            } catch (UnsupportedEncodingException e) {
                throw new BuildException("Unsupported encoding while reading "
                    + "manifest: " + e.getMessage(), e);
            }
        } else if (filesetManifestConfig != null
                    && !filesetManifestConfig.getValue().equals("skip")) {
            logWhenWriting("Found manifest to merge in file " + file,
                           Project.MSG_VERBOSE);
            try {
                Manifest newManifest = null;
                if (is != null) {
                    InputStreamReader isr;
                    if (manifestEncoding == null) {
                        isr = new InputStreamReader(is);
                    } else {
                        isr = new InputStreamReader(is, manifestEncoding);
                    }
                    newManifest = getManifest(isr);
                } else {
                    newManifest = getManifest(file);
                }
                if (filesetManifest == null) {
                    filesetManifest = newManifest;
                } else {
                    filesetManifest.merge(newManifest, false, mergeClassPaths);
                }
            } catch (UnsupportedEncodingException e) {
                throw new BuildException("Unsupported encoding while reading "
                    + "manifest: " + e.getMessage(), e);
            } catch (ManifestException e) {
                log("Manifest in file " + file + " is invalid: "
                    + e.getMessage(), Project.MSG_ERR);
                throw new BuildException("Invalid Manifest", e, getLocation());
            }
        } else {
        }
    }
    protected ArchiveState getResourcesToAdd(ResourceCollection[] rcs,
                                             File zipFile,
                                             boolean needsUpdate)
        throws BuildException {
        if (skipWriting) {
            Resource[][] manifests = grabManifests(rcs);
            int count = 0;
            for (int i = 0; i < manifests.length; i++) {
                count += manifests[i].length;
            }
            log("found a total of " + count + " manifests in "
                + manifests.length + " resource collections",
                Project.MSG_VERBOSE);
            return new ArchiveState(true, manifests);
        }
        if (zipFile.exists()) {
            try {
                originalManifest = getManifestFromJar(zipFile);
                if (originalManifest == null) {
                    log("Updating jar since the current jar has"
                                   + " no manifest", Project.MSG_VERBOSE);
                    needsUpdate = true;
                } else {
                    Manifest mf = createManifest();
                    if (!mf.equals(originalManifest)) {
                        log("Updating jar since jar manifest has"
                                       + " changed", Project.MSG_VERBOSE);
                        needsUpdate = true;
                    }
                }
            } catch (Throwable t) {
                log("error while reading original manifest in file: "
                    + zipFile.toString() + " due to " + t.getMessage(),
                    Project.MSG_WARN);
                needsUpdate = true;
            }
        } else {
            needsUpdate = true;
        }
        createEmpty = needsUpdate;
        if (!needsUpdate && index) {
            try {
                needsUpdate = !jarHasIndex(zipFile);
            } catch (IOException e) {
                needsUpdate = true;
            }
        }
        return super.getResourcesToAdd(rcs, zipFile, needsUpdate);
    }
    protected boolean createEmptyZip(File zipFile) throws BuildException {
        if (!createEmpty) {
            return true;
        }
        if (emptyBehavior.equals("skip")) {
            if (!skipWriting) {
                log("Warning: skipping " + archiveType + " archive "
                    + zipFile + " because no files were included.",
                    Project.MSG_WARN);
            }
            return true;
        } else if (emptyBehavior.equals("fail")) {
            throw new BuildException("Cannot create " + archiveType
                                     + " archive " + zipFile
                                     + ": no files were included.",
                                     getLocation());
        }
        ZipOutputStream zOut = null;
        try {
            if (!skipWriting) {
                log("Building MANIFEST-only jar: "
                    + getDestFile().getAbsolutePath());
            }
            zOut = new ZipOutputStream(new FileOutputStream(getDestFile()));
            zOut.setEncoding(getEncoding());
            if (isCompress()) {
                zOut.setMethod(ZipOutputStream.DEFLATED);
            } else {
                zOut.setMethod(ZipOutputStream.STORED);
            }
            initZipOutputStream(zOut);
            finalizeZipOutputStream(zOut);
        } catch (IOException ioe) {
            throw new BuildException("Could not create almost empty JAR archive"
                                     + " (" + ioe.getMessage() + ")", ioe,
                                     getLocation());
        } finally {
            FileUtils.close(zOut);
            createEmpty = false;
        }
        return true;
    }
    protected void cleanUp() {
        super.cleanUp();
        checkJarSpec();
        if (!doubleFilePass || !skipWriting) {
            manifest = null;
            configuredManifest = savedConfiguredManifest;
            filesetManifest = null;
            originalManifest = null;
        }
        rootEntries.removeAllElements();
    }
    private void checkJarSpec() {
        String br = System.getProperty("line.separator");
        StringBuffer message = new StringBuffer();
        Section mainSection = (configuredManifest == null)
                            ? null
                            : configuredManifest.getMainSection();
        if (mainSection == null) {
            message.append("No Implementation-Title set.");
            message.append("No Implementation-Version set.");
            message.append("No Implementation-Vendor set.");
        } else {
            if (mainSection.getAttribute("Implementation-Title") == null) {
                message.append("No Implementation-Title set.");
            }
            if (mainSection.getAttribute("Implementation-Version") == null) {
                message.append("No Implementation-Version set.");
            }
            if (mainSection.getAttribute("Implementation-Vendor") == null) {
                message.append("No Implementation-Vendor set.");
            }
        }
        if (message.length() > 0) {
            message.append(br);
            message.append("Location: ").append(getLocation());
            message.append(br);
            if (strict.getValue().equalsIgnoreCase("fail")) {
                throw new BuildException(message.toString(), getLocation());
            } else {
                logWhenWriting(message.toString(), strict.getLogLevel());
            }
        }
    }
    public void reset() {
        super.reset();
        emptyBehavior = "create";
        configuredManifest = null;
        filesetManifestConfig = null;
        mergeManifestsMain = false;
        manifestFile = null;
        index = false;
    }
    public static class FilesetManifestConfig extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"skip", "merge", "mergewithoutmain"};
        }
    }
    protected final void writeIndexLikeList(List dirs, List files,
                                            PrintWriter writer)
        throws IOException {
        Collections.sort(dirs);
        Collections.sort(files);
        Iterator iter = dirs.iterator();
        while (iter.hasNext()) {
            String dir = (String) iter.next();
            dir = dir.replace('\\', '/');
            if (dir.startsWith("./")) {
                dir = dir.substring(2);
            }
            while (dir.startsWith("/")) {
                dir = dir.substring(1);
            }
            int pos = dir.lastIndexOf('/');
            if (pos != -1) {
                dir = dir.substring(0, pos);
            }
            if (!indexMetaInf && dir.startsWith("META-INF")) {
                continue;
            }
            writer.println(dir);
        }
        iter = files.iterator();
        while (iter.hasNext()) {
            writer.println(iter.next());
        }
    }
    protected static String findJarName(String fileName,
                                              String[] classpath) {
        if (classpath == null) {
            return (new File(fileName)).getName();
        }
        fileName = fileName.replace(File.separatorChar, '/');
        TreeMap matches = new TreeMap(new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof String && o2 instanceof String) {
                        return ((String) o2).length()
                            - ((String) o1).length();
                    }
                    return 0;
                }
            });
        for (int i = 0; i < classpath.length; i++) {
            if (fileName.endsWith(classpath[i])) {
                matches.put(classpath[i], classpath[i]);
            } else {
                int slash = classpath[i].indexOf("/");
                String candidate = classpath[i];
                while (slash > -1) {
                    candidate = candidate.substring(slash + 1);
                    if (fileName.endsWith(candidate)) {
                        matches.put(candidate, classpath[i]);
                        break;
                    }
                    slash = candidate.indexOf("/");
                }
            }
        }
        return matches.size() == 0
            ? null : (String) matches.get(matches.firstKey());
    }
    protected static void grabFilesAndDirs(String file, List dirs,
                                                 List files)
        throws IOException {
        org.apache.tools.zip.ZipFile zf = null;
        try {
            zf = new org.apache.tools.zip.ZipFile(file, "utf-8");
            Enumeration entries = zf.getEntries();
            HashSet dirSet = new HashSet();
            while (entries.hasMoreElements()) {
                org.apache.tools.zip.ZipEntry ze =
                    (org.apache.tools.zip.ZipEntry) entries.nextElement();
                String name = ze.getName();
                if (ze.isDirectory()) {
                    dirSet.add(name);
                } else if (name.indexOf("/") == -1) {
                    files.add(name);
                } else {
                    dirSet.add(name.substring(0, name.lastIndexOf("/") + 1));
                }
            }
            dirs.addAll(dirSet);
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    private Resource[][] grabManifests(ResourceCollection[] rcs) {
        Resource[][] manifests = new Resource[rcs.length][];
        for (int i = 0; i < rcs.length; i++) {
            Resource[][] resources = null;
            if (rcs[i] instanceof FileSet) {
                resources = grabResources(new FileSet[] {(FileSet) rcs[i]});
            } else {
                resources = grabNonFileSetResources(new ResourceCollection[] {
                        rcs[i]
                    });
            }
            for (int j = 0; j < resources[0].length; j++) {
                String name = resources[0][j].getName().replace('\\', '/');
                if (rcs[i] instanceof ArchiveFileSet) {
                    ArchiveFileSet afs = (ArchiveFileSet) rcs[i];
                    if (!"".equals(afs.getFullpath(getProject()))) {
                        name = afs.getFullpath(getProject());
                    } else if (!"".equals(afs.getPrefix(getProject()))) {
                        String prefix = afs.getPrefix(getProject());
                        if (!prefix.endsWith("/") && !prefix.endsWith("\\")) {
                            prefix += "/";
                        }
                        name = prefix + name;
                    }
                }
                if (name.equalsIgnoreCase(MANIFEST_NAME)) {
                    manifests[i] = new Resource[] {resources[0][j]};
                    break;
                }
            }
            if (manifests[i] == null) {
                manifests[i] = new Resource[0];
            }
        }
        return manifests;
    }
    public static class StrictMode extends EnumeratedAttribute {
        public StrictMode() {
        }
        public StrictMode(String value) {
            setValue(value);
        }
        public String[] getValues() {
            return new String[]{"fail", "warn", "ignore"};
        }
        public int getLogLevel() {
            return (getValue().equals("ignore")) ? Project.MSG_VERBOSE : Project.MSG_WARN;
        }
    }
}
