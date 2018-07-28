package org.apache.cassandra.io.sstable;
import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.DeletionService;
import org.apache.cassandra.service.StorageService;
public class SSTableDeletingReference extends PhantomReference<SSTableReader>
{
    private static final Logger logger = LoggerFactory.getLogger(SSTableDeletingReference.class);
    public static final int RETRY_DELAY = 10000;
    private final SSTableTracker tracker;
    public final Descriptor desc;
    public final Set<Component> components;
    private final long size;
    private boolean deleteOnCleanup;
    SSTableDeletingReference(SSTableTracker tracker, SSTableReader referent, ReferenceQueue<? super SSTableReader> q)
    {
        super(referent, q);
        this.tracker = tracker;
        this.desc = referent.descriptor;
        this.components = referent.components;
        this.size = referent.bytesOnDisk();
    }
    public void deleteOnCleanup()
    {
        deleteOnCleanup = true;
    }
    public void cleanup() throws IOException
    {
        if (deleteOnCleanup)
        {
            StorageService.scheduledTasks.schedule(new CleanupTask(), RETRY_DELAY, TimeUnit.MILLISECONDS);
        }
    }
    private class CleanupTask implements Runnable
    {
        int attempts = 0;
        public void run()
        {
            File datafile = new File(desc.filenameFor(Component.DATA));
            if (!datafile.delete())
            {
                if (attempts++ < DeletionService.MAX_RETRIES)
                {
                    StorageService.scheduledTasks.schedule(this, RETRY_DELAY, TimeUnit.MILLISECONDS);
                    return;
                }
                else
                {
                    logger.error("Unable to delete " + datafile + " (it will be removed on server restart)");
                    return;
                }
            }
            components.remove(Component.DATA);
            SSTable.delete(desc, components);
            tracker.spaceReclaimed(size);
        }
    }
}
