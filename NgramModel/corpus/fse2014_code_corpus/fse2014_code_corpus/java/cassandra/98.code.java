package org.apache.cassandra.concurrent;
import java.util.EnumMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static org.apache.cassandra.config.DatabaseDescriptor.*;
public class StageManager
{
    private static EnumMap<Stage, ThreadPoolExecutor> stages = new EnumMap<Stage, ThreadPoolExecutor>(Stage.class);
    public static final long KEEPALIVE = 60; 
    static
    {
        stages.put(Stage.MUTATION, multiThreadedConfigurableStage(Stage.MUTATION, getConcurrentWriters()));
        stages.put(Stage.READ, multiThreadedConfigurableStage(Stage.READ, getConcurrentReaders()));        
        stages.put(Stage.REQUEST_RESPONSE, multiThreadedStage(Stage.REQUEST_RESPONSE, Math.max(2, Runtime.getRuntime().availableProcessors())));
        stages.put(Stage.INTERNAL_RESPONSE, multiThreadedStage(Stage.INTERNAL_RESPONSE, Math.max(2, Runtime.getRuntime().availableProcessors())));
        stages.put(Stage.REPLICATE_ON_WRITE, multiThreadedConfigurableStage(Stage.REPLICATE_ON_WRITE, getConcurrentReplicators()));
        stages.put(Stage.STREAM, new JMXEnabledThreadPoolExecutor(Stage.STREAM));
        stages.put(Stage.GOSSIP, new JMXEnabledThreadPoolExecutor(Stage.GOSSIP));
        stages.put(Stage.ANTI_ENTROPY, new JMXEnabledThreadPoolExecutor(Stage.ANTI_ENTROPY));
        stages.put(Stage.MIGRATION, new JMXEnabledThreadPoolExecutor(Stage.MIGRATION));
        stages.put(Stage.MISC, new JMXEnabledThreadPoolExecutor(Stage.MISC));
    }
    private static ThreadPoolExecutor multiThreadedStage(Stage stage, int numThreads)
    {
        assert numThreads > 1 : "multi-threaded stages must have at least 2 threads";
        return new JMXEnabledThreadPoolExecutor(numThreads,
                                                numThreads,
                                                KEEPALIVE,
                                                TimeUnit.SECONDS,
                                                new LinkedBlockingQueue<Runnable>(),
                                                new NamedThreadFactory(stage.getJmxName()),
                                                stage.getJmxType());
    }
    private static ThreadPoolExecutor multiThreadedConfigurableStage(Stage stage, int numThreads)
    {
        assert numThreads > 1 : "multi-threaded stages must have at least 2 threads";
        return new JMXConfigurableThreadPoolExecutor(numThreads,
                                                     numThreads,
                                                     KEEPALIVE,
                                                     TimeUnit.SECONDS,
                                                     new LinkedBlockingQueue<Runnable>(),
                                                     new NamedThreadFactory(stage.getJmxName()),
                                                     stage.getJmxType());
    }
    public static ThreadPoolExecutor getStage(Stage stage)
    {
        return stages.get(stage);
    }
    public static void shutdownNow()
    {
        for (Stage stage : Stage.values())
        {
            StageManager.stages.get(stage).shutdownNow();
        }
    }
}
