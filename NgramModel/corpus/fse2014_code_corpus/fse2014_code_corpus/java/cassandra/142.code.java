package org.apache.cassandra.db;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.cassandra.utils.WrappedRunnable;
public class DefinitionsUpdateResponseVerbHandler implements IVerbHandler
{
    private static final Logger logger = LoggerFactory.getLogger(DefinitionsUpdateResponseVerbHandler.class);
    public void doVerb(final Message message)
    {
        try
        {
            Collection<Column> cols = MigrationManager.makeColumns(message);
            for (Column col : cols)
            {
                final UUID version = UUIDGen.getUUID(col.name());
                if (version.timestamp() > DatabaseDescriptor.getDefsVersion().timestamp())
                {
                    final Migration m = Migration.deserialize(col.value());
                    assert m.getVersion().equals(version);
                    StageManager.getStage(Stage.MIGRATION).submit(new WrappedRunnable()
                    {
                        @Override
                        protected void runMayThrow() throws Exception
                        {
                            if (DatabaseDescriptor.getDefsVersion().timestamp() == version.timestamp())
                                logger.debug("Not appling (equal) " + version.toString());
                            else if (DatabaseDescriptor.getDefsVersion().timestamp() > version.timestamp())
                                logger.debug("Not applying (before)" + version.toString());
                            else
                            {
                                logger.debug("Applying {} from {}", m.getClass().getSimpleName(), message.getFrom());
                                try
                                {
                                    m.apply();
                                }
                                catch (ConfigurationException ex)
                                {
                                    logger.debug("Migration not applied " + ex.getMessage());
                                }
                            }
                        }
                    });
                }
            }
        }
        catch (IOException ex)
        {
            throw new IOError(ex);
        }
    }
}
