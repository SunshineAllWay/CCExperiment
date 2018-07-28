package org.apache.cassandra.net.sink;
import java.net.InetAddress;
import org.apache.cassandra.net.Message;
public interface IMessageSink
{
    public Message handleMessage(Message message, InetAddress to);
}
