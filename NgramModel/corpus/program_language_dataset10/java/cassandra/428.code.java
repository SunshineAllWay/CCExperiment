package org.apache.cassandra.utils;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.service.StorageService;
public class ResourceWatcher
{
    public static void watch(String resource, Runnable callback, int period)
    {
        StorageService.scheduledTasks.scheduleWithFixedDelay(new WatchedResource(resource, callback), period, period, TimeUnit.MILLISECONDS);
    }
    public static class WatchedResource implements Runnable
    {
        private static Logger logger = LoggerFactory.getLogger(WatchedResource.class);
        private String resource;
        private Runnable callback;
        private long lastLoaded;
        public WatchedResource(String resource, Runnable callback)
        {
            this.resource = resource;
            this.callback = callback;
            lastLoaded = 0;
        }
        public void run()
        {
            try
            {
                String filename = FBUtilities.resourceToFile(resource);
                long lastModified = new File(filename).lastModified();
                if (lastModified > lastLoaded)
                {
                    callback.run();
                    lastLoaded = lastModified;
                }
            }
            catch (Throwable t)
            {
                logger.error(String.format("Timed run of %s failed.", callback.getClass()), t);
            }
        }
    }
}
