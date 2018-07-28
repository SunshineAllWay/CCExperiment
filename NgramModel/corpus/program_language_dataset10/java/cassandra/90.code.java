package org.apache.cassandra.concurrent;
public interface IExecutorMBean
{
    public int getActiveCount();
    public long getCompletedTasks();
    public long getPendingTasks();
}
