package org.apache.cassandra.concurrent;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
public class AIOExecutorService implements ExecutorService
{
    private ExecutorService executorService_;
    public AIOExecutorService(int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory)
    {
        executorService_ = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);        
    }
    public void execute(Runnable command)
    {
        executorService_.execute(command);
    }
    public void shutdown()
    {    
    }
    public List<Runnable> shutdownNow()
    {
        return executorService_.shutdownNow();
    }
    public boolean isShutdown()
    {
        return executorService_.isShutdown();
    }
    public boolean isTerminated()
    {
        return executorService_.isTerminated();
    }
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        return executorService_.awaitTermination(timeout, unit);
    }
    public <T> Future<T> submit(Callable<T> task)
    {
        return executorService_.submit(task);
    }
    public <T> Future<T> submit(Runnable task, T result)
    {
        return executorService_.submit(task, result);
    }
    public Future<?> submit(Runnable task)
    {
        return executorService_.submit(task);
    }
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        return executorService_.invokeAll(tasks);
    }
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
    {
        return executorService_.invokeAll(tasks, timeout, unit);
    }
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        return executorService_.invokeAny(tasks);
    }
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return executorService_.invokeAny(tasks, timeout, unit);
    }
}
