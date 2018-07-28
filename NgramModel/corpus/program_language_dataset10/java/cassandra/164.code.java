package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class RowMutationVerbHandler implements IVerbHandler
{
    private static Logger logger_ = LoggerFactory.getLogger(RowMutationVerbHandler.class);
    public void doVerb(Message message)
    {
        try
        {
            RowMutation rm = RowMutation.fromBytes(message.getMessageBody());
            if (logger_.isDebugEnabled())
              logger_.debug("Applying " + rm);
            byte[] hintedBytes = message.getHeader(RowMutation.HINT);
            if (hintedBytes != null)
            {
                assert hintedBytes.length > 0;
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(hintedBytes));
                while (dis.available() > 0)
                {
                    ByteBuffer addressBytes = ByteBufferUtil.readWithShortLength(dis);
                    if (logger_.isDebugEnabled())
                        logger_.debug("Adding hint for " + InetAddress.getByName(ByteBufferUtil.string(addressBytes, Charsets.UTF_8)));
                    RowMutation hintedMutation = new RowMutation(Table.SYSTEM_TABLE, addressBytes);
                    hintedMutation.addHints(rm);
                    hintedMutation.apply();
                }
            }
            byte[] forwardBytes = message.getHeader(RowMutation.FORWARD_HEADER);
            if (forwardBytes != null)
                forwardToLocalNodes(message, forwardBytes);
            Table.open(rm.getTable()).apply(rm, true);
            WriteResponse response = new WriteResponse(rm.getTable(), rm.key(), true);
            Message responseMessage = WriteResponse.makeWriteResponseMessage(message, response);
            if (logger_.isDebugEnabled())
              logger_.debug(rm + " applied.  Sending response to " + message.getMessageId() + "@" + message.getFrom());
            MessagingService.instance().sendOneWay(responseMessage, message.getFrom());
        }
        catch (IOException e)
        {
            logger_.error("Error in row mutation", e);
        }
    }  
    private void forwardToLocalNodes(Message message, byte[] forwardBytes) throws UnknownHostException
    {
        message.setHeader(RowMutation.FORWARD_HEADER, null);
        int bytesPerInetAddress = FBUtilities.getLocalAddress().getAddress().length;
        assert forwardBytes.length >= bytesPerInetAddress;
        assert forwardBytes.length % bytesPerInetAddress == 0;
        int offset = 0;
        byte[] addressBytes = new byte[bytesPerInetAddress];
        while (offset < forwardBytes.length)
        {
            System.arraycopy(forwardBytes, offset, addressBytes, 0, bytesPerInetAddress);
            InetAddress address = InetAddress.getByAddress(addressBytes);
            if (logger_.isDebugEnabled())
                logger_.debug("Forwarding message to " + address);
            MessagingService.instance().sendOneWay(message, message.getFrom());
            offset += bytesPerInetAddress;
        }
    }
}
