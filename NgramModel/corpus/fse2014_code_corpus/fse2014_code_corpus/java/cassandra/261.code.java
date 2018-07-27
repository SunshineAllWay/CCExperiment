package org.apache.cassandra.hadoop;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
public class ColumnFamilySplit extends InputSplit implements Writable
{
    private String startToken;
    private String endToken;
    private String[] dataNodes;
    public ColumnFamilySplit(String startToken, String endToken, String[] dataNodes)
    {
        assert startToken != null;
        assert endToken != null;
        this.startToken = startToken;
        this.endToken = endToken;
        this.dataNodes = dataNodes;
    }
    public String getStartToken()
    {
        return startToken;
    }
    public String getEndToken()
    {
        return endToken;
    }
    public long getLength()
    {
        return 0;
    }
    public String[] getLocations()
    {
        return dataNodes;
    }
    protected ColumnFamilySplit() {}
    public void write(DataOutput out) throws IOException
    {
        out.writeUTF(startToken);
        out.writeUTF(endToken);
        out.writeInt(dataNodes.length);
        for (String endpoint : dataNodes)
        {
            out.writeUTF(endpoint);
        }
    }
    public void readFields(DataInput in) throws IOException
    {
        startToken = in.readUTF();
        endToken = in.readUTF();
        int numOfEndpoints = in.readInt();
        dataNodes = new String[numOfEndpoints];
        for(int i = 0; i < numOfEndpoints; i++)
        {
            dataNodes[i] = in.readUTF();
        }
    }
    @Override
    public String toString()
    {
        return "ColumnFamilySplit{" +
               "startToken='" + startToken + '\'' +
               ", endToken='" + endToken + '\'' +
               ", dataNodes=" + (dataNodes == null ? null : Arrays.asList(dataNodes)) +
               '}';
    }
    public static ColumnFamilySplit read(DataInput in) throws IOException
    {
        ColumnFamilySplit w = new ColumnFamilySplit();
        w.readFields(in);
        return w;
    }
}
