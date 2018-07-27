package org.apache.cassandra.gms;
import java.net.InetAddress;
public interface IFailureDetector
{
    public boolean isAlive(InetAddress ep);
    public void interpret(InetAddress ep);
    public void report(InetAddress ep);
    public void remove(InetAddress ep);
    public void registerFailureDetectionEventListener(IFailureDetectionEventListener listener);
    public void unregisterFailureDetectionEventListener(IFailureDetectionEventListener listener);
}
