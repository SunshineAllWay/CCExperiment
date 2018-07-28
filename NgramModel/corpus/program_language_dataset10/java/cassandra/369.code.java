package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.gms.*;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
public class StorageLoadBalancer implements IEndpointStateChangeSubscriber
{
    class LoadBalancer implements Runnable
    {
        LoadBalancer()
        {
            loadInfo2_.putAll(loadInfo_);
        }
        public void run()
        {
        }
    }
    class MoveMessageVerbHandler implements IVerbHandler
    {
        public void doVerb(Message message)
        {
            Message reply = message.getInternalReply(new byte[] {(byte)(isMoveable_.get() ? 1 : 0)});
            MessagingService.instance().sendOneWay(reply, message.getFrom());
            if ( isMoveable_.get() )
            {
                isMoveable_.set(false);
            }
        }
    }
    private static final int BROADCAST_INTERVAL = 60 * 1000;
    public static final StorageLoadBalancer instance = new StorageLoadBalancer();
    private static final Logger logger_ = LoggerFactory.getLogger(StorageLoadBalancer.class);
    private static final int delay_ = 5;
    private static final double TOPHEAVY_RATIO = 1.5;
    private AtomicBoolean isMoveable_ = new AtomicBoolean(false);
    private Map<InetAddress, Double> loadInfo_ = new HashMap<InetAddress, Double>();
    private Map<InetAddress, Double> loadInfo2_ = new HashMap<InetAddress, Double>();
    private StorageLoadBalancer()
    {
        Gossiper.instance.register(this);
    }
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value)
    {
        if (state != ApplicationState.LOAD)
            return;
        loadInfo_.put(endpoint, Double.valueOf(value.value));
    }
    public void onJoin(InetAddress endpoint, EndpointState epState)
    {
        VersionedValue localValue = epState.getApplicationState(ApplicationState.LOAD);
        if (localValue != null)
        {
            onChange(endpoint, ApplicationState.LOAD, localValue);
        }
    }
    public void onAlive(InetAddress endpoint, EndpointState state) {}
    public void onDead(InetAddress endpoint, EndpointState state) {}
    public void onRemove(InetAddress endpoint) {}
    private double localLoad()
    {
        Double load = loadInfo2_.get(FBUtilities.getLocalAddress());
        return load == null ? 0 : load;
    }
    private double averageSystemLoad()
    {
        int nodeCount = loadInfo2_.size();
        Set<InetAddress> nodes = loadInfo2_.keySet();
        double systemLoad = 0;
        for (InetAddress node : nodes)
        {
            systemLoad += loadInfo2_.get(node);
        }
        double averageLoad = (nodeCount > 0) ? (systemLoad / nodeCount) : 0;
        if (logger_.isDebugEnabled())
            logger_.debug("Average system load is {}", averageLoad);
        return averageLoad;
    }
    private boolean isHeavyNode()
    {
        return ( localLoad() > ( StorageLoadBalancer.TOPHEAVY_RATIO * averageSystemLoad() ) );
    }
    private boolean isMoveable(InetAddress target)
    {
        double threshold = StorageLoadBalancer.TOPHEAVY_RATIO * averageSystemLoad();
        if (isANeighbour(target))
        {
            Double load = loadInfo2_.get(target);
            if (load == null)
            {
                return false;
            }
            else
            {
                double myload = localLoad();
                double avgLoad = (load + myload) / 2;
                return avgLoad <= threshold;
            }
        }
        else
        {
            InetAddress successor = StorageService.instance.getSuccessor(target);
            double sLoad = loadInfo2_.get(successor);
            double targetLoad = loadInfo2_.get(target);
            return (sLoad + targetLoad) <= threshold;
        }
    }
    private boolean isANeighbour(InetAddress neighbour)
    {
        InetAddress predecessor = StorageService.instance.getPredecessor(FBUtilities.getLocalAddress());
        if ( predecessor.equals(neighbour) )
            return true;
        InetAddress successor = StorageService.instance.getSuccessor(FBUtilities.getLocalAddress());
        if ( successor.equals(neighbour) )
            return true;
        return false;
    }
    private InetAddress findARandomLightNode()
    {
        List<InetAddress> potentialCandidates = new ArrayList<InetAddress>();
        Set<InetAddress> allTargets = loadInfo2_.keySet();
        double avgLoad = averageSystemLoad();
        for (InetAddress target : allTargets)
        {
            double load = loadInfo2_.get(target);
            if (load < avgLoad)
            {
                potentialCandidates.add(target);
            }
        }
        if (potentialCandidates.size() > 0)
        {
            Random random = new Random();
            int index = random.nextInt(potentialCandidates.size());
            return potentialCandidates.get(index);
        }
        return null;
    }
    public Map<InetAddress, Double> getLoadInfo()
    {
        return loadInfo_;
    }
    public void startBroadcasting()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if (logger_.isDebugEnabled())
                    logger_.debug("Disseminating load info ...");
                Gossiper.instance.addLocalApplicationState(ApplicationState.LOAD,
                                                           StorageService.valueFactory.load(StorageService.instance.getLoad()));
            }
        };
        StorageService.scheduledTasks.scheduleWithFixedDelay(runnable, 2 * Gossiper.intervalInMillis_, BROADCAST_INTERVAL, TimeUnit.MILLISECONDS);
    }
    public void waitForLoadInfo()
    {
        int duration = BROADCAST_INTERVAL + StorageService.RING_DELAY;
        try
        {
            logger_.info("Sleeping {} ms to wait for load information...", duration);
            Thread.sleep(duration);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }
}
