package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.cassandra.Util.column;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.Util;
import org.apache.cassandra.db.commitlog.CommitLog;
public class RecoveryManager2Test extends CleanupHelper
{
    private static Logger logger = LoggerFactory.getLogger(RecoveryManager2Test.class);
    @Test
    public void testWithFlush() throws Exception
    {
        CompactionManager.instance.disableAutoCompaction();
        insertRow("Standard2", "key");
        for (int i = 0; i < 100; i++)
        {
            String key = "key" + i;
            insertRow("Standard1", key);
        }
        Table table1 = Table.open("Keyspace1");
        ColumnFamilyStore cfs = table1.getColumnFamilyStore("Standard1");
        logger.debug("forcing flush");
        cfs.forceBlockingFlush();
        cfs.clearUnsafe();
        logger.debug("begin manual replay");
        CommitLog.instance.resetUnsafe();
        CommitLog.recover();
        assert Util.getRangeSlice(cfs).isEmpty();
    }
    private void insertRow(String cfname, String key) throws IOException
    {
        RowMutation rm = new RowMutation("Keyspace1", ByteBuffer.wrap(key.getBytes()));
        ColumnFamily cf = ColumnFamily.create("Keyspace1", cfname);
        cf.addColumn(column("col1", "val1", 1L));
        rm.add(cf);
        rm.apply();
    }
}
