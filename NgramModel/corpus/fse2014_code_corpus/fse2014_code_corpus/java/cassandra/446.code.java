package org.apache.cassandra;
import java.io.File;
import java.io.IOException;
import org.junit.BeforeClass;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.io.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class CleanupHelper extends SchemaLoader
{
    private static Logger logger = LoggerFactory.getLogger(CleanupHelper.class);
    @BeforeClass
    public static void cleanupAndLeaveDirs() throws IOException
    {
        mkdirs();
        cleanup();
        mkdirs();
        CommitLog.instance.resetUnsafe(); 
    }
    public static void cleanup() throws IOException
    {
        String[] directoryNames = { DatabaseDescriptor.getCommitLogLocation(), };
        for (String dirName : directoryNames)
        {
            File dir = new File(dirName);
            if (!dir.exists())
                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
            FileUtils.deleteRecursive(dir);
        }
        for (String dirName : DatabaseDescriptor.getAllDataFileLocations())
        {
            File dir = new File(dirName);
            if (!dir.exists())
                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
            FileUtils.deleteRecursive(dir);
        }
    }
    public static void mkdirs()
    {
        try
        {
            DatabaseDescriptor.createAllDirectories();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
