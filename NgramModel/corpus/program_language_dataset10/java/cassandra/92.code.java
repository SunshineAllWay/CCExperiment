package org.apache.cassandra.concurrent;
public interface JMXConfigurableThreadPoolExecutorMBean extends JMXEnabledThreadPoolExecutorMBean
{
    void setCorePoolSize(int n);
    int getCorePoolSize();
}