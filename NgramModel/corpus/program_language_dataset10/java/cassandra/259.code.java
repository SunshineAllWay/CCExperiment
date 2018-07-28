package org.apache.cassandra.hadoop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import com.google.common.collect.AbstractIterator;
import org.apache.cassandra.auth.SimpleAuthenticator;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
public class ColumnFamilyRecordReader extends RecordReader<ByteBuffer, SortedMap<ByteBuffer, IColumn>>
{
    private ColumnFamilySplit split;
    private RowIterator iter;
    private Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> currentRow;
    private SlicePredicate predicate;
    private int totalRowCount; 
    private int batchRowCount; 
    private String cfName;
    private String keyspace;
    private TSocket socket;
    private Cassandra.Client client;
    public void close() 
    {
        if (socket != null && socket.isOpen())
        {
            socket.close();
            socket = null;
            client = null;
        }
    }
    public ByteBuffer getCurrentKey()
    {
        return currentRow.left;
    }
    public SortedMap<ByteBuffer, IColumn> getCurrentValue()
    {
        return currentRow.right;
    }
    public float getProgress()
    {
        return ((float)iter.rowsRead()) / totalRowCount;
    }
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException
    {
        this.split = (ColumnFamilySplit) split;
        Configuration conf = context.getConfiguration();
        predicate = ConfigHelper.getInputSlicePredicate(conf);
        totalRowCount = ConfigHelper.getInputSplitSize(conf);
        batchRowCount = ConfigHelper.getRangeBatchSize(conf);
        cfName = ConfigHelper.getInputColumnFamily(conf);
        keyspace = ConfigHelper.getInputKeyspace(conf);
        try
        {
            if (socket != null && socket.isOpen())
                return;
            String location = getLocation();
            socket = new TSocket(location, ConfigHelper.getRpcPort(conf));
            TBinaryProtocol binaryProtocol = new TBinaryProtocol(new TFramedTransport(socket));
            client = new Cassandra.Client(binaryProtocol);
            socket.open();
            client.set_keyspace(keyspace);
            if (ConfigHelper.getInputKeyspaceUserName(conf) != null)
            {
                Map<String, String> creds = new HashMap<String, String>();
                creds.put(SimpleAuthenticator.USERNAME_KEY, ConfigHelper.getInputKeyspaceUserName(conf));
                creds.put(SimpleAuthenticator.PASSWORD_KEY, ConfigHelper.getInputKeyspacePassword(conf));
                AuthenticationRequest authRequest = new AuthenticationRequest(creds);
                client.login(authRequest);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        iter = new RowIterator();
    }
    public boolean nextKeyValue() throws IOException
    {
        if (!iter.hasNext())
            return false;
        currentRow = iter.next();
        return true;
    }
    private String getLocation()
    {
        InetAddress[] localAddresses;
        try
        {
            localAddresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException e)
        {
            throw new AssertionError(e);
        }
        for (InetAddress address : localAddresses)
        {
            for (String location : split.getLocations())
            {
                InetAddress locationAddress = null;
                try
                {
                    locationAddress = InetAddress.getByName(location);
                }
                catch (UnknownHostException e)
                {
                    throw new AssertionError(e);
                }
                if (address.equals(locationAddress))
                {
                    return location;
                }
            }
        }
        return split.getLocations()[0];
    }
    private class RowIterator extends AbstractIterator<Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>>
    {
        private List<KeySlice> rows;
        private String startToken;
        private int totalRead = 0;
        private int i = 0;
        private final AbstractType comparator;
        private final AbstractType subComparator;
        private final IPartitioner partitioner;
        private RowIterator()
        {
            try
            {
                partitioner = FBUtilities.newPartitioner(client.describe_partitioner());
                KsDef ks_def = client.describe_keyspace(keyspace);
                List<String> cfnames = new ArrayList<String>();
                for (CfDef cfd : ks_def.cf_defs)
                    cfnames.add(cfd.name);
                int idx = cfnames.indexOf(cfName);
                CfDef cf_def = ks_def.cf_defs.get(idx);
                comparator = FBUtilities.getComparator(cf_def.comparator_type);
                subComparator = cf_def.subcomparator_type == null ? null : FBUtilities.getComparator(cf_def.subcomparator_type);
            }
            catch (ConfigurationException e)
            {
                throw new RuntimeException("unable to load sub/comparator", e);
            }
            catch (TException e)
            {
                throw new RuntimeException("error communicating via Thrift", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException("unable to load keyspace " + keyspace, e);
            }
        }
        private void maybeInit()
        {
            if (rows != null && i >= rows.size())
                rows = null;
            if (rows != null)
                return;
            if (startToken == null)
            {
                startToken = split.getStartToken();
            } 
            else if (startToken.equals(split.getEndToken()))
            {
                rows = null;
                return;
            }
            KeyRange keyRange = new KeyRange(batchRowCount)
                                .setStart_token(startToken)
                                .setEnd_token(split.getEndToken());
            try
            {
                rows = client.get_range_slices(new ColumnParent(cfName),
                                               predicate,
                                               keyRange,
                                               ConsistencyLevel.ONE);
                if (rows.isEmpty())
                {
                    rows = null;
                    return;
                }
                i = 0;
                KeySlice lastRow = rows.get(rows.size() - 1);
                ByteBuffer rowkey = lastRow.key;
                startToken = partitioner.getTokenFactory().toString(partitioner.getToken(rowkey));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        public int rowsRead()
        {
            return totalRead;
        }
        @Override
        protected Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> computeNext()
        {
            maybeInit();
            if (rows == null)
                return endOfData();
            totalRead++;
            KeySlice ks = rows.get(i++);
            SortedMap<ByteBuffer, IColumn> map = new TreeMap<ByteBuffer, IColumn>(comparator);
            for (ColumnOrSuperColumn cosc : ks.columns)
            {
                IColumn column = unthriftify(cosc);
                map.put(column.name(), column);
            }
            return new Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>(ks.key, map);
        }
        private IColumn unthriftify(ColumnOrSuperColumn cosc)
        {
            if (cosc.column == null)
                return unthriftifySuper(cosc.super_column);
            return unthriftifySimple(cosc.column);
        }
        private IColumn unthriftifySuper(SuperColumn super_column)
        {
            org.apache.cassandra.db.SuperColumn sc = new org.apache.cassandra.db.SuperColumn(super_column.name, subComparator);
            for (Column column : super_column.columns)
            {
                sc.addColumn(unthriftifySimple(column));
            }
            return sc;
        }
        private IColumn unthriftifySimple(Column column)
        {
            return new org.apache.cassandra.db.Column(column.name, column.value, column.timestamp);
        }
    }
}
