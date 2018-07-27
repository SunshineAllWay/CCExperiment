package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOError;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
public class ReadRepairVerbHandler implements IVerbHandler
{
    private static Logger logger_ = LoggerFactory.getLogger(ReadRepairVerbHandler.class);    
    public void doVerb(Message message)
    {          
        byte[] body = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(body);
        try
        {
            RowMutation rm = RowMutation.serializer().deserialize(new DataInputStream(buffer));
            rm.apply();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
