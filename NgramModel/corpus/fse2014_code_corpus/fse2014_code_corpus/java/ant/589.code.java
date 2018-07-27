package org.apache.tools.ant.types;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.FileResourceIterator;
import org.apache.tools.ant.types.resources.FileProvider;
public abstract class ArchiveScanner extends DirectoryScanner {
    protected File srcFile;
    private Resource src;
    private Resource lastScannedResource;
    private TreeMap fileEntries = new TreeMap();
    private TreeMap dirEntries = new TreeMap();
    private TreeMap matchFileEntries = new TreeMap();
    private TreeMap matchDirEntries = new TreeMap();
    private String encoding;
    private boolean errorOnMissingArchive = true;
    public void setErrorOnMissingArchive(boolean errorOnMissingArchive) {
        this.errorOnMissingArchive = errorOnMissingArchive;
    }
    public void scan() {
        if (src == null || (!src.isExists() && !errorOnMissingArchive)) {
            return;
        }
        super.scan();
    }
    public void setSrc(File srcFile) {
        setSrc(new FileResource(srcFile));
    }
    public void setSrc(Resource src) {
        this.src = src;
        FileProvider fp = (FileProvider) src.as(FileProvider.class);
        if (fp != null) {
            srcFile = fp.getFile();
        }
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String[] getIncludedFiles() {
        if (src == null) {
            return super.getIncludedFiles();
        }
        scanme();
        Set s = matchFileEntries.keySet();
        return (String[]) (s.toArray(new String[s.size()]));
    }
    public int getIncludedFilesCount() {
        if (src == null) {
            return super.getIncludedFilesCount();
        }
        scanme();
        return matchFileEntries.size();
    }
    public String[] getIncludedDirectories() {
        if (src == null) {
            return super.getIncludedDirectories();
        }
        scanme();
        Set s = matchDirEntries.keySet();
        return (String[]) (s.toArray(new String[s.size()]));
    }
    public int getIncludedDirsCount() {
        if (src == null) {
            return super.getIncludedDirsCount();
        }
        scanme();
        return matchDirEntries.size();
    }
     Iterator getResourceFiles(Project project) {
        if (src == null) {
            return new FileResourceIterator(project, getBasedir(), getIncludedFiles());
        }
        scanme();
        return matchFileEntries.values().iterator();
    }
      Iterator getResourceDirectories(Project project) {
        if (src == null) {
            return new FileResourceIterator(project, getBasedir(), getIncludedDirectories());
        }
        scanme();
        return matchDirEntries.values().iterator();
    }
    public void init() {
        if (includes == null) {
            includes = new String[1];
            includes[0] = "**";
        }
        if (excludes == null) {
            excludes = new String[0];
        }
    }
    public boolean match(String path) {
        String vpath = path.replace('/', File.separatorChar).
            replace('\\', File.separatorChar);
        return isIncluded(vpath) && !isExcluded(vpath);
    }
    public Resource getResource(String name) {
        if (src == null) {
            return super.getResource(name);
        }
        if (name.equals("")) {
            return new Resource("", true, Long.MAX_VALUE, true);
        }
        scanme();
        if (fileEntries.containsKey(name)) {
            return (Resource) fileEntries.get(name);
        }
        name = trimSeparator(name);
        if (dirEntries.containsKey(name)) {
            return (Resource) dirEntries.get(name);
        }
        return new Resource(name);
    }
    protected abstract void fillMapsFromArchive(Resource archive,
                                                String encoding,
                                                Map fileEntries,
                                                Map matchFileEntries,
                                                Map dirEntries,
                                                Map matchDirEntries);
    private void scanme() {
        if (!src.isExists() && !errorOnMissingArchive) {
            return;
        }
        Resource thisresource = new Resource(src.getName(),
                                             src.isExists(),
                                             src.getLastModified());
        if (lastScannedResource != null
            && lastScannedResource.getName().equals(thisresource.getName())
            && lastScannedResource.getLastModified()
            == thisresource.getLastModified()) {
            return;
        }
        init();
        fileEntries.clear();
        dirEntries.clear();
        matchFileEntries.clear();
        matchDirEntries.clear();
        fillMapsFromArchive(src, encoding, fileEntries, matchFileEntries,
                            dirEntries, matchDirEntries);
        lastScannedResource = thisresource;
    }
    protected static final String trimSeparator(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
