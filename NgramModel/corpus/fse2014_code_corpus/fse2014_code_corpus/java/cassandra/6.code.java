package org.apache.cassandra.contrib.utils.service;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.io.util.FileUtils;
public class CassandraServiceDataCleaner {
    public void prepare() throws IOException {
        makeDirsIfNotExist();
        cleanupDataDirectories();
        CommitLog.instance.resetUnsafe();
    }
    public void cleanupDataDirectories() throws IOException {
        for (String s: getDataDirs()) {
            cleanDir(s);
        }
    }
    public void makeDirsIfNotExist() throws IOException {
        DatabaseDescriptor.createAllDirectories();
    }
    private Set<String> getDataDirs() {
        Set<String> dirs = new HashSet<String>();
        for (String s : DatabaseDescriptor.getAllDataFileLocations()) {
            dirs.add(s);
        }
        dirs.add(DatabaseDescriptor.getCommitLogLocation());
        return dirs;
    }
    private void cleanDir(String dir) throws IOException {
        File dirFile = new File(dir);
        if (dirFile.exists() && dirFile.isDirectory()) {
            for (File f : dirFile.listFiles()) {
                FileUtils.deleteRecursive(f);
            }
        }
    }
}
