package org.apache.cassandra.service;
import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.gms.*;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
public class MigrationManager implements IEndpointStateChangeSubscriber
{
    private static final Logger logger = LoggerFactory.getLogger(MigrationManager.class);
    public void onJoin(InetAddress endpoint, EndpointState epState) { }
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value)
    {
        if (state != ApplicationState.SCHEMA)
            return;
        UUID theirVersion = UUID.fromString(value.value);
        rectify(theirVersion, endpoint);
    }
    public void onAlive(InetAddress endpoint, EndpointState state)
    { 
        VersionedValue value = state.getApplicationState(ApplicationState.SCHEMA);
        if (value != null)
        {
            UUID theirVersion = UUID.fromString(value.value);
            rectify(theirVersion, endpoint);
        }
    }
    public void onDead(InetAddress endpoint, EndpointState state) { }
    public void onRemove(InetAddress endpoint) { }
    public static void rectify(UUID theirVersion, InetAddress endpoint)
    {
        UUID myVersion = DatabaseDescriptor.getDefsVersion();
        if (theirVersion.timestamp() == myVersion.timestamp())
            return;
        else if (theirVersion.timestamp() > myVersion.timestamp())
        {
            logger.debug("My data definitions are old. Asking for updates since {}", myVersion.toString());
            announce(myVersion, Collections.singleton(endpoint));
        }
        else
        {
            logger.debug("Their data definitions are old. Sending updates since {}", theirVersion.toString());
            pushMigrations(theirVersion, myVersion, endpoint);
        }
    }
    public static void announce(UUID version, Set<InetAddress> hosts)
    {
        Message msg = makeVersionMessage(version);
        for (InetAddress host : hosts)
            MessagingService.instance().sendOneWay(msg, host);
        if (!StorageService.instance.isClientMode())
            Gossiper.instance.addLocalApplicationState(ApplicationState.SCHEMA, StorageService.valueFactory.migration(version));
    }
    public static void applyMigrations(final UUID from, final UUID to) throws IOException
    {
        List<Future> updates = new ArrayList<Future>();
        Collection<IColumn> migrations = Migration.getLocalMigrations(from, to);
        for (IColumn col : migrations)
        {
            final Migration migration = Migration.deserialize(col.value());
            Future update = StageManager.getStage(Stage.MIGRATION).submit(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        migration.apply();
                    }
                    catch (ConfigurationException ex)
                    {
                        logger.debug("Migration not applied " + ex.getMessage());
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            });
            updates.add(update);
        }
        for (Future f : updates)
        {
            try
            {
                f.get();
            }
            catch (InterruptedException e)
            {
                throw new IOException(e);
            }
            catch (ExecutionException e)
            {
                throw new IOException(e);
            }
        }
    }
    public static void pushMigrations(UUID from, UUID to, InetAddress host)
    {
        Collection<IColumn> migrations = Migration.getLocalMigrations(from, to);
        try
        {
            Message msg = makeMigrationMessage(migrations);
            MessagingService.instance().sendOneWay(msg, host);
        }
        catch (IOException ex)
        {
            throw new IOError(ex);
        }
    }
    private static Message makeVersionMessage(UUID version)
    {
        byte[] body = version.toString().getBytes();
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.DEFINITIONS_ANNOUNCE, body);
    }
    private static Message makeMigrationMessage(Collection<IColumn> migrations) throws IOException
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeInt(migrations.size());
        for (IColumn col : migrations)
        {
            assert col instanceof Column;
            ByteBufferUtil.writeWithLength(col.name(), dout);
            ByteBufferUtil.writeWithLength(col.value(), dout);
        }
        dout.close();
        byte[] body = bout.toByteArray();
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.DEFINITIONS_UPDATE_RESPONSE, body);
    }
    public static Collection<Column> makeColumns(Message msg) throws IOException
    {
        Collection<Column> cols = new ArrayList<Column>();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(msg.getMessageBody()));
        int count = in.readInt();
        for (int i = 0; i < count; i++)
        {
            byte[] name = new byte[in.readInt()];
            in.readFully(name);
            byte[] value = new byte[in.readInt()];
            in.readFully(value);
            cols.add(new Column(ByteBuffer.wrap(name), ByteBuffer.wrap(value)));
        }
        in.close();
        return cols;
    }
}
