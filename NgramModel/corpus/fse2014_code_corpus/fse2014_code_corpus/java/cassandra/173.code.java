package org.apache.cassandra.db;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
public class Truncation
{
    private static ICompactSerializer<Truncation> serializer;
    public final String keyspace;
    public final String columnFamily;
    static
    {
        serializer = new TruncationSerializer();
    }
    public static ICompactSerializer<Truncation> serializer()
    {
        return serializer;
    }
    public Truncation(String keyspace, String columnFamily)
    {
        this.keyspace = keyspace;
        this.columnFamily = columnFamily;
    }
    public void apply() throws IOException
    {
        Table.open(keyspace).getColumnFamilyStore(columnFamily).truncate();
    }
    public Message makeTruncationMessage() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        serializer().serialize(this, dos);
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.TRUNCATE, bos.toByteArray());
    }
    public String toString()
    {
        return "Truncation(" + "keyspace='" + keyspace + '\'' + ", cf='" + columnFamily + "\')";
    }
}
class TruncationSerializer implements ICompactSerializer<Truncation>
{
    public void serialize(Truncation t, DataOutputStream dos) throws IOException
    {
        dos.writeUTF(t.keyspace);
        dos.writeUTF(t.columnFamily);
    }
    public Truncation deserialize(DataInputStream dis) throws IOException
    {
        String keyspace = dis.readUTF();
        String columnFamily = dis.readUTF();
        return new Truncation(keyspace, columnFamily);
    }
}
