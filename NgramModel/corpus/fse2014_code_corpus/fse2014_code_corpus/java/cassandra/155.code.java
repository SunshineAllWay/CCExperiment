package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.FBUtilities;
public class RangeSliceReply
{
    public final List<Row> rows;
    public RangeSliceReply(List<Row> rows)
    {
        this.rows = rows;
    }
    public Message getReply(Message originalMessage) throws IOException
    {
        DataOutputBuffer dob = new DataOutputBuffer();
        dob.writeInt(rows.size());
        for (Row row : rows)
        {
            Row.serializer().serialize(row, dob);
        }
        byte[] data = Arrays.copyOf(dob.getData(), dob.getLength());
        return originalMessage.getReply(FBUtilities.getLocalAddress(), data);
    }
    @Override
    public String toString()
    {
        return "RangeSliceReply{" +
               "rows=" + StringUtils.join(rows, ",") +
               '}';
    }
    public static RangeSliceReply read(byte[] body) throws IOException
    {
        ByteArrayInputStream bufIn = new ByteArrayInputStream(body);
        DataInputStream dis = new DataInputStream(bufIn);
        int rowCount = dis.readInt();
        List<Row> rows = new ArrayList<Row>(rowCount);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Row.serializer().deserialize(dis));
        }
        return new RangeSliceReply(rows);
    }
}
