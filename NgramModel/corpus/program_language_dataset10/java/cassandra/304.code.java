package org.apache.cassandra.locator;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.AbstractStatsDeque;
import org.apache.cassandra.utils.FBUtilities;
public class DynamicEndpointSnitch extends AbstractEndpointSnitch implements ILatencySubscriber, DynamicEndpointSnitchMBean
{
    private static final int UPDATES_PER_INTERVAL = 10000;
    private static final int WINDOW_SIZE = 100;
    private int UPDATE_INTERVAL_IN_MS = DatabaseDescriptor.getDynamicUpdateInterval();
    private int RESET_INTERVAL_IN_MS = DatabaseDescriptor.getDynamicResetInterval();
    private double BADNESS_THRESHOLD = DatabaseDescriptor.getDynamicBadnessThreshold();
    private String mbeanName;
    private boolean registered = false;
    private final ConcurrentHashMap<InetAddress, Double> scores = new ConcurrentHashMap<InetAddress, Double>();
    private final ConcurrentHashMap<InetAddress, AdaptiveLatencyTracker> windows = new ConcurrentHashMap<InetAddress, AdaptiveLatencyTracker>();
    private final AtomicInteger intervalupdates = new AtomicInteger(0);
    public final IEndpointSnitch subsnitch;
    public DynamicEndpointSnitch(IEndpointSnitch snitch)
    {
        mbeanName = "org.apache.cassandra.db:type=DynamicEndpointSnitch,instance="+hashCode();
        subsnitch = snitch;
        Runnable update = new Runnable()
        {
            public void run()
            {
                updateScores();
            }
        };
        Runnable reset = new Runnable()
        {
            public void run()
            {
                reset();
            }
        };
        StorageService.scheduledTasks.scheduleWithFixedDelay(update, UPDATE_INTERVAL_IN_MS, UPDATE_INTERVAL_IN_MS, TimeUnit.MILLISECONDS);
        StorageService.scheduledTasks.scheduleWithFixedDelay(reset, RESET_INTERVAL_IN_MS, RESET_INTERVAL_IN_MS, TimeUnit.MILLISECONDS);
        registerMBean();
   }
    private void registerMBean()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(this, new ObjectName(mbeanName));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public void unregisterMBean()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.unregisterMBean(new ObjectName(mbeanName));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void gossiperStarting()
    {
        subsnitch.gossiperStarting();
    }
    public String getRack(InetAddress endpoint)
    {
        return subsnitch.getRack(endpoint);
    }
    public String getDatacenter(InetAddress endpoint)
    {
        return subsnitch.getDatacenter(endpoint);
    }
    public List<InetAddress> getSortedListByProximity(final InetAddress address, Collection<InetAddress> addresses)
    {
        List<InetAddress> list = new ArrayList<InetAddress>(addresses);
        sortByProximity(address, list);
        return list;
    }
    public void sortByProximity(final InetAddress address, List<InetAddress> addresses)
    {
        assert address.equals(FBUtilities.getLocalAddress()); 
        if (BADNESS_THRESHOLD == 0)
        {
            sortByProximityWithScore(address, addresses);
        }
        else
        {
            sortByProximityWithBadness(address, addresses);
        }
    }
    private void sortByProximityWithScore(final InetAddress address, List<InetAddress> addresses)
    {
        Collections.sort(addresses, new Comparator<InetAddress>()
        {
            public int compare(InetAddress a1, InetAddress a2)
            {
                return compareEndpoints(address, a1, a2);
            }
        });
    }
    private void sortByProximityWithBadness(final InetAddress address, List<InetAddress> addresses)
    {
        if (addresses.size() < 2)
            return;
        subsnitch.sortByProximity(address, addresses);
        Double first = scores.get(addresses.get(0));
        if (first == null)
            return;
        for (InetAddress addr : addresses)
        {
            Double next = scores.get(addr);
            if (next == null)
                return;
            if ((first - next) / first > BADNESS_THRESHOLD)
            {
                sortByProximityWithScore(address, addresses);
                return;
            }
        }
    }
    public int compareEndpoints(InetAddress target, InetAddress a1, InetAddress a2)
    {
        Double scored1 = scores.get(a1);
        Double scored2 = scores.get(a2);
        if (scored1 == null || scored2 == null || scored1.equals(scored2))
            return subsnitch.compareEndpoints(target, a1, a2);
        if (scored1 < scored2)
            return -1;
        else
            return 1;
    }
    public void receiveTiming(InetAddress host, Double latency) 
    {
        if (intervalupdates.intValue() >= UPDATES_PER_INTERVAL)
            return;
        AdaptiveLatencyTracker tracker = windows.get(host);
        if (tracker == null)
        {
            AdaptiveLatencyTracker alt = new AdaptiveLatencyTracker(WINDOW_SIZE);
            tracker = windows.putIfAbsent(host, alt);
            if (tracker == null)
                tracker = alt;
        }
        tracker.add(latency);
        intervalupdates.getAndIncrement();
    }
    private void updateScores() 
    {
        if (!StorageService.instance.isInitialized()) 
            return;
        if (!registered)
        {
       	    ILatencyPublisher handler = (ILatencyPublisher) MessagingService.instance().getVerbHandler(StorageService.Verb.REQUEST_RESPONSE);
            if (handler != null)
            {
                handler.register(this);
                registered = true;
            }
        }
        for (Map.Entry<InetAddress, AdaptiveLatencyTracker> entry: windows.entrySet())
        {
            scores.put(entry.getKey(), entry.getValue().score());
        }
        intervalupdates.set(0);
    }
    private void reset()
    {
        for (AdaptiveLatencyTracker tracker : windows.values())
        {
            tracker.clear();
        }
    }
    public Map<InetAddress, Double> getScores()
    {
        return scores;
    }
    public int getUpdateInterval()
    {
        return UPDATE_INTERVAL_IN_MS;
    }
    public int getResetInterval()
    {
        return RESET_INTERVAL_IN_MS;
    }
    public double getBadnessThreshold()
    {
        return BADNESS_THRESHOLD;
    }
    public String getSubsnitchClassName()
    {
        return subsnitch.getClass().getName();
    }
}
class AdaptiveLatencyTracker extends AbstractStatsDeque
{
    private final LinkedBlockingDeque<Double> latencies;
    private static final double SENTINEL_COMPARE = 0.0001; 
    AdaptiveLatencyTracker(int size)
    {
        latencies = new LinkedBlockingDeque<Double>(size);
    }
    public void add(double i)
    {
        if (!latencies.offer(i))
        {
            latencies.remove();
            latencies.offer(i);
        }
    }
    public void clear()
    {
        latencies.clear();
    }
    public Iterator<Double> iterator()
    {
        return latencies.iterator();
    }
    public int size()
    {
        return latencies.size();
    }
    double p(double t)
    {
        double mean = mean();
        double exponent = (-1) * (t) / mean;
        return 1 - Math.pow( Math.E, exponent);
    }
    double score()
    {
        double log = 0d;
        if ( latencies.size() > 0 )
        {
            double probability = p(SENTINEL_COMPARE);
            log = (-1) * Math.log10( probability );
        }
        return log;
    }
}
