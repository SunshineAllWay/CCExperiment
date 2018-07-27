package org.apache.cassandra.concurrent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
public class JMXConfigurableThreadPoolExecutor extends JMXEnabledThreadPoolExecutor implements JMXConfigurableThreadPoolExecutorMBean 
{
    public JMXConfigurableThreadPoolExecutor(int corePoolSize,
                                             int maximumPoolSize, 
        	                                 long keepAliveTime, 
        	                                 TimeUnit unit,
                                             BlockingQueue<Runnable> workQueue, 
                                             NamedThreadFactory threadFactory,
                                             String jmxPath)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, jmxPath);
    }
}