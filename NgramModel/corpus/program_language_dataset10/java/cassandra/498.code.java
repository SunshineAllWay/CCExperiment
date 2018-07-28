package org.apache.cassandra.hadoop;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
public class ColumnFamilyInputFormatTest
{
    @Test
    public void testSlicePredicate()
    {
        long columnValue = 1271253600000l;
        ByteBuffer columnBytes = ByteBufferUtil.bytes(columnValue);
        List<ByteBuffer> columnNames = new ArrayList<ByteBuffer>();
        columnNames.add(columnBytes);
        SlicePredicate originalPredicate = new SlicePredicate().setColumn_names(columnNames);
        Configuration conf = new Configuration();
        ConfigHelper.setInputSlicePredicate(conf, originalPredicate);
        SlicePredicate rtPredicate = ConfigHelper.getInputSlicePredicate(conf);
        assert rtPredicate.column_names.size() == 1;
        assert originalPredicate.column_names.get(0).equals(rtPredicate.column_names.get(0));
    }
}
