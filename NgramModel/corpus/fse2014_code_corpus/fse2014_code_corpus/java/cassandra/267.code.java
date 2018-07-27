package org.apache.cassandra.io;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutor;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.utils.WrappedRunnable;
public class DeletionService
{
    public static final int MAX_RETRIES = 10;
    public static final ExecutorService executor = new JMXEnabledThreadPoolExecutor("FILEUTILS-DELETE-POOL");
    public static void submitDelete(final String file)
    {
        Runnable deleter = new WrappedRunnable()
        {
            @Override
            protected void runMayThrow() throws IOException
            {
                FileUtils.deleteWithConfirm(new File(file));
            }
        };
        executor.submit(deleter);
    }
    public static void waitFor() throws InterruptedException, ExecutionException
    {
        executor.submit(new Runnable() { public void run() { }}).get();
    }
}
