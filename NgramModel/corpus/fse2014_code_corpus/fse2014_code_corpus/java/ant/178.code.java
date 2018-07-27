package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.ResourceFactory;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.LinkedHashtable;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.FlatFileNameMapper;
public class Copy extends Task {
    private static final String MSG_WHEN_COPYING_EMPTY_RC_TO_FILE =
        "Cannot perform operation from directory to file.";
    static final File NULL_FILE_PLACEHOLDER = new File("/NULL_FILE");
    static final String LINE_SEPARATOR = System.getProperty("line.separator");
    protected File file = null;     
    protected File destFile = null; 
    protected File destDir = null;  
    protected Vector rcs = new Vector();
    protected Vector filesets = rcs;
    private boolean enableMultipleMappings = false;
    protected boolean filtering = false;
    protected boolean preserveLastModified = false;
    protected boolean forceOverwrite = false;
    protected boolean flatten = false;
    protected int verbosity = Project.MSG_VERBOSE;
    protected boolean includeEmpty = true;
    protected boolean failonerror = true;
    protected Hashtable fileCopyMap = new LinkedHashtable();
    protected Hashtable dirCopyMap = new LinkedHashtable();
    protected Hashtable completeDirMap = new LinkedHashtable();
    protected Mapper mapperElement = null;
    protected FileUtils fileUtils;
    private Vector filterChains = new Vector();
    private Vector filterSets = new Vector();
    private String inputEncoding = null;
    private String outputEncoding = null;
    private long granularity = 0;
    private boolean force = false;
    private Resource singleResource = null;
    public Copy() {
        fileUtils = FileUtils.getFileUtils();
        granularity = fileUtils.getFileTimestampGranularity();
    }
    protected FileUtils getFileUtils() {
        return fileUtils;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public void setTofile(File destFile) {
        this.destFile = destFile;
    }
    public void setTodir(File destDir) {
        this.destDir = destDir;
    }
    public FilterChain createFilterChain() {
        FilterChain filterChain = new FilterChain();
        filterChains.addElement(filterChain);
        return filterChain;
    }
    public FilterSet createFilterSet() {
        FilterSet filterSet = new FilterSet();
        filterSets.addElement(filterSet);
        return filterSet;
    }
    public void setPreserveLastModified(String preserve) {
        setPreserveLastModified(Project.toBoolean(preserve));
    }
    public void setPreserveLastModified(boolean preserve) {
        preserveLastModified = preserve;
    }
    public boolean getPreserveLastModified() {
        return preserveLastModified;
    }
    protected Vector getFilterSets() {
        return filterSets;
    }
    protected Vector getFilterChains() {
        return filterChains;
    }
    public void setFiltering(boolean filtering) {
        this.filtering = filtering;
    }
    public void setOverwrite(boolean overwrite) {
        this.forceOverwrite = overwrite;
    }
    public void setForce(boolean f) {
        force = f;
    }
    public boolean getForce() {
        return force;
    }
    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }
    public void setVerbose(boolean verbose) {
        this.verbosity = verbose ? Project.MSG_INFO : Project.MSG_VERBOSE;
    }
    public void setIncludeEmptyDirs(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }
    public void setEnableMultipleMappings(boolean enableMultipleMappings) {
        this.enableMultipleMappings = enableMultipleMappings;
    }
    public boolean isEnableMultipleMapping() {
        return enableMultipleMappings;
    }
    public void setFailOnError(boolean failonerror) {
        this.failonerror = failonerror;
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void add(ResourceCollection res) {
        rcs.add(res);
    }
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public void setEncoding(String encoding) {
        this.inputEncoding = encoding;
        if (outputEncoding == null) {
            outputEncoding = encoding;
        }
    }
    public String getEncoding() {
        return inputEncoding;
    }
    public void setOutputEncoding(String encoding) {
        this.outputEncoding = encoding;
    }
    public String getOutputEncoding() {
        return outputEncoding;
    }
    public void setGranularity(long granularity) {
        this.granularity = granularity;
    }
    public void execute() throws BuildException {
        File savedFile = file; 
        File savedDestFile = destFile;
        File savedDestDir = destDir;
        ResourceCollection savedRc = null;
        if (file == null && destFile != null && rcs.size() == 1) {
            savedRc = (ResourceCollection) rcs.elementAt(0);
        }
        try {
            try {
                validateAttributes();
            } catch (BuildException e) {
                if (failonerror
                    || !getMessage(e)
                    .equals(MSG_WHEN_COPYING_EMPTY_RC_TO_FILE)) {
                    throw e;
                } else {
                    log("Warning: " + getMessage(e), Project.MSG_ERR);
                    return;
                }
            }
            copySingleFile();
            HashMap filesByBasedir = new HashMap();
            HashMap dirsByBasedir = new HashMap();
            HashSet baseDirs = new HashSet();
            ArrayList nonFileResources = new ArrayList();
            for (int i = 0; i < rcs.size(); i++) {
                ResourceCollection rc = (ResourceCollection) rcs.elementAt(i);
                if (rc instanceof FileSet && rc.isFilesystemOnly()) {
                    FileSet fs = (FileSet) rc;
                    DirectoryScanner ds = null;
                    try {
                        ds = fs.getDirectoryScanner(getProject());
                    } catch (BuildException e) {
                        if (failonerror
                            || !getMessage(e).endsWith(DirectoryScanner
                                                       .DOES_NOT_EXIST_POSTFIX)) {
                            throw e;
                        } else {
                            log("Warning: " + getMessage(e), Project.MSG_ERR);
                            continue;
                        }
                    }
                    File fromDir = fs.getDir(getProject());
                    String[] srcFiles = ds.getIncludedFiles();
                    String[] srcDirs = ds.getIncludedDirectories();
                    if (!flatten && mapperElement == null
                        && ds.isEverythingIncluded() && !fs.hasPatterns()) {
                        completeDirMap.put(fromDir, destDir);
                    }
                    add(fromDir, srcFiles, filesByBasedir);
                    add(fromDir, srcDirs, dirsByBasedir);
                    baseDirs.add(fromDir);
                } else { 
                    if (!rc.isFilesystemOnly() && !supportsNonFileResources()) {
                        throw new BuildException(
                                   "Only FileSystem resources are supported.");
                    }
                    Iterator resources = rc.iterator();
                    while (resources.hasNext()) {
                        Resource r = (Resource) resources.next();
                        if (!r.isExists()) {
                            String message = "Warning: Could not find resource "
                                + r.toLongString() + " to copy.";
                            if (!failonerror) {
                                log(message, Project.MSG_ERR);
                            } else {
                                throw new BuildException(message);
                            }
                            continue;
                        }
                        File baseDir = NULL_FILE_PLACEHOLDER;
                        String name = r.getName();
                        FileProvider fp = (FileProvider) r.as(FileProvider.class);
                        if (fp != null) {
                            FileResource fr = ResourceUtils.asFileResource(fp);
                            baseDir = getKeyFile(fr.getBaseDir());
                            if (fr.getBaseDir() == null) {
                                name = fr.getFile().getAbsolutePath();
                            }
                        }
                        if (r.isDirectory() || fp != null) {
                            add(baseDir, name,
                                r.isDirectory() ? dirsByBasedir
                                                : filesByBasedir);
                            baseDirs.add(baseDir);
                        } else { 
                            nonFileResources.add(r);
                        }
                    }
                }
            }
            iterateOverBaseDirs(baseDirs, dirsByBasedir, filesByBasedir);
            try {
                doFileOperations();
            } catch (BuildException e) {
                if (!failonerror) {
                    log("Warning: " + getMessage(e), Project.MSG_ERR);
                } else {
                    throw e;
                }
            }
            if (nonFileResources.size() > 0 || singleResource != null) {
                Resource[] nonFiles =
                    (Resource[]) nonFileResources.toArray(new Resource[nonFileResources.size()]);
                Map map = scan(nonFiles, destDir);
                if (singleResource != null) {
                    map.put(singleResource,
                            new String[] { destFile.getAbsolutePath() });
                }
                try {
                    doResourceOperations(map);
                } catch (BuildException e) {
                    if (!failonerror) {
                        log("Warning: " + getMessage(e), Project.MSG_ERR);
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            singleResource = null;
            file = savedFile;
            destFile = savedDestFile;
            destDir = savedDestDir;
            if (savedRc != null) {
                rcs.insertElementAt(savedRc, 0);
            }
            fileCopyMap.clear();
            dirCopyMap.clear();
            completeDirMap.clear();
        }
    }
    private void copySingleFile() {
        if (file != null) {
            if (file.exists()) {
                if (destFile == null) {
                    destFile = new File(destDir, file.getName());
                }
                if (forceOverwrite || !destFile.exists()
                    || (file.lastModified() - granularity
                        > destFile.lastModified())) {
                    fileCopyMap.put(file.getAbsolutePath(),
                                    new String[] {destFile.getAbsolutePath()});
                } else {
                    log(file + " omitted as " + destFile
                        + " is up to date.", Project.MSG_VERBOSE);
                }
            } else {
                String message = "Warning: Could not find file "
                    + file.getAbsolutePath() + " to copy.";
                if (!failonerror) {
                    log(message, Project.MSG_ERR);
                } else {
                    throw new BuildException(message);
                }
            }
        }
    }
    private void iterateOverBaseDirs(
        HashSet baseDirs, HashMap dirsByBasedir, HashMap filesByBasedir) {
        Iterator iter = baseDirs.iterator();
        while (iter.hasNext()) {
            File f = (File) iter.next();
            List files = (List) filesByBasedir.get(f);
            List dirs = (List) dirsByBasedir.get(f);
            String[] srcFiles = new String[0];
            if (files != null) {
                srcFiles = (String[]) files.toArray(srcFiles);
            }
            String[] srcDirs = new String[0];
            if (dirs != null) {
                srcDirs = (String[]) dirs.toArray(srcDirs);
            }
            scan(f == NULL_FILE_PLACEHOLDER ? null : f, destDir, srcFiles,
                 srcDirs);
        }
    }
    protected void validateAttributes() throws BuildException {
        if (file == null && rcs.size() == 0) {
            throw new BuildException(
                "Specify at least one source--a file or a resource collection.");
        }
        if (destFile != null && destDir != null) {
            throw new BuildException(
                "Only one of tofile and todir may be set.");
        }
        if (destFile == null && destDir == null) {
            throw new BuildException("One of tofile or todir must be set.");
        }
        if (file != null && file.isDirectory()) {
            throw new BuildException("Use a resource collection to copy directories.");
        }
        if (destFile != null && rcs.size() > 0) {
            if (rcs.size() > 1) {
                throw new BuildException(
                    "Cannot concatenate multiple files into a single file.");
            } else {
                ResourceCollection rc = (ResourceCollection) rcs.elementAt(0);
                if (!rc.isFilesystemOnly() && !supportsNonFileResources()) {
                    throw new BuildException("Only FileSystem resources are"
                                             + " supported.");
                }
                if (rc.size() == 0) {
                    throw new BuildException(MSG_WHEN_COPYING_EMPTY_RC_TO_FILE);
                } else if (rc.size() == 1) {
                    Resource res = (Resource) rc.iterator().next();
                    FileProvider r = (FileProvider) res.as(FileProvider.class);
                    if (file == null) {
                        if (r != null) {
                            file = r.getFile();
                        } else {
                            singleResource = res;
                        }
                        rcs.removeElementAt(0);
                    } else {
                        throw new BuildException(
                            "Cannot concatenate multiple files into a single file.");
                    }
                } else {
                    throw new BuildException(
                        "Cannot concatenate multiple files into a single file.");
                }
            }
        }
        if (destFile != null) {
            destDir = destFile.getParentFile();
        }
    }
    protected void scan(File fromDir, File toDir, String[] files,
                        String[] dirs) {
        FileNameMapper mapper = getMapper();
        buildMap(fromDir, toDir, files, mapper, fileCopyMap);
        if (includeEmpty) {
            buildMap(fromDir, toDir, dirs, mapper, dirCopyMap);
        }
    }
    protected Map scan(Resource[] fromResources, File toDir) {
        return buildMap(fromResources, toDir, getMapper());
    }
    protected void buildMap(File fromDir, File toDir, String[] names,
                            FileNameMapper mapper, Hashtable map) {
        String[] toCopy = null;
        if (forceOverwrite) {
            Vector v = new Vector();
            for (int i = 0; i < names.length; i++) {
                if (mapper.mapFileName(names[i]) != null) {
                    v.addElement(names[i]);
                }
            }
            toCopy = new String[v.size()];
            v.copyInto(toCopy);
        } else {
            SourceFileScanner ds = new SourceFileScanner(this);
            toCopy = ds.restrict(names, fromDir, toDir, mapper, granularity);
        }
        for (int i = 0; i < toCopy.length; i++) {
            File src = new File(fromDir, toCopy[i]);
            String[] mappedFiles = mapper.mapFileName(toCopy[i]);
            if (!enableMultipleMappings) {
                map.put(src.getAbsolutePath(),
                        new String[] {new File(toDir, mappedFiles[0]).getAbsolutePath()});
            } else {
                for (int k = 0; k < mappedFiles.length; k++) {
                    mappedFiles[k] = new File(toDir, mappedFiles[k]).getAbsolutePath();
                }
                map.put(src.getAbsolutePath(), mappedFiles);
            }
        }
    }
    protected Map buildMap(Resource[] fromResources, final File toDir,
                           FileNameMapper mapper) {
        HashMap map = new HashMap();
        Resource[] toCopy = null;
        if (forceOverwrite) {
            Vector v = new Vector();
            for (int i = 0; i < fromResources.length; i++) {
                if (mapper.mapFileName(fromResources[i].getName()) != null) {
                    v.addElement(fromResources[i]);
                }
            }
            toCopy = new Resource[v.size()];
            v.copyInto(toCopy);
        } else {
            toCopy =
                ResourceUtils.selectOutOfDateSources(this, fromResources,
                                                     mapper,
                                                     new ResourceFactory() {
                           public Resource getResource(String name) {
                               return new FileResource(toDir, name);
                           }
                                                     },
                                                     granularity);
        }
        for (int i = 0; i < toCopy.length; i++) {
            String[] mappedFiles = mapper.mapFileName(toCopy[i].getName());
            for (int j = 0; j < mappedFiles.length; j++) {
                if (mappedFiles[j] == null) {
                    throw new BuildException("Can't copy a resource without a"
                                             + " name if the mapper doesn't"
                                             + " provide one.");
                }
            }
            if (!enableMultipleMappings) {
                map.put(toCopy[i],
                        new String[] {new File(toDir, mappedFiles[0]).getAbsolutePath()});
            } else {
                for (int k = 0; k < mappedFiles.length; k++) {
                    mappedFiles[k] = new File(toDir, mappedFiles[k]).getAbsolutePath();
                }
                map.put(toCopy[i], mappedFiles);
            }
        }
        return map;
    }
    protected void doFileOperations() {
        if (fileCopyMap.size() > 0) {
            log("Copying " + fileCopyMap.size()
                + " file" + (fileCopyMap.size() == 1 ? "" : "s")
                + " to " + destDir.getAbsolutePath());
            Enumeration e = fileCopyMap.keys();
            while (e.hasMoreElements()) {
                String fromFile = (String) e.nextElement();
                String[] toFiles = (String[]) fileCopyMap.get(fromFile);
                for (int i = 0; i < toFiles.length; i++) {
                    String toFile = toFiles[i];
                    if (fromFile.equals(toFile)) {
                        log("Skipping self-copy of " + fromFile, verbosity);
                        continue;
                    }
                    try {
                        log("Copying " + fromFile + " to " + toFile, verbosity);
                        FilterSetCollection executionFilters =
                            new FilterSetCollection();
                        if (filtering) {
                            executionFilters
                                .addFilterSet(getProject().getGlobalFilterSet());
                        }
                        for (Enumeration filterEnum = filterSets.elements();
                            filterEnum.hasMoreElements();) {
                            executionFilters
                                .addFilterSet((FilterSet) filterEnum.nextElement());
                        }
                        fileUtils.copyFile(new File(fromFile), new File(toFile),
                                           executionFilters,
                                           filterChains, forceOverwrite,
                                           preserveLastModified,
                                            false, inputEncoding,
                                           outputEncoding, getProject(),
                                           getForce());
                    } catch (IOException ioe) {
                        String msg = "Failed to copy " + fromFile + " to " + toFile
                            + " due to " + getDueTo(ioe);
                        File targetFile = new File(toFile);
                        if (targetFile.exists() && !targetFile.delete()) {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        if (failonerror) {
                            throw new BuildException(msg, ioe, getLocation());
                        }
                        log(msg, Project.MSG_ERR);
                    }
                }
            }
        }
        if (includeEmpty) {
            Enumeration e = dirCopyMap.elements();
            int createCount = 0;
            while (e.hasMoreElements()) {
                String[] dirs = (String[]) e.nextElement();
                for (int i = 0; i < dirs.length; i++) {
                    File d = new File(dirs[i]);
                    if (!d.exists()) {
                        if (!d.mkdirs()) {
                            log("Unable to create directory "
                                + d.getAbsolutePath(), Project.MSG_ERR);
                        } else {
                            createCount++;
                        }
                    }
                }
            }
            if (createCount > 0) {
                log("Copied " + dirCopyMap.size()
                    + " empty director"
                    + (dirCopyMap.size() == 1 ? "y" : "ies")
                    + " to " + createCount
                    + " empty director"
                    + (createCount == 1 ? "y" : "ies") + " under "
                    + destDir.getAbsolutePath());
            }
        }
    }
    protected void doResourceOperations(Map map) {
        if (map.size() > 0) {
            log("Copying " + map.size()
                + " resource" + (map.size() == 1 ? "" : "s")
                + " to " + destDir.getAbsolutePath());
            Iterator iter = map.keySet().iterator();
            while (iter.hasNext()) {
                Resource fromResource = (Resource) iter.next();
                String[] toFiles = (String[]) map.get(fromResource);
                for (int i = 0; i < toFiles.length; i++) {
                    String toFile = toFiles[i];
                    try {
                        log("Copying " + fromResource + " to " + toFile,
                            verbosity);
                        FilterSetCollection executionFilters =
                            new FilterSetCollection();
                        if (filtering) {
                            executionFilters
                                .addFilterSet(getProject().getGlobalFilterSet());
                        }
                        for (Enumeration filterEnum = filterSets.elements();
                            filterEnum.hasMoreElements();) {
                            executionFilters
                                .addFilterSet((FilterSet) filterEnum.nextElement());
                        }
                        ResourceUtils.copyResource(fromResource,
                                                   new FileResource(destDir,
                                                                    toFile),
                                                   executionFilters,
                                                   filterChains,
                                                   forceOverwrite,
                                                   preserveLastModified,
                                                    false,
                                                   inputEncoding,
                                                   outputEncoding,
                                                   getProject(),
                                                   getForce());
                    } catch (IOException ioe) {
                        String msg = "Failed to copy " + fromResource
                            + " to " + toFile
                            + " due to " + getDueTo(ioe);
                        File targetFile = new File(toFile);
                        if (targetFile.exists() && !targetFile.delete()) {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        if (failonerror) {
                            throw new BuildException(msg, ioe, getLocation());
                        }
                        log(msg, Project.MSG_ERR);
                    }
                }
            }
        }
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(Copy.class);
    }
    private static void add(File baseDir, String[] names, Map m) {
        if (names != null) {
            baseDir = getKeyFile(baseDir);
            List l = (List) m.get(baseDir);
            if (l == null) {
                l = new ArrayList(names.length);
                m.put(baseDir, l);
            }
            l.addAll(java.util.Arrays.asList(names));
        }
    }
    private static void add(File baseDir, String name, Map m) {
        if (name != null) {
            add(baseDir, new String[] {name}, m);
        }
    }
    private static File getKeyFile(File f) {
        return f == null ? NULL_FILE_PLACEHOLDER : f;
    }
    private FileNameMapper getMapper() {
        FileNameMapper mapper = null;
        if (mapperElement != null) {
            mapper = mapperElement.getImplementation();
        } else if (flatten) {
            mapper = new FlatFileNameMapper();
        } else {
            mapper = new IdentityMapper();
        }
        return mapper;
    }
    private String getMessage(Exception ex) {
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }
    private String getDueTo(Exception ex) {
        boolean baseIOException = ex.getClass() == IOException.class;
        StringBuffer message = new StringBuffer();
        if (!baseIOException || ex.getMessage() == null) {
            message.append(ex.getClass().getName());
        }
        if (ex.getMessage() != null) {
            if (!baseIOException) {
                message.append(" ");
            }
            message.append(ex.getMessage());
        }
        if (ex.getClass().getName().indexOf("MalformedInput") != -1) {
            message.append(LINE_SEPARATOR);
            message.append(
                "This is normally due to the input file containing invalid");
             message.append(LINE_SEPARATOR);
            message.append("bytes for the character encoding used : ");
            message.append(
                (inputEncoding == null
                 ? fileUtils.getDefaultEncoding() : inputEncoding));
            message.append(LINE_SEPARATOR);
        }
        return message.toString();
    }
}
