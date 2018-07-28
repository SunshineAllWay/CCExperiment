package org.apache.cassandra.scheduler;
public interface IRequestScheduler
{
    public void queue(Thread t, String id);
    public void release();
}
