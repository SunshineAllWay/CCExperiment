package org.apache.cassandra.streaming;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
public interface StreamingServiceMBean
{
    public Set<InetAddress> getStreamDestinations();
    public List<String> getOutgoingFiles(String host) throws IOException;
    public Set<InetAddress> getStreamSources();
    public List<String> getIncomingFiles(String host) throws IOException;
    public String getStatus();
}
