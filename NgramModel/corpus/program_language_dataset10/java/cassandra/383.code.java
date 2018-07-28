package org.apache.cassandra.streaming;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class StreamingService implements StreamingServiceMBean
{
    private static final Logger logger = LoggerFactory.getLogger(StreamingService.class);
    public static final String MBEAN_OBJECT_NAME = "org.apache.cassandra.net:type=StreamingService";
    public static final StreamingService instance = new StreamingService();
    private StreamingService()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(this, new ObjectName(MBEAN_OBJECT_NAME));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public String getStatus()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Receiving from:\n");
        for (InetAddress source : StreamInSession.getSources())
        {
            sb.append(String.format(" %s:%n", source.getHostAddress()));
            for (PendingFile pf : StreamInSession.getIncomingFiles(source))
            {
                sb.append(String.format("  %s%n", pf.toString()));
            }
        }
        sb.append("Sending to:%n");
        for (InetAddress dest : StreamOutSession.getDestinations())
        {
            sb.append(String.format(" %s:%n", dest.getHostAddress()));
            for (PendingFile pf : StreamOutSession.getOutgoingFiles(dest))
            {
                sb.append(String.format("  %s%n", pf.toString()));
            }
        }
        return sb.toString();
    }
    public Set<InetAddress> getStreamDestinations()
    {
        return StreamOutSession.getDestinations();
    }
    public List<String> getOutgoingFiles(String host) throws IOException
    {
        List<String> files = new ArrayList<String>();
        Set<InetAddress> existingDestinations = getStreamDestinations();
        InetAddress dest = InetAddress.getByName(host);
        if (!existingDestinations.contains(dest))
            return files;
        for (PendingFile f : StreamOutSession.getOutgoingFiles(dest))
            files.add(String.format("%s", f.toString()));
        return files;
    }
    public Set<InetAddress> getStreamSources()
    {
        return StreamInSession.getSources();
    }
    public List<String> getIncomingFiles(String host) throws IOException
    {
        List<String> files = new ArrayList<String>();
        for (PendingFile pf : StreamInSession.getIncomingFiles(InetAddress.getByName(host)))
        {
            files.add(String.format("%s: %s", pf.desc.ksname, pf.toString()));
        }
        return files;
    }
}
