package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
public class BinaryVerbHandler implements IVerbHandler
{
    private static Logger logger_ = LoggerFactory.getLogger(BinaryVerbHandler.class);
    public void doVerb(Message message)
    { 
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
        try
        {
            RowMutation rm = RowMutation.serializer().deserialize(new DataInputStream(buffer));
            rm.applyBinary();
            WriteResponse response = new WriteResponse(rm.getTable(), rm.key(), true);
            Message responseMessage = WriteResponse.makeWriteResponseMessage(message, response);
            if (logger_.isDebugEnabled())
              logger_.debug("binary " + rm + " applied.  Sending response to " + message.getMessageId() + "@" + message.getFrom());
            MessagingService.instance().sendOneWay(responseMessage, message.getFrom());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
