package org.apache.cassandra.net;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.locator.ILatencyPublisher;
import org.apache.cassandra.locator.ILatencySubscriber;
public class ResponseVerbHandler implements IVerbHandler, ILatencyPublisher
{
    private static final Logger logger_ = LoggerFactory.getLogger( ResponseVerbHandler.class );
    private List<ILatencySubscriber>  subscribers = new ArrayList<ILatencySubscriber>();
    public void doVerb(Message message)
    {     
        String messageId = message.getMessageId();
        MessagingService.instance().responseReceivedFrom(messageId, message.getFrom());
        double age = System.currentTimeMillis() - MessagingService.instance().getRegisteredCallbackAge(messageId);
        IMessageCallback cb = MessagingService.instance().getRegisteredCallback(messageId);
        if (cb == null)
            return;
        for (ILatencySubscriber subscriber : subscribers)
            subscriber.receiveTiming(message.getFrom(), age);
        if (cb instanceof IAsyncCallback)
        {
            if (logger_.isDebugEnabled())
                logger_.debug("Processing response on a callback from " + message.getMessageId() + "@" + message.getFrom());
            ((IAsyncCallback) cb).response(message);
        }
        else
        {
            if (logger_.isDebugEnabled())
                logger_.debug("Processing response on an async result from " + message.getMessageId() + "@" + message.getFrom());
            ((IAsyncResult) cb).result(message);
        }
    }
    public void register(ILatencySubscriber subscriber)
    {
        subscribers.add(subscriber);
    }
}
