package org.apache.cassandra.concurrent;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DebuggableThreadPoolExecutor extends ThreadPoolExecutor
{
    protected static Logger logger = LoggerFactory.getLogger(DebuggableThreadPoolExecutor.class);
    public DebuggableThreadPoolExecutor(String threadPoolName, int priority)
    {
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPoolName, priority));
    }
    public DebuggableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        if (maximumPoolSize > 1)
        {
            this.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }
        else
        {
            this.setRejectedExecutionHandler(new RejectedExecutionHandler()
            {
                public void rejectedExecution(Runnable task, ThreadPoolExecutor executor)
                {
                    BlockingQueue<Runnable> queue = executor.getQueue();
                    while (true)
                    {
                        if (executor.isShutdown())
                            throw new RejectedExecutionException("ThreadPoolExecutor has shut down");
                        try
                        {
                            if (queue.offer(task, 1000, TimeUnit.MILLISECONDS))
                                break;
                        }
                        catch (InterruptedException e)
                        {
                            throw new AssertionError(e);    
                        }
                    }
                }
            });
        }
    }
    public void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r,t);
        if (r instanceof FutureTask)
        {
            try
            {
                ((FutureTask) r).get();
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
            catch (ExecutionException e)
            {
                if (Thread.getDefaultUncaughtExceptionHandler() != null)
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e.getCause());
            }
        }
        if (t != null)
        {
            logger.error("Error in ThreadPoolExecutor", t);
        }
    }
}
