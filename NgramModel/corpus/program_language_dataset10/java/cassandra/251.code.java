package org.apache.cassandra.gms;
import java.net.InetAddress;
public interface IFailureDetectionEventListener
{  
    public void convict(InetAddress ep);
}
