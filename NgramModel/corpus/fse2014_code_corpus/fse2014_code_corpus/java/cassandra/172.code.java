package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
public class TruncateVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(TruncateVerbHandler.class);
    public void doVerb(Message message)
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
        try
        {
            Truncation t = Truncation.serializer().deserialize(new DataInputStream(buffer));
            logger.debug("Applying {}", t);
            try
            {
                Table.open(t.keyspace).truncate(t.columnFamily);
            }
            catch (IOException e)
            {
                logger.error("Error in truncation", e);
                respondError(t, message);
                throw e;
            }
            catch (InterruptedException e)
            {
                logger.error("Error in truncation", e);
                respondError(t, message);
                throw e;
            }
            catch (ExecutionException e)
            {
                logger.error("Error in truncation", e);
                respondError(t, message);
                throw e;
            }
            logger.debug("Truncate operation succeeded at this host");
            TruncateResponse response = new TruncateResponse(t.keyspace, t.columnFamily, true);
            Message responseMessage = TruncateResponse.makeTruncateResponseMessage(message, response);
            logger.debug("{} applied.  Sending response to {}@{} ",
                    new Object[]{t, message.getMessageId(), message.getFrom()});
            MessagingService.instance().sendOneWay(responseMessage, message.getFrom());
        }
        catch (IOException e)
        {
            logger.error("Error in truncation", e);
            throw new RuntimeException("Error in truncation", e);
        }
        catch (InterruptedException e)
        {
            logger.error("Error in truncation", e);
            throw new RuntimeException("Error in truncation", e);
        }
        catch (ExecutionException e)
        {
            logger.error("Error in truncation", e);
            throw new RuntimeException("Error in truncation", e);
        }
    }
    private static void respondError(Truncation t, Message truncateRequestMessage) throws IOException
    {
        TruncateResponse response = new TruncateResponse(t.keyspace, t.columnFamily, false);
        Message responseMessage = TruncateResponse.makeTruncateResponseMessage(truncateRequestMessage, response);
        MessagingService.instance().sendOneWay(responseMessage, truncateRequestMessage.getFrom());
    }
}
