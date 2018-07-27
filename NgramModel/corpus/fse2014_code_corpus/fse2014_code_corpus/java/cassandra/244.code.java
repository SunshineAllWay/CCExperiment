package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.io.ICompactSerializer;
class GossipDigestAckMessage
{
    private static ICompactSerializer<GossipDigestAckMessage> serializer_;
    static
    {
        serializer_ = new GossipDigestAckMessageSerializer();
    }
    List<GossipDigest> gDigestList_ = new ArrayList<GossipDigest>();
    Map<InetAddress, EndpointState> epStateMap_ = new HashMap<InetAddress, EndpointState>();
    static ICompactSerializer<GossipDigestAckMessage> serializer()
    {
        return serializer_;
    }
    GossipDigestAckMessage(List<GossipDigest> gDigestList, Map<InetAddress, EndpointState> epStateMap)
    {
        gDigestList_ = gDigestList;
        epStateMap_ = epStateMap;
    }
    List<GossipDigest> getGossipDigestList()
    {
        return gDigestList_;
    }
    Map<InetAddress, EndpointState> getEndpointStateMap()
    {
        return epStateMap_;
    }
}
class GossipDigestAckMessageSerializer implements ICompactSerializer<GossipDigestAckMessage>
{
    public void serialize(GossipDigestAckMessage gDigestAckMessage, DataOutputStream dos) throws IOException
    {
        GossipDigestSerializationHelper.serialize(gDigestAckMessage.gDigestList_, dos);
        dos.writeBoolean(true); 
        EndpointStatesSerializationHelper.serialize(gDigestAckMessage.epStateMap_, dos);
    }
    public GossipDigestAckMessage deserialize(DataInputStream dis) throws IOException
    {
        List<GossipDigest> gDigestList = GossipDigestSerializationHelper.deserialize(dis);
        dis.readBoolean(); 
        Map<InetAddress, EndpointState> epStateMap = EndpointStatesSerializationHelper.deserialize(dis);
        return new GossipDigestAckMessage(gDigestList, epStateMap);
    }
}
