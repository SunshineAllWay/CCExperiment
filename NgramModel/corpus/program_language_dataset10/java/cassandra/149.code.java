package org.apache.cassandra.db;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
public interface IFlushable
{
    public void flushAndSignal(CountDownLatch condition, ExecutorService sorter, ExecutorService writer);
}
