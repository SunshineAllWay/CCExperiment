package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Restrict;
import org.apache.tools.ant.types.resources.selectors.Exists;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.NoneSelector;
public class Sync extends Task {
    private MyCopy myCopy;
    private SyncTarget syncTarget;
    private Restrict resources = null;
    public void init()
        throws BuildException {
        myCopy = new MyCopy();
        configureTask(myCopy);
        myCopy.setFiltering(false);
        myCopy.setIncludeEmptyDirs(false);
        myCopy.setPreserveLastModified(true);
    }
    private void configureTask(Task helper) {
        helper.setProject(getProject());
        helper.setTaskName(getTaskName());
        helper.setOwningTarget(getOwningTarget());
        helper.init();
    }
    public void execute()
        throws BuildException {
        File toDir = myCopy.getToDir();
        Set allFiles = myCopy.nonOrphans;
        boolean noRemovalNecessary = !toDir.exists() || toDir.list().length < 1;
        log("PASS#1: Copying files to " + toDir, Project.MSG_DEBUG);
        myCopy.execute();
        if (noRemovalNecessary) {
            log("NO removing necessary in " + toDir, Project.MSG_DEBUG);
            return; 
        }
        Set preservedDirectories = new LinkedHashSet();
        log("PASS#2: Removing orphan files from " + toDir, Project.MSG_DEBUG);
        int[] removedFileCount = removeOrphanFiles(allFiles, toDir,
                                                   preservedDirectories);
        logRemovedCount(removedFileCount[0], "dangling director", "y", "ies");
        logRemovedCount(removedFileCount[1], "dangling file", "", "s");
        if (!myCopy.getIncludeEmptyDirs()
            || getExplicitPreserveEmptyDirs() == Boolean.FALSE) {
            log("PASS#3: Removing empty directories from " + toDir,
                Project.MSG_DEBUG);
            int removedDirCount = 0;
            if (!myCopy.getIncludeEmptyDirs()) {
                removedDirCount =
                    removeEmptyDirectories(toDir, false, preservedDirectories);
            } else { 
                removedDirCount =
                    removeEmptyDirectories(preservedDirectories);
            }
            logRemovedCount(removedDirCount, "empty director", "y", "ies");
        }
    }
    private void logRemovedCount(int count, String prefix,
                                 String singularSuffix, String pluralSuffix) {
        File toDir = myCopy.getToDir();
        String what = (prefix == null) ? "" : prefix;
        what += (count < 2) ? singularSuffix : pluralSuffix;
        if (count > 0) {
            log("Removed " + count + " " + what + " from " + toDir,
                Project.MSG_INFO);
        } else {
            log("NO " + what + " to remove from " + toDir,
                Project.MSG_VERBOSE);
        }
    }
    private int[] removeOrphanFiles(Set nonOrphans, File toDir,
                                    Set preservedDirectories) {
        int[] removedCount = new int[] {0, 0};
        String[] excls =
            (String[]) nonOrphans.toArray(new String[nonOrphans.size() + 1]);
        excls[nonOrphans.size()] = "";
        DirectoryScanner ds = null;
        if (syncTarget != null) {
            FileSet fs = syncTarget.toFileSet(false);
            fs.setDir(toDir);
            PatternSet ps = syncTarget.mergePatterns(getProject());
            fs.appendExcludes(ps.getIncludePatterns(getProject()));
            fs.appendIncludes(ps.getExcludePatterns(getProject()));
            fs.setDefaultexcludes(!syncTarget.getDefaultexcludes());
            FileSelector[] s = syncTarget.getSelectors(getProject());
            if (s.length > 0) {
                NoneSelector ns = new NoneSelector();
                for (int i = 0; i < s.length; i++) {
                    ns.appendSelector(s[i]);
                }
                fs.appendSelector(ns);
            }
            ds = fs.getDirectoryScanner(getProject());
        } else {
            ds = new DirectoryScanner();
            ds.setBasedir(toDir);
        }
        ds.addExcludes(excls);
        ds.scan();
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            File f = new File(toDir, files[i]);
            log("Removing orphan file: " + f, Project.MSG_DEBUG);
            f.delete();
            ++removedCount[1];
        }
        String[] dirs = ds.getIncludedDirectories();
        for (int i = dirs.length - 1; i >= 0; --i) {
            File f = new File(toDir, dirs[i]);
            String[] children = f.list();
            if (children == null || children.length < 1) {
                log("Removing orphan directory: " + f, Project.MSG_DEBUG);
                f.delete();
                ++removedCount[0];
            }
        }
        Boolean ped = getExplicitPreserveEmptyDirs();
        if (ped != null && ped.booleanValue() != myCopy.getIncludeEmptyDirs()) {
            FileSet fs = syncTarget.toFileSet(true);
            fs.setDir(toDir);
            String[] preservedDirs =
                fs.getDirectoryScanner(getProject()).getIncludedDirectories();
            for (int i = preservedDirs.length - 1; i >= 0; --i) {
                preservedDirectories.add(new File(toDir, preservedDirs[i]));
            }
        }
        return removedCount;
    }
    private int removeEmptyDirectories(File dir, boolean removeIfEmpty,
                                       Set preservedEmptyDirectories) {
        int removedCount = 0;
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; ++i) {
                File file = children[i];
                if (file.isDirectory()) {
                    removedCount +=
                        removeEmptyDirectories(file, true,
                                               preservedEmptyDirectories);
                }
            }
            if (children.length > 0) {
                children = dir.listFiles();
            }
            if (children.length < 1 && removeIfEmpty
                && !preservedEmptyDirectories.contains(dir)) {
                log("Removing empty directory: " + dir, Project.MSG_DEBUG);
                dir.delete();
                ++removedCount;
            }
        }
        return removedCount;
    }
    private int removeEmptyDirectories(Set preservedEmptyDirectories) {
        int removedCount = 0;
        for (Iterator iter = preservedEmptyDirectories.iterator();
             iter.hasNext(); ) {
            File f = (File) iter.next();
            String[] s = f.list();
            if (s == null || s.length == 0) {
                log("Removing empty directory: " + f, Project.MSG_DEBUG);
                f.delete();
                ++removedCount;
            }
        }
        return removedCount;
    }
    public void setTodir(File destDir) {
        myCopy.setTodir(destDir);
    }
    public void setVerbose(boolean verbose) {
        myCopy.setVerbose(verbose);
    }
    public void setOverwrite(boolean overwrite) {
        myCopy.setOverwrite(overwrite);
    }
    public void setIncludeEmptyDirs(boolean includeEmpty) {
        myCopy.setIncludeEmptyDirs(includeEmpty);
    }
    public void setFailOnError(boolean failonerror) {
        myCopy.setFailOnError(failonerror);
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void add(ResourceCollection rc) {
        if (rc instanceof FileSet && rc.isFilesystemOnly()) {
            myCopy.add(rc);
        } else {
            if (resources == null) {
                resources = new Restrict();
                resources.add(new Exists());
                myCopy.add(resources);
            }
            resources.add(rc);
        }
    }
    public void setGranularity(long granularity) {
        myCopy.setGranularity(granularity);
    }
    public void addPreserveInTarget(SyncTarget s) {
        if (syncTarget != null) {
            throw new BuildException("you must not specify multiple "
                                     + "preserveintarget elements.");
        }
        syncTarget = s;
    }
    private Boolean getExplicitPreserveEmptyDirs() {
        return syncTarget == null ? null : syncTarget.getPreserveEmptyDirs();
    }
    public static class MyCopy extends Copy {
        private Set nonOrphans = new HashSet();
        public MyCopy() {
        }
        protected void scan(File fromDir, File toDir, String[] files,
                            String[] dirs) {
            assertTrue("No mapper", mapperElement == null);
            super.scan(fromDir, toDir, files, dirs);
            for (int i = 0; i < files.length; ++i) {
                nonOrphans.add(files[i]);
            }
            for (int i = 0; i < dirs.length; ++i) {
                nonOrphans.add(dirs[i]);
            }
        }
        protected Map scan(Resource[] resources, File toDir) {
            assertTrue("No mapper", mapperElement == null);
            Map m = super.scan(resources, toDir);
            Iterator iter = m.keySet().iterator();
            while (iter.hasNext()) {
                nonOrphans.add(((Resource) iter.next()).getName());
            }
            return m;
        }
        public File getToDir() {
            return destDir;
        }
        public boolean getIncludeEmptyDirs() {
            return includeEmpty;
        }
        protected boolean supportsNonFileResources() {
            return true;
        }
    }
    public static class SyncTarget extends AbstractFileSet {
        private Boolean preserveEmptyDirs;
        public SyncTarget() {
            super();
        }
        public void setDir(File dir) throws BuildException {
            throw new BuildException("preserveintarget doesn't support the dir "
                                     + "attribute");
        }
        public void setPreserveEmptyDirs(boolean b) {
            preserveEmptyDirs = Boolean.valueOf(b);
        }
        public Boolean getPreserveEmptyDirs() {
            return preserveEmptyDirs;
        }
        private FileSet toFileSet(boolean withPatterns) {
            FileSet fs = new FileSet();
            fs.setCaseSensitive(isCaseSensitive());
            fs.setFollowSymlinks(isFollowSymlinks());
            fs.setMaxLevelsOfSymlinks(getMaxLevelsOfSymlinks());
            fs.setProject(getProject());
            if (withPatterns) {
                PatternSet ps = mergePatterns(getProject());
                fs.appendIncludes(ps.getIncludePatterns(getProject()));
                fs.appendExcludes(ps.getExcludePatterns(getProject()));
                for (Enumeration e = selectorElements(); e.hasMoreElements(); ) {
                    fs.appendSelector((FileSelector) e.nextElement());
                }
                fs.setDefaultexcludes(getDefaultexcludes());
            }
            return fs;
        }
    }
    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new BuildException("Assertion Error: " + message);
        }
    }
}
