package org.apache.cassandra.contrib.stress;
import org.apache.cassandra.contrib.stress.operations.*;
import org.apache.cassandra.contrib.stress.util.OperationThread;
import org.apache.commons.cli.Option;
import java.io.PrintStream;
import java.util.Random;
public final class Stress
{
    public static enum Operation
    {
        INSERT, READ, RANGE_SLICE, INDEXED_RANGE_SLICE, MULTI_GET
    }
    public static Session session;
    public static Random randomizer = new Random();
    public static void main(String[] arguments) throws Exception
    {
        int epoch, total, oldTotal, latency, keyCount, oldKeyCount, oldLatency;
        try
        {
            session = new Session(arguments);
        }
        catch (IllegalArgumentException e)
        {
            printHelpMessage();
            return;
        }
        if (session.getOperation() == Stress.Operation.INSERT)
        {
            session.createKeySpaces();
        }
        int threadCount  = session.getThreads();
        Thread[] threads = new Thread[threadCount];
        PrintStream out  = session.getOutputStream();
        try
        {
            for (int i = 0; i < threadCount; i++)
            {
                threads[i] = createOperation(i);
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            return;
        }
        for (int i = 0; i < threadCount; i++)
        {
            threads[i].start();
        }
        boolean terminate = false;
        epoch = total = latency = keyCount = 0;
        int interval = session.getProgressInterval();
        int epochIntervals = session.getProgressInterval() * 10;
        long testStartTime = System.currentTimeMillis();
        out.println("total,interval_op_rate,interval_key_rate,avg_latency,elapsed_time");
        while (!terminate)
        {
            Thread.sleep(100);
            int alive = 0;
            for (Thread thread : threads)
                if (thread.isAlive()) alive++;
            if (alive == 0)
                terminate = true;
            epoch++;
            if (terminate || epoch > epochIntervals)
            {
                epoch = 0;
                oldTotal    = total;
                oldLatency  = latency;
                oldKeyCount = keyCount;
                int currentTotal = 0, currentKeyCount = 0, currentLatency = 0;
                for (Thread t : threads)
                {
                    OperationThread thread = (OperationThread) t;
                    currentTotal    += session.operationCount.get(thread.index);
                    currentKeyCount += session.keyCount.get(thread.index);
                    currentLatency  += session.latencies.get(thread.index);
                }
                total    = currentTotal;
                keyCount = currentKeyCount;
                latency  = currentLatency;
                int opDelta  = total - oldTotal;
                int keyDelta = keyCount - oldKeyCount;
                double latencyDelta = latency - oldLatency;
                long currentTimeInSeconds = (System.currentTimeMillis() - testStartTime) / 1000;
                String formattedDelta = (opDelta > 0) ? Double.toString(latencyDelta / (opDelta * 1000)) : "NaN";
                out.println(String.format("%d,%d,%d,%s,%d", total, opDelta / interval, keyDelta / interval, formattedDelta, currentTimeInSeconds));
            }
        }
    }
    private static Thread createOperation(int index)
    {
        switch (session.getOperation())
        {
            case READ:
                return new Reader(index);
            case INSERT:
                return new Inserter(index);
            case RANGE_SLICE:
                return new RangeSlicer(index);
            case INDEXED_RANGE_SLICE:
                return new IndexedRangeSlicer(index);
            case MULTI_GET:
                return new MultiGetter(index);
        }
        throw new UnsupportedOperationException();
    }
    public static void printHelpMessage()
    {
        System.out.println("Usage: ./bin/stress [options]\n\nOptions:");
        for(Object o : Session.availableOptions.getOptions())
        {
            Option option = (Option) o;
            String upperCaseName = option.getLongOpt().toUpperCase();
            System.out.println(String.format("-%s%s, --%s%s%n\t\t%s%n", option.getOpt(), (option.hasArg()) ? " "+upperCaseName : "",
                                                            option.getLongOpt(), (option.hasArg()) ? "="+upperCaseName : "", option.getDescription()));
        }
    }
}
