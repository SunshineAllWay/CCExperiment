package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
public class Expand extends Task {
    private static final int BUFFER_SIZE = 1024;
    private File dest; 
    private File source; 
    private boolean overwrite = true;
    private Mapper mapperElement = null;
    private Vector patternsets = new Vector();
    private Union resources = new Union();
    private boolean resourcesSpecified = false;
    private boolean failOnEmptyArchive = false;
    private boolean stripAbsolutePathSpec = false;
    private boolean scanForUnicodeExtraFields = true;
    public static final String NATIVE_ENCODING = "native-encoding";
    private String encoding = "UTF8";
    public static final String ERROR_MULTIPLE_MAPPERS = "Cannot define more than one mapper";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public void setFailOnEmptyArchive(boolean b) {
        failOnEmptyArchive = b;
    }
    public boolean getFailOnEmptyArchive() {
        return failOnEmptyArchive;
    }
    public void execute() throws BuildException {
        if ("expand".equals(getTaskType())) {
            log("!! expand is deprecated. Use unzip instead. !!");
        }
        if (source == null && !resourcesSpecified) {
            throw new BuildException("src attribute and/or resources must be "
                                     + "specified");
        }
        if (dest == null) {
            throw new BuildException(
                "Dest attribute must be specified");
        }
        if (dest.exists() && !dest.isDirectory()) {
            throw new BuildException("Dest must be a directory.", getLocation());
        }
        if (source != null) {
            if (source.isDirectory()) {
                throw new BuildException("Src must not be a directory."
                    + " Use nested filesets instead.", getLocation());
            } else if (!source.exists()) {
                throw new BuildException("src '" + source + "' doesn't exist.");
            } else if (!source.canRead()) {
                throw new BuildException("src '" + source + "' cannot be read.");
            } else {
                expandFile(FILE_UTILS, source, dest);
            }
        }
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            Resource r = (Resource) iter.next();
            if (!r.isExists()) {
                log("Skipping '" + r.getName() + "' because it doesn't exist.");
                continue;
            }
            FileProvider fp = (FileProvider) r.as(FileProvider.class);
            if (fp != null) {
                expandFile(FILE_UTILS, fp.getFile(), dest);
            } else {
                expandResource(r, dest);
            }
        }
    }
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        log("Expanding: " + srcF + " into " + dir, Project.MSG_INFO);
        ZipFile zf = null;
        FileNameMapper mapper = getMapper();
        if (!srcF.exists()) {
            throw new BuildException("Unable to expand "
                    + srcF
                    + " as the file does not exist",
                    getLocation());
        }
        try {
            zf = new ZipFile(srcF, encoding, scanForUnicodeExtraFields);
            boolean empty = true;
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                empty = false;
                ZipEntry ze = (ZipEntry) e.nextElement();
                InputStream is = null;
                log("extracting " + ze.getName(), Project.MSG_DEBUG);
                try {
                    extractFile(fileUtils, srcF, dir,
                                is = zf.getInputStream(ze),
                                ze.getName(), new Date(ze.getTime()),
                                ze.isDirectory(), mapper);
                } finally {
                    FileUtils.close(is);
                }
            }
            if (empty && getFailOnEmptyArchive()) {
                throw new BuildException("archive '" + srcF + "' is empty");
            }
            log("expand complete", Project.MSG_VERBOSE);
        } catch (IOException ioe) {
            throw new BuildException(
                "Error while expanding " + srcF.getPath()
                + "\n" + ioe.toString(),
                ioe);
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }
    protected void expandResource(Resource srcR, File dir) {
        throw new BuildException("only filesystem based resources are"
                                 + " supported by this task.");
    }
    protected FileNameMapper getMapper() {
        FileNameMapper mapper = null;
        if (mapperElement != null) {
            mapper = mapperElement.getImplementation();
        } else {
            mapper = new IdentityMapper();
        }
        return mapper;
    }
    protected void extractFile(FileUtils fileUtils, File srcF, File dir,
                               InputStream compressedInputStream,
                               String entryName, Date entryDate,
                               boolean isDirectory, FileNameMapper mapper)
                               throws IOException {
        if (stripAbsolutePathSpec && entryName.length() > 0
            && (entryName.charAt(0) == File.separatorChar
                || entryName.charAt(0) == '/'
                || entryName.charAt(0) == '\\')) {
            log("stripped absolute path spec from " + entryName,
                Project.MSG_VERBOSE);
            entryName = entryName.substring(1);
        }
        if (patternsets != null && patternsets.size() > 0) {
            String name = entryName.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
            boolean included = false;
            Set includePatterns = new HashSet();
            Set excludePatterns = new HashSet();
            for (int v = 0, size = patternsets.size(); v < size; v++) {
                PatternSet p = (PatternSet) patternsets.elementAt(v);
                String[] incls = p.getIncludePatterns(getProject());
                if (incls == null || incls.length == 0) {
                    incls = new String[] {"**"};
                }
                for (int w = 0; w < incls.length; w++) {
                    String pattern = incls[w].replace('/', File.separatorChar)
                        .replace('\\', File.separatorChar);
                    if (pattern.endsWith(File.separator)) {
                        pattern += "**";
                    }
                    includePatterns.add(pattern);
                }
                String[] excls = p.getExcludePatterns(getProject());
                if (excls != null) {
                    for (int w = 0; w < excls.length; w++) {
                        String pattern = excls[w]
                            .replace('/', File.separatorChar)
                            .replace('\\', File.separatorChar);
                        if (pattern.endsWith(File.separator)) {
                            pattern += "**";
                        }
                        excludePatterns.add(pattern);
                    }
                }
            }
            for (Iterator iter = includePatterns.iterator();
                 !included && iter.hasNext();) {
                String pattern = (String) iter.next();
                included = SelectorUtils.matchPath(pattern, name);
            }
            for (Iterator iter = excludePatterns.iterator();
                 included && iter.hasNext();) {
                String pattern = (String) iter.next();
                included = !SelectorUtils.matchPath(pattern, name);
            }
            if (!included) {
                log("skipping " + entryName
                    + " as it is excluded or not included.",
                    Project.MSG_VERBOSE);
                return;
            }
        }
        String[] mappedNames = mapper.mapFileName(entryName);
        if (mappedNames == null || mappedNames.length == 0) {
            mappedNames = new String[] {entryName};
        }
        File f = fileUtils.resolveFile(dir, mappedNames[0]);
        try {
            if (!overwrite && f.exists()
                && f.lastModified() >= entryDate.getTime()) {
                log("Skipping " + f + " as it is up-to-date",
                    Project.MSG_DEBUG);
                return;
            }
            log("expanding " + entryName + " to " + f,
                Project.MSG_VERBOSE);
            File dirF = f.getParentFile();
            if (dirF != null) {
                dirF.mkdirs();
            }
            if (isDirectory) {
                f.mkdirs();
            } else {
                byte[] buffer = new byte[BUFFER_SIZE];
                int length = 0;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    while ((length =
                            compressedInputStream.read(buffer)) >= 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.close();
                    fos = null;
                } finally {
                    FileUtils.close(fos);
                }
            }
            fileUtils.setFileLastModified(f, entryDate.getTime());
        } catch (FileNotFoundException ex) {
            log("Unable to expand to file " + f.getPath(),
                    ex,
                    Project.MSG_WARN);
        }
    }
    public void setDest(File d) {
        this.dest = d;
    }
    public void setSrc(File s) {
        this.source = s;
    }
    public void setOverwrite(boolean b) {
        overwrite = b;
    }
    public void addPatternset(PatternSet set) {
        patternsets.addElement(set);
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void add(ResourceCollection rc) {
        resourcesSpecified = true;
        resources.add(rc);
    }
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException(ERROR_MULTIPLE_MAPPERS,
                                     getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public void setEncoding(String encoding) {
        internalSetEncoding(encoding);
    }
    protected void internalSetEncoding(String encoding) {
        if (NATIVE_ENCODING.equals(encoding)) {
            encoding = null;
        }
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setStripAbsolutePathSpec(boolean b) {
        stripAbsolutePathSpec = b;
    }
    public void setScanForUnicodeExtraFields(boolean b) {
        internalSetScanForUnicodeExtraFields(b);
    }
    protected void internalSetScanForUnicodeExtraFields(boolean b) {
        scanForUnicodeExtraFields = b;
    }
    public boolean getScanForUnicodeExtraFields() {
        return scanForUnicodeExtraFields;
    }
}
