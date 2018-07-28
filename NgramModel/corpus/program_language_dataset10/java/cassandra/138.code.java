package org.apache.cassandra.db;
import java.io.*;
import java.util.concurrent.TimeoutException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.net.*;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
public class CounterMutationVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(CounterMutationVerbHandler.class);
    public void doVerb(Message message)
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
        try
        {
            DataInputStream is = new DataInputStream(buffer);
            CounterMutation cm = CounterMutation.serializer().deserialize(is);
            if (logger.isDebugEnabled())
              logger.debug("Applying forwarded " + cm);
            StorageProxy.applyCounterMutationOnLeader(cm);
            WriteResponse response = new WriteResponse(cm.getTable(), cm.key(), true);
            Message responseMessage = WriteResponse.makeWriteResponseMessage(message, response);
            MessagingService.instance().sendOneWay(responseMessage, message.getFrom());
        }
        catch (UnavailableException e)
        {
        }
        catch (TimeoutException e)
        {
        }
        catch (IOException e)
        {
            logger.error("Error in counter mutation", e);
        }
    }
}
