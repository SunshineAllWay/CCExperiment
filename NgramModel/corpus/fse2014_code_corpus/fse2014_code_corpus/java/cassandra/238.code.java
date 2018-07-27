package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.ICompactSerializer;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class EndpointState
{
    protected static Logger logger = LoggerFactory.getLogger(EndpointState.class);
    private final static ICompactSerializer<EndpointState> serializer_ = new EndpointStateSerializer();
    volatile HeartBeatState hbState_;
    final Map<ApplicationState, VersionedValue> applicationState_ = new NonBlockingHashMap<ApplicationState, VersionedValue>();
    volatile long updateTimestamp_;
    volatile boolean isAlive_;
    volatile boolean isAGossiper_;
    volatile boolean hasToken_;
    public static ICompactSerializer<EndpointState> serializer()
    {
        return serializer_;
    }
    EndpointState(HeartBeatState hbState)
    { 
        hbState_ = hbState; 
        updateTimestamp_ = System.currentTimeMillis(); 
        isAlive_ = true; 
        isAGossiper_ = false;
        hasToken_ = false;
    }
    HeartBeatState getHeartBeatState()
    {
        return hbState_;
    }
    void setHeartBeatState(HeartBeatState hbState)
    {
        updateTimestamp();
        hbState_ = hbState;
    }
    public VersionedValue getApplicationState(ApplicationState key)
    {
        return applicationState_.get(key);
    }
    @Deprecated
    public Map<ApplicationState, VersionedValue> getApplicationStateMap()
    {
        return applicationState_;
    }
    void addApplicationState(ApplicationState key, VersionedValue value)
    {
        applicationState_.put(key, value);
    }
    long getUpdateTimestamp()
    {
        return updateTimestamp_;
    }
    void updateTimestamp()
    {
        updateTimestamp_ = System.currentTimeMillis();
    }
    public boolean isAlive()
    {        
        return isAlive_;
    }
    void isAlive(boolean value)
    {        
        isAlive_ = value;        
    }
    boolean isAGossiper()
    {        
        return isAGossiper_;
    }
    void isAGossiper(boolean value)
    {                
        isAGossiper_ = value;        
    }
    public void setHasToken(boolean value)
    {
        hasToken_ = value;
    }
    public boolean getHasToken()
    {
        return hasToken_;
    }
}
class EndpointStateSerializer implements ICompactSerializer<EndpointState>
{
    private static Logger logger_ = LoggerFactory.getLogger(EndpointStateSerializer.class);
    public void serialize(EndpointState epState, DataOutputStream dos) throws IOException
    {
        HeartBeatState hbState = epState.getHeartBeatState();
        HeartBeatState.serializer().serialize(hbState, dos);
        int size = epState.applicationState_.size();
        dos.writeInt(size);
        for (Map.Entry<ApplicationState, VersionedValue> entry : epState.applicationState_.entrySet())
        {
            VersionedValue value = entry.getValue();
            if (value != null)
            {
                dos.writeInt(entry.getKey().ordinal());
                VersionedValue.serializer.serialize(value, dos);
            }
        }
    }
    public EndpointState deserialize(DataInputStream dis) throws IOException
    {
        HeartBeatState hbState = HeartBeatState.serializer().deserialize(dis);
        EndpointState epState = new EndpointState(hbState);
        int appStateSize = dis.readInt();
        for ( int i = 0; i < appStateSize; ++i )
        {
            if ( dis.available() == 0 )
            {
                break;
            }
            int key = dis.readInt();
            VersionedValue value = VersionedValue.serializer.deserialize(dis);
            epState.addApplicationState(Gossiper.STATES[key], value);
        }
        return epState;
    }
}
