package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.*;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.utils.FBUtilities;
public class NetworkTopologyStrategy extends AbstractReplicationStrategy
{
    private IEndpointSnitch snitch;
    private Map<String, Integer> datacenters;
    private static final Logger logger = LoggerFactory.getLogger(NetworkTopologyStrategy.class);
    public NetworkTopologyStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions) throws ConfigurationException
    {
        super(table, tokenMetadata, snitch, configOptions);
        this.snitch = snitch;
        Map<String, Integer> newDatacenters = new HashMap<String, Integer>();
        if (configOptions != null)
        {
            for (Entry entry : configOptions.entrySet())
                newDatacenters.put((String) entry.getKey(), Integer.valueOf((String) entry.getValue()));
        }
        datacenters = Collections.unmodifiableMap(newDatacenters);
        logger.debug("Configured datacenter replicas are {}", FBUtilities.toString(datacenters));
    }
    public List<InetAddress> calculateNaturalEndpoints(Token searchToken, TokenMetadata tokenMetadata)
    {
        List<InetAddress> endpoints = new ArrayList<InetAddress>(getReplicationFactor());
        for (Entry<String, Integer> dcEntry : datacenters.entrySet())
        {
            String dcName = dcEntry.getKey();
            int dcReplicas = dcEntry.getValue();
            TokenMetadata dcTokens = new TokenMetadata();
            for (Entry<Token, InetAddress> tokenEntry : tokenMetadata.entrySet())
            {
                if (snitch.getDatacenter(tokenEntry.getValue()).equals(dcName))
                    dcTokens.updateNormalToken(tokenEntry.getKey(), tokenEntry.getValue());
            }
            List<InetAddress> dcEndpoints = new ArrayList<InetAddress>(dcReplicas);
            Set<String> racks = new HashSet<String>();
            for (Iterator<Token> iter = TokenMetadata.ringIterator(dcTokens.sortedTokens(), searchToken, false);
                 dcEndpoints.size() < dcReplicas && iter.hasNext(); )
            {
                Token token = iter.next();
                InetAddress endpoint = dcTokens.getEndpoint(token);
                String rack = snitch.getRack(endpoint);
                if (!racks.contains(rack))
                {
                    dcEndpoints.add(endpoint);
                    racks.add(rack);
                }
            }
            for (Iterator<Token> iter = TokenMetadata.ringIterator(dcTokens.sortedTokens(), searchToken, false);
                 dcEndpoints.size() < dcReplicas && iter.hasNext(); )
            {
                Token token = iter.next();
                InetAddress endpoint = dcTokens.getEndpoint(token);
                if (!dcEndpoints.contains(endpoint))
                    dcEndpoints.add(endpoint);
            }
            if (dcEndpoints.size() < dcReplicas)
                throw new IllegalStateException(String.format("datacenter (%s) has no more endpoints, (%s) replicas still needed",
                                                              dcName, dcReplicas - dcEndpoints.size()));
            if (logger.isDebugEnabled())
                logger.debug("{} endpoints in datacenter {} for token {} ",
                             new Object[] { StringUtils.join(dcEndpoints, ","), dcName, searchToken});
            endpoints.addAll(dcEndpoints);
        }
        return endpoints;
    }
    public int getReplicationFactor()
    {
        int total = 0;
        for (int repFactor : datacenters.values())
            total += repFactor;
        return total;
    }
    public int getReplicationFactor(String dc)
    {
        return datacenters.get(dc);
    }
    public Set<String> getDatacenters()
    {
        return datacenters.keySet();
    }
    @Override
    public IWriteResponseHandler getWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistency_level)
    {
        if (consistency_level == ConsistencyLevel.LOCAL_QUORUM)
        {
            return DatacenterWriteResponseHandler.create(writeEndpoints, hintedEndpoints, consistency_level, table);
        }
        else if (consistency_level == ConsistencyLevel.EACH_QUORUM)
        {
            return DatacenterSyncWriteResponseHandler.create(writeEndpoints, hintedEndpoints, consistency_level, table);
        }
        return super.getWriteResponseHandler(writeEndpoints, hintedEndpoints, consistency_level);
    }
}
