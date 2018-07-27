package org.apache.cassandra.service;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.marshal.AbstractCommutativeType;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class ReadResponseResolver implements IResponseResolver<Row>
{
	private static Logger logger_ = LoggerFactory.getLogger(ReadResponseResolver.class);
    private final String table;
    private final ConcurrentMap<Message, ReadResponse> results = new NonBlockingHashMap<Message, ReadResponse>();
    private DecoratedKey key;
    private ByteBuffer digest;
    public ReadResponseResolver(String table, ByteBuffer key)
    {
        this.table = table;
        this.key = StorageService.getPartitioner().decorateKey(key);
    }
    public Row getData() throws IOException
    {
        for (Map.Entry<Message, ReadResponse> entry : results.entrySet())
        {
            ReadResponse result = entry.getValue();
            if (!result.isDigestQuery())
                return result.row();
        }
        throw new AssertionError("getData should not be invoked when no data is present");
    }
    public Row resolve() throws DigestMismatchException, IOException
    {
        if (logger_.isDebugEnabled())
            logger_.debug("resolving " + results.size() + " responses");
        long startTime = System.currentTimeMillis();
		List<ColumnFamily> versions = new ArrayList<ColumnFamily>();
		List<InetAddress> endpoints = new ArrayList<InetAddress>();
        for (Map.Entry<Message, ReadResponse> entry : results.entrySet())
        {
            ReadResponse result = entry.getValue();
            Message message = entry.getKey();
            if (result.isDigestQuery())
            {
                if (digest == null)
                {
                    digest = result.digest();
                }
                else
                {
                    ByteBuffer digest2 = result.digest();
                    if (!digest.equals(digest2))
                        throw new DigestMismatchException(key, digest, digest2);
                }
            }
            else
            {
                ColumnFamily cf = result.row().cf;
                InetAddress from = message.getFrom();
                if (cf != null)
                {
                    AbstractType defaultValidator = cf.metadata().getDefaultValidator();
                    if (!FBUtilities.getLocalAddress().equals(from) && defaultValidator.isCommutative())
                    {
                        cf = cf.cloneMe();
                        ((AbstractCommutativeType) defaultValidator).cleanContext(cf, FBUtilities.getLocalAddress());
                    }
                }
                versions.add(cf);
                endpoints.add(from);
            }
            results.remove(message);
        }
        if (digest != null)
        {
            for (ColumnFamily cf : versions)
            {
                ByteBuffer digest2 = ColumnFamily.digest(cf);
                if (!digest.equals(digest2))
                    throw new DigestMismatchException(key, digest, digest2);
            }
            if (logger_.isDebugEnabled())
                logger_.debug("digests verified");
        }
        ColumnFamily resolved;
        if (versions.size() > 1)
        {
            resolved = resolveSuperset(versions);
            if (logger_.isDebugEnabled())
                logger_.debug("versions merged");
            maybeScheduleRepairs(resolved, table, key, versions, endpoints);
        }
        else
        {
            resolved = versions.get(0);
        }
        if (logger_.isDebugEnabled())
            logger_.debug("resolve: " + (System.currentTimeMillis() - startTime) + " ms.");
		return new Row(key, resolved);
	}
    public static void maybeScheduleRepairs(ColumnFamily resolved, String table, DecoratedKey key, List<ColumnFamily> versions, List<InetAddress> endpoints)
    {
        for (int i = 0; i < versions.size(); i++)
        {
            ColumnFamily diffCf = ColumnFamily.diff(versions.get(i), resolved);
            if (diffCf == null) 
                continue;
            RowMutation rowMutation = new RowMutation(table, key.key);
            AbstractType defaultValidator = diffCf.metadata().getDefaultValidator();
            if (defaultValidator.isCommutative())
                ((AbstractCommutativeType)defaultValidator).cleanContext(diffCf, endpoints.get(i));
            if (diffCf.getColumnsMap().isEmpty() && !diffCf.isMarkedForDelete())
                continue;
            rowMutation.add(diffCf);
            Message repairMessage;
            try
            {
                repairMessage = rowMutation.makeRowMutationMessage(StorageService.Verb.READ_REPAIR);
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
            MessagingService.instance().sendOneWay(repairMessage, endpoints.get(i));
        }
    }
    static ColumnFamily resolveSuperset(List<ColumnFamily> versions)
    {
        assert versions.size() > 0;
        ColumnFamily resolved = null;
        for (ColumnFamily cf : versions)
        {
            if (cf != null)
            {
                resolved = cf.cloneMeShallow();
                break;
            }
        }
        if (resolved == null)
            return null;
        for (ColumnFamily cf : versions)
            resolved.resolve(cf);
        return resolved;
    }
    public void preprocess(Message message)
    {
        byte[] body = message.getMessageBody();
        ByteArrayInputStream bufIn = new ByteArrayInputStream(body);
        try
        {
            ReadResponse result = ReadResponse.serializer().deserialize(new DataInputStream(bufIn));
            if (logger_.isDebugEnabled())
                logger_.debug("Preprocessed {} response", result.isDigestQuery() ? "digest" : "data");
            results.put(message, result);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public void injectPreProcessed(Message message, ReadResponse result)
    {
        results.put(message, result);
    }
    public boolean isDataPresent()
	{
        for (ReadResponse result : results.values())
        {
            if (!result.isDigestQuery())
                return true;
        }
        return false;
    }
    public Iterable<Message> getMessages()
    {
        return results.keySet();
    }
    public int getMessageCount()
    {
        return results.size();
    }
}
