package org.apache.cassandra.gms;
import java.net.InetAddress;
public interface IEndpointStateChangeSubscriber
{
    public void onJoin(InetAddress endpoint, EndpointState epState);
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value);
    public void onAlive(InetAddress endpoint, EndpointState state);
    public void onDead(InetAddress endpoint, EndpointState state);
    public void onRemove(InetAddress endpoint);
}
