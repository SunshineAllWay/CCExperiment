package org.apache.cassandra.scheduler;
import org.apache.cassandra.config.RequestSchedulerOptions;
public class NoScheduler implements IRequestScheduler
{
    public NoScheduler(RequestSchedulerOptions options) {}
    public NoScheduler() {}
    public void queue(Thread t, String id) {}
    public void release() {}
}
