package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.io.ICompactSerializer;
public class VersionedValue implements Comparable<VersionedValue>
{
    public static final ICompactSerializer<VersionedValue> serializer = new VersionedValueSerializer();
    public final static char DELIMITER = ',';
    public final static String DELIMITER_STR = new String(new char[] { DELIMITER });
    public final static String STATUS_BOOTSTRAPPING = "BOOT";
    public final static String STATUS_NORMAL = "NORMAL";
    public final static String STATUS_LEAVING = "LEAVING";
    public final static String STATUS_LEFT = "LEFT";
    public final static String REMOVING_TOKEN = "removing";
    public final static String REMOVED_TOKEN = "removed";
    public final int version;
    public final String value;
    private VersionedValue(String value, int version)
    {
        this.value = value;
        this.version = version;
    }
    private VersionedValue(String value)
    {
        this.value = value;
        version = VersionGenerator.getNextVersion();
    }
    public int compareTo(VersionedValue value)
    {
        return this.version - value.version;
    }
    public static class VersionedValueFactory
    {
        IPartitioner partitioner;
        public VersionedValueFactory(IPartitioner partitioner)
        {
            this.partitioner = partitioner;
        }
        public VersionedValue bootstrapping(Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_BOOTSTRAPPING + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue normal(Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_NORMAL + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue load(double load)
        {
            return new VersionedValue(String.valueOf(load));
        }
        public VersionedValue migration(UUID newVersion)
        {
            return new VersionedValue(newVersion.toString());
        }
        public VersionedValue leaving(Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_LEAVING + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue left(Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_LEFT + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue removingNonlocal(Token localToken, Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_NORMAL
                                        + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(localToken)
                                        + VersionedValue.DELIMITER + VersionedValue.REMOVING_TOKEN
                                        + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue removedNonlocal(Token localToken, Token token)
        {
            return new VersionedValue(VersionedValue.STATUS_NORMAL
                                        + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(localToken)
                                        + VersionedValue.DELIMITER + VersionedValue.REMOVED_TOKEN
                                        + VersionedValue.DELIMITER + partitioner.getTokenFactory().toString(token));
        }
        public VersionedValue datacenter(String dcId)
        {
            return new VersionedValue(dcId);
        }
        public VersionedValue rack(String rackId)
        {
            return new VersionedValue(rackId);
        }
    }
    private static class VersionedValueSerializer implements ICompactSerializer<VersionedValue>
    {
        public void serialize(VersionedValue value, DataOutputStream dos) throws IOException
        {
            dos.writeUTF(value.value);
            dos.writeInt(value.version);
        }
        public VersionedValue deserialize(DataInputStream dis) throws IOException
        {
            String value = dis.readUTF();
            int version = dis.readInt();
            return new VersionedValue(value, version);
        }
    }
}
