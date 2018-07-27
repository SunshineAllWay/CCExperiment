package org.apache.cassandra.scheduler;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.RequestSchedulerOptions;
import org.apache.cassandra.utils.Pair;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class RoundRobinScheduler implements IRequestScheduler
{
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinScheduler.class);
    private final NonBlockingHashMap<String, Pair<Integer, SynchronousQueue<Thread>>> queues;
    private static boolean started = false;
    private final Semaphore taskCount;
    private final Semaphore queueSize = new Semaphore(0, false);
    private Integer defaultWeight;
    private Map<String, Integer> weights;
    public RoundRobinScheduler(RequestSchedulerOptions options)
    {
        assert !started;
        defaultWeight = options.default_weight;
        weights = options.weights;
        taskCount = new Semaphore(options.throttle_limit);
        queues = new NonBlockingHashMap<String, Pair<Integer, SynchronousQueue<Thread>>>();
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    schedule();
                }
            }
        };
        Thread scheduler = new Thread(runnable, "REQUEST-SCHEDULER");
        scheduler.start();
        logger.info("Started the RoundRobin Request Scheduler");
        started = true;
    }
    public void queue(Thread t, String id)
    {
        Pair<Integer, SynchronousQueue<Thread>> weightedQueue = getWeightedQueue(id);
        try
        {
            queueSize.release();
            weightedQueue.right.put(t);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Interrupted while queueing requests", e);
        }
    }
    public void release()
    {
        taskCount.release();
    }
    private void schedule()
    {
        int weight;
        SynchronousQueue<Thread> queue;
        Thread t;
        queueSize.acquireUninterruptibly();
        for (Map.Entry<String,Pair<Integer, SynchronousQueue<Thread>>> request : queues.entrySet())
        {
            weight = request.getValue().left;
            queue = request.getValue().right;
            for (int i=0; i<weight; i++)
            {
                t = queue.poll();
                if (t == null)
                    break;
                else
                {
                    taskCount.acquireUninterruptibly();
                    queueSize.acquireUninterruptibly();
                }
            }
        }
        queueSize.release();
    }
    private Pair<Integer, SynchronousQueue<Thread>> getWeightedQueue(String id)
    {
        Pair<Integer, SynchronousQueue<Thread>> weightedQueue = queues.get(id);
        if (weightedQueue != null)
            return weightedQueue;
        Pair<Integer, SynchronousQueue<Thread>> maybenew = new Pair(getWeight(id), new SynchronousQueue<Thread>(true));
        weightedQueue = queues.putIfAbsent(id, maybenew);
        if (weightedQueue == null)
            return maybenew;
        return weightedQueue;
    }
    Semaphore getTaskCount()
    {
        return taskCount;
    }
    private int getWeight(String weightingVar)
    {
        return (weights != null && weights.containsKey(weightingVar))
                ? weights.get(weightingVar)
                : defaultWeight;
    }
}
