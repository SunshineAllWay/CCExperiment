package org.apache.cassandra.db.commitlog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.utils.WrappedRunnable;
class BatchCommitLogExecutorService extends AbstractCommitLogExecutorService implements ICommitLogExecutorService, BatchCommitLogExecutorServiceMBean
{
    private final BlockingQueue<CheaterFutureTask> queue;
    public BatchCommitLogExecutorService()
    {
        this(DatabaseDescriptor.getConcurrentWriters());
    }
    public BatchCommitLogExecutorService(int queueSize)
    {
        queue = new LinkedBlockingQueue<CheaterFutureTask>(queueSize);
        Runnable runnable = new WrappedRunnable()
        {
            public void runMayThrow() throws Exception
            {
                while (true)
                {
                    processWithSyncBatch();
                    completedTaskCount++;
                }
            }
        };
        new Thread(runnable, "COMMIT-LOG-WRITER").start();
        registerMBean(this);
    }
    public long getPendingTasks()
    {
        return queue.size();
    }
    private final ArrayList<CheaterFutureTask> incompleteTasks = new ArrayList<CheaterFutureTask>();
    private final ArrayList taskValues = new ArrayList(); 
    private void processWithSyncBatch() throws Exception
    {
        CheaterFutureTask firstTask = queue.take();
        if (!(firstTask.getRawCallable() instanceof CommitLog.LogRecordAdder))
        {
            firstTask.run();
            return;
        }
        incompleteTasks.clear();
        taskValues.clear();
        long end = System.nanoTime() + (long)(1000000 * DatabaseDescriptor.getCommitLogSyncBatchWindow());
        incompleteTasks.add(firstTask);
        taskValues.add(firstTask.getRawCallable().call());
        while (!queue.isEmpty()
               && queue.peek().getRawCallable() instanceof CommitLog.LogRecordAdder
               && System.nanoTime() < end)
        {
            CheaterFutureTask task = queue.remove();
            incompleteTasks.add(task);
            taskValues.add(task.getRawCallable().call());
        }
        try
        {
            CommitLog.instance.sync();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < incompleteTasks.size(); i++)
        {
            incompleteTasks.get(i).set(taskValues.get(i));
        }
    }
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
    {
        return newTaskFor(Executors.callable(runnable, value));
    }
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
    {
        return new CheaterFutureTask(callable);
    }
    public void execute(Runnable command)
    {
        try
        {
            queue.put((CheaterFutureTask)command);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void add(CommitLog.LogRecordAdder adder)
    {
        try
        {
            submit((Callable)adder).get();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static class CheaterFutureTask<V> extends FutureTask<V>
    {
        private final Callable rawCallable;
        public CheaterFutureTask(Callable<V> callable)
        {
            super(callable);
            rawCallable = callable;
        }
        public Callable getRawCallable()
        {
            return rawCallable;
        }
        @Override
        public void set(V v)
        {
            super.set(v);
        }
    }
}
