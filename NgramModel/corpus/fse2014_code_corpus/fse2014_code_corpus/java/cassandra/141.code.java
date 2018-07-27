package org.apache.cassandra.db;
import java.util.UUID;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.MigrationManager;
public class DefinitionsAnnounceVerbHandler implements IVerbHandler
{
    public void doVerb(Message message)
    {
        UUID theirVersion = UUID.fromString(new String(message.getMessageBody()));
        MigrationManager.rectify(theirVersion, message.getFrom());
    } 
}
