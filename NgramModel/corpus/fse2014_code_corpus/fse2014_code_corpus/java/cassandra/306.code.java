package org.apache.cassandra.locator;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
public class Ec2Snitch extends AbstractNetworkTopologySnitch
{
    protected static Logger logger = LoggerFactory.getLogger(Ec2Snitch.class);
    protected String ec2zone;
    protected String ec2region;
    public Ec2Snitch() throws IOException, ConfigurationException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://169.254.169.254/latest/meta-data/placement/availability-zone").openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200)
        {
            throw new ConfigurationException("Ec2Snitch was unable to find region/zone data. Not an ec2 node?");
        }
        int cl = conn.getContentLength();
        byte[] b = new byte[cl];
        DataInputStream d = new DataInputStream((FilterInputStream)conn.getContent());
        d.readFully(b);
        String azone = new String(b ,"UTF-8");
        String[] splits = azone.split("-");
        ec2zone = splits[splits.length - 1];
        ec2region = splits.length < 3 ? splits[0] : splits[0]+"-"+splits[1];
        logger.info("EC2Snitch using region: " + ec2region + ", zone: " + ec2zone + ".");
    }
    public String getRack(InetAddress endpoint)
    {
        if (endpoint == FBUtilities.getLocalAddress())
            return ec2zone;
        else
            return Gossiper.instance.getEndpointStateForEndpoint(endpoint).getApplicationState(ApplicationState.RACK).value;
    }
    public String getDatacenter(InetAddress endpoint)
    {
        if (endpoint == FBUtilities.getLocalAddress())
            return ec2region;
        else
            return Gossiper.instance.getEndpointStateForEndpoint(endpoint).getApplicationState(ApplicationState.DC).value;
    }
    @Override
    public void gossiperStarting()
    {
        logger.info("Ec2Snitch adding ApplicationState ec2region=" + ec2region + " ec2zone=" + ec2zone);
        Gossiper.instance.addLocalApplicationState(ApplicationState.DC, StorageService.valueFactory.datacenter(ec2region));
        Gossiper.instance.addLocalApplicationState(ApplicationState.RACK, StorageService.valueFactory.rack(ec2zone));
    }
}
