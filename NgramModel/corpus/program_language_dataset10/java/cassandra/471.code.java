package org.apache.cassandra.db;
import static org.apache.cassandra.Util.column;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.Util;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.junit.Test;
import org.apache.cassandra.utils.ByteBufferUtil;
public class RecoveryManagerTruncateTest extends CleanupHelper
{
	@Test
	public void testTruncate() throws IOException, ExecutionException, InterruptedException
	{
		Table table = Table.open("Keyspace1");
		ColumnFamilyStore cfs = table.getColumnFamilyStore("Standard1");
		RowMutation rm;
		ColumnFamily cf;
		rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("keymulti"));
		cf = ColumnFamily.create("Keyspace1", "Standard1");
		cf.addColumn(column("col1", "val1", 1L));
		rm.add(cf);
		rm.apply();
		assertNotNull(getFromTable(table, "Standard1", "keymulti", "col1"));
		cfs.truncate().get();
		CommitLog.recover();
		assertNull(getFromTable(table, "Standard1", "keymulti", "col1"));
		rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("keymulti"));
		cf = ColumnFamily.create("Keyspace1", "Standard1");
		cf.addColumn(column("col1", "val1", 1L));
		rm.add(cf);
		rm.apply();
		cfs.forceBlockingFlush();
		cfs.truncate().get();
		CommitLog.recover();
		assertNull(getFromTable(table, "Standard1", "keymulti", "col1"));
	}
	private IColumn getFromTable(Table table, String cfName, String keyName, String columnName)
	{
		ColumnFamily cf;
		ColumnFamilyStore cfStore = table.getColumnFamilyStore(cfName);
		if (cfStore == null)
		{
			return null;
		}
		cf = cfStore.getColumnFamily(QueryFilter.getNamesFilter(
		        Util.dk(keyName), new QueryPath(cfName), ByteBuffer.wrap(columnName.getBytes())));
		if (cf == null)
		{
			return null;
		}
		return cf.getColumn(ByteBuffer.wrap(columnName.getBytes()));
	}
}
