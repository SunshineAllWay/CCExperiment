package org.apache.cassandra.locator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.ResourceWatcher;
import org.apache.cassandra.utils.WrappedRunnable;
public class PropertyFileSnitch extends AbstractNetworkTopologySnitch
{
    private static final Logger logger = LoggerFactory.getLogger(PropertyFileSnitch.class);
    private static final String RACK_PROPERTY_FILENAME = "cassandra-topology.properties";
    private static volatile Map<InetAddress, String[]> endpointMap;
    private static volatile String[] defaultDCRack;
    public PropertyFileSnitch() throws ConfigurationException
    {
        reloadConfiguration();
        Runnable runnable = new WrappedRunnable()
        {
            protected void runMayThrow() throws ConfigurationException
            {
                reloadConfiguration();
            }
        };
        ResourceWatcher.watch(RACK_PROPERTY_FILENAME, runnable, 60 * 1000);
    }
    public String[] getEndpointInfo(InetAddress endpoint)
    {
        String[] value = endpointMap.get(endpoint);
        if (value == null)
        {
            logger.debug("Could not find end point information for {}, will use default", endpoint);
            return defaultDCRack;
        }
        return value;
    }
    public String getDatacenter(InetAddress endpoint)
    {
        return getEndpointInfo(endpoint)[0];
    }
    public String getRack(InetAddress endpoint)
    {
        return getEndpointInfo(endpoint)[1];
    }
    public void reloadConfiguration() throws ConfigurationException
    {
        HashMap<InetAddress, String[]> reloadedMap = new HashMap<InetAddress, String[]>();
        String rackPropertyFilename = FBUtilities.resourceToFile(RACK_PROPERTY_FILENAME);
        Properties properties = new Properties();
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(rackPropertyFilename));
            properties.load(reader);
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Unable to read " + RACK_PROPERTY_FILENAME, e);
        }
        finally
        {
            FileUtils.closeQuietly(reader);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.equals("default"))
            {
                defaultDCRack = value.split(":");
                if (defaultDCRack.length < 2)
                    defaultDCRack = new String[] { "default", "default" };
            }
            else
            {
                InetAddress host;
                String hostString = key.replace("/", "");
                try
                {
                    host = InetAddress.getByName(hostString);
                }
                catch (UnknownHostException e)
                {
                    throw new ConfigurationException("Unknown host " + hostString, e);
                }
                String[] token = value.split(":");
                if (token.length < 2)
                    token = new String[] { "default", "default" };
                reloadedMap.put(host, token);
            }
        }
        logger.debug("loaded network topology {}", FBUtilities.toString(reloadedMap));
        endpointMap = reloadedMap;
        StorageService.instance.getTokenMetadata().invalidateCaches();
    }
}
