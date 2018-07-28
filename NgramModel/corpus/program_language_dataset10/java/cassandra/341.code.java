package org.apache.cassandra.net.sink;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.net.Message;
public class SinkManager
{
    private static List<IMessageSink> sinks = new ArrayList<IMessageSink>();
    public static void add(IMessageSink ms)
    {
        sinks.add(ms);
    }
    public static void clear()
    {
        sinks.clear();
    }
    public static Message processClientMessage(Message message, InetAddress to)
    {
        if (sinks.isEmpty())
            return message;
        for (IMessageSink ms : sinks)
        {
            message = ms.handleMessage(message, to);
            if (message == null)
                return null;
        }
        return message;
    }
    public static Message processServerMessage(Message message)
    {
        if (sinks.isEmpty())
            return message;
        for (IMessageSink ms : sinks)
        {
            message = ms.handleMessage(message, null);
            if (message == null)
                return null;
        }
        return message;
    }
}
