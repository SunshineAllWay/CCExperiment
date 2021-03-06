package org.apache.cassandra.db;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.avro.Schema;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.io.SerDeUtils;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.UUIDGen;
import static com.google.common.base.Charsets.UTF_8;
public class DefsTable
{
    public static final ByteBuffer DEFINITION_SCHEMA_COLUMN_NAME = ByteBufferUtil.bytes("Avro/Schema");
    public static synchronized void dumpToStorage(UUID version) throws IOException
    {
        final ByteBuffer versionKey = Migration.toUTF8Bytes(version);
        Collection<String> ksnames = DatabaseDescriptor.getNonSystemTables();
        RowMutation rm = new RowMutation(Table.SYSTEM_TABLE, versionKey);
        long now = System.currentTimeMillis();
        for (String ksname : ksnames)
        {
            KSMetaData ksm = DatabaseDescriptor.getTableDefinition(ksname);
            rm.add(new QueryPath(Migration.SCHEMA_CF, null, ByteBuffer.wrap(ksm.name.getBytes(UTF_8))), SerDeUtils.serialize(ksm.deflate()), now);
        }
        rm.add(new QueryPath(Migration.SCHEMA_CF,
                             null,
                             DEFINITION_SCHEMA_COLUMN_NAME),
                             ByteBuffer.wrap(org.apache.cassandra.db.migration.avro.KsDef.SCHEMA$.toString().getBytes(UTF_8)),
                             now);
        rm.apply();
        rm = new RowMutation(Table.SYSTEM_TABLE, Migration.LAST_MIGRATION_KEY);
        rm.add(new QueryPath(Migration.SCHEMA_CF, null, Migration.LAST_MIGRATION_KEY),
               ByteBuffer.wrap(UUIDGen.decompose(version)),
               now);
        rm.apply();
    }
    public static synchronized Collection<KSMetaData> loadFromStorage(UUID version) throws IOException
    {
        DecoratedKey vkey = StorageService.getPartitioner().decorateKey(Migration.toUTF8Bytes(version));
        Table defs = Table.open(Table.SYSTEM_TABLE);
        ColumnFamilyStore cfStore = defs.getColumnFamilyStore(Migration.SCHEMA_CF);
        QueryFilter filter = QueryFilter.getIdentityFilter(vkey, new QueryPath(Migration.SCHEMA_CF));
        ColumnFamily cf = cfStore.getColumnFamily(filter);
        IColumn avroschema = cf.getColumn(DEFINITION_SCHEMA_COLUMN_NAME);
        if (avroschema == null)
            throw new RuntimeException("Cannot read system table! Are you upgrading a pre-release version?");
        ByteBuffer value = avroschema.value();
        Schema schema = Schema.parse(ByteBufferUtil.string(value));
        Collection<KSMetaData> keyspaces = new ArrayList<KSMetaData>();
        for (IColumn column : cf.getSortedColumns())
        {
            if (column.name().equals(DEFINITION_SCHEMA_COLUMN_NAME))
                continue;
            org.apache.cassandra.db.migration.avro.KsDef ks = SerDeUtils.deserialize(schema, column.value(), new org.apache.cassandra.db.migration.avro.KsDef());
            keyspaces.add(KSMetaData.inflate(ks));
        }
        return keyspaces;
    }
    public static Set<File> getFiles(String table, final String cf)
    {
        Set<File> found = new HashSet<File>();
        for (String path : DatabaseDescriptor.getAllDataFileLocationsForTable(table))
        {
            File[] dbFiles = new File(path).listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().startsWith(cf + "-") && pathname.getName().endsWith(".db") && pathname.exists();
                }
            });
            found.addAll(Arrays.asList(dbFiles));
        }
        return found;
    }
}
