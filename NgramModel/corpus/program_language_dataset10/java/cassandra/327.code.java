package org.apache.cassandra.net;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
public interface IAsyncResult extends IMessageCallback
{    
    public byte[] get(long timeout, TimeUnit tu) throws TimeoutException;
    public void result(Message result);
    public InetAddress getFrom();
}
