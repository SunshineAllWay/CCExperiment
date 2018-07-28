package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.thrift.UnavailableException;
public interface IWriteResponseHandler extends IAsyncCallback
{
    public void get() throws TimeoutException;
    public void addHintCallback(Message hintedMessage, InetAddress destination);
    public void assureSufficientLiveNodes() throws UnavailableException;
}
