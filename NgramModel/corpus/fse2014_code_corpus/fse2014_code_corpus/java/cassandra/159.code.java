package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class ReadVerbHandler implements IVerbHandler
{
    protected static class ReadContext
    {
        protected ByteArrayInputStream bufIn_;
        protected DataOutputBuffer bufOut_ = new DataOutputBuffer();
    }
    private static Logger logger_ = LoggerFactory.getLogger( ReadVerbHandler.class );
    private static ThreadLocal<ReadVerbHandler.ReadContext> tls_ = new InheritableThreadLocal<ReadVerbHandler.ReadContext>();
    public void doVerb(Message message)
    {
        byte[] body = message.getMessageBody();
        ReadContext readCtx = tls_.get();
        if ( readCtx == null )
        {
            readCtx = new ReadContext();
            tls_.set(readCtx);
        }
        readCtx.bufIn_ = new ByteArrayInputStream(body);
        try
        {
            if (StorageService.instance.isBootstrapMode())
            {
                throw new RuntimeException("Cannot service reads while bootstrapping!");
            }
            ReadCommand command = ReadCommand.serializer().deserialize(new DataInputStream(readCtx.bufIn_));
            Table table = Table.open(command.table);
            Row row = command.getRow(table);
            ReadResponse readResponse;
            if (command.isDigestQuery())
            {
                if (logger_.isDebugEnabled())
                    logger_.debug("digest is " + ByteBufferUtil.bytesToHex(ColumnFamily.digest(row.cf)));
                readResponse = new ReadResponse(ColumnFamily.digest(row.cf));
            }
            else
            {
                readResponse = new ReadResponse(row);
            }
            readCtx.bufOut_.reset();
            ReadResponse.serializer().serialize(readResponse, readCtx.bufOut_);
            byte[] bytes = new byte[readCtx.bufOut_.getLength()];
            System.arraycopy(readCtx.bufOut_.getData(), 0, bytes, 0, bytes.length);
            Message response = message.getReply(FBUtilities.getLocalAddress(), bytes);
            if (logger_.isDebugEnabled())
              logger_.debug(String.format("Read key %s; sending response to %s@%s",
                                          ByteBufferUtil.bytesToHex(command.key), message.getMessageId(), message.getFrom()));
            MessagingService.instance().sendOneWay(response, message.getFrom());
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
