package org.apache.cassandra.db;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.FBUtilities;
public class TruncateResponse
{
    private static TruncateResponseSerializer serializer_ = new TruncateResponseSerializer();
    public static TruncateResponseSerializer serializer()
    {
        return serializer_;
    }
    public final String keyspace;
    public final String columnFamily;
    public final boolean success;
    public static Message makeTruncateResponseMessage(Message original, TruncateResponse truncateResponseMessage)
            throws IOException
    {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        TruncateResponse.serializer().serialize(truncateResponseMessage, dos);
        return original.getReply(FBUtilities.getLocalAddress(), bos.toByteArray());
    }
    public TruncateResponse(String keyspace, String columnFamily, boolean success) {
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;
		this.success = success;
	}
    public static class TruncateResponseSerializer implements ICompactSerializer<TruncateResponse>
    {
        public void serialize(TruncateResponse tr, DataOutputStream dos) throws IOException
        {
            dos.writeUTF(tr.keyspace);
            dos.writeUTF(tr.columnFamily);
            dos.writeBoolean(tr.success);
        }
        public TruncateResponse deserialize(DataInputStream dis) throws IOException
        {
            String keyspace = dis.readUTF();
            String columnFamily = dis.readUTF();
            boolean success = dis.readBoolean();
            return new TruncateResponse(keyspace, columnFamily, success);
        }
    }
}
