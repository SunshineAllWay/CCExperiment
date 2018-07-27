package org.apache.cassandra.db;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class WriteResponse 
{
    private static WriteResponseSerializer serializer_ = new WriteResponseSerializer();
    public static WriteResponseSerializer serializer()
    {
        return serializer_;
    }
    public static Message makeWriteResponseMessage(Message original, WriteResponse writeResponseMessage) throws IOException
    {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        WriteResponse.serializer().serialize(writeResponseMessage, dos);
        return original.getReply(FBUtilities.getLocalAddress(), bos.toByteArray());
    }
	private final String table_;
	private final ByteBuffer key_;
	private final boolean status_;
	public WriteResponse(String table, ByteBuffer key, boolean bVal) {
		table_ = table;
		key_ = key;
		status_ = bVal;
	}
	public String table()
	{
		return table_;
	}
	public ByteBuffer key()
	{
		return key_;
	}
	public boolean isSuccess()
	{
		return status_;
	}
    public static class WriteResponseSerializer implements ICompactSerializer<WriteResponse>
    {
        public void serialize(WriteResponse wm, DataOutputStream dos) throws IOException
        {
            dos.writeUTF(wm.table());
            ByteBufferUtil.writeWithShortLength(wm.key(), dos);
            dos.writeBoolean(wm.isSuccess());
        }
        public WriteResponse deserialize(DataInputStream dis) throws IOException
        {
            String table = dis.readUTF();
            ByteBuffer key = ByteBufferUtil.readWithShortLength(dis);
            boolean status = dis.readBoolean();
            return new WriteResponse(table, key, status);
        }
    }
}
