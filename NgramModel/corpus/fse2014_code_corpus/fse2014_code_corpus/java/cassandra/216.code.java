package org.apache.cassandra.db.migration;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.io.SerDeUtils;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.UUIDGen;
import static com.google.common.base.Charsets.UTF_8;
public abstract class Migration
{
    protected static final Logger logger = LoggerFactory.getLogger(Migration.class);
    public static final String NAME_VALIDATOR_REGEX = "\\w+";
    public static final String MIGRATIONS_CF = "Migrations";
    public static final String SCHEMA_CF = "Schema";
    public static final ByteBuffer MIGRATIONS_KEY = ByteBufferUtil.bytes("Migrations Key");
    public static final ByteBuffer LAST_MIGRATION_KEY = ByteBufferUtil.bytes("Last Migration");
    protected RowMutation rm;
    protected UUID newVersion;
    protected UUID lastVersion;
    protected transient boolean clientMode;
    protected Migration() {  }
    Migration(UUID newVersion, UUID lastVersion)
    {
        this.newVersion = newVersion;
        this.lastVersion = lastVersion;
        clientMode = StorageService.instance.isClientMode();
    }
    protected final void acquireLocks()
    {
        CompactionManager.instance.getCompactionLock().lock();
        Table.getFlushLock().lock();
    }
    protected final void releaseLocks()
    {
        Table.getFlushLock().unlock();
        CompactionManager.instance.getCompactionLock().unlock();
    }
    public void beforeApplyModels() {}
    public final void apply() throws IOException, ConfigurationException
    {
        if (!DatabaseDescriptor.getDefsVersion().equals(lastVersion))
            throw new ConfigurationException("Previous version mismatch. cannot apply.");
        assert rm != null;
        if (!clientMode)
            rm.apply();
        beforeApplyModels();
        if (!clientMode)
        {
            long now = System.currentTimeMillis();
            ByteBuffer buf = serialize();
            RowMutation migration = new RowMutation(Table.SYSTEM_TABLE, MIGRATIONS_KEY);
            migration.add(new QueryPath(MIGRATIONS_CF, null, ByteBuffer.wrap(UUIDGen.decompose(newVersion))), buf, now);
            migration.apply();
            logger.debug("Applying migration " + newVersion.toString());
            migration = new RowMutation(Table.SYSTEM_TABLE, LAST_MIGRATION_KEY);
            migration.add(new QueryPath(SCHEMA_CF, null, LAST_MIGRATION_KEY), ByteBuffer.wrap(UUIDGen.decompose(newVersion)), now);
            migration.apply();
            ColumnFamilyStore[] schemaStores = new ColumnFamilyStore[] {
                Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(Migration.MIGRATIONS_CF),
                Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(Migration.SCHEMA_CF)
            };
            List<Future> flushes = new ArrayList<Future>();
            for (ColumnFamilyStore cfs : schemaStores)
                flushes.add(cfs.forceFlush());
            for (Future f : flushes)
            {
                if (f == null)
                    continue;
                try
                {
                    f.get();
                }
                catch (ExecutionException e)
                {
                    throw new IOException(e);
                }
                catch (InterruptedException e)
                {
                    throw new IOException(e);
                }
            }
        }
        applyModels(); 
    }
    public final void announce()
    {
        if (StorageService.instance.isClientMode())
            return;
        MigrationManager.announce(newVersion, Gossiper.instance.getLiveMembers());
    }
    public static UUID getLastMigrationId()
    {
        DecoratedKey dkey = StorageService.getPartitioner().decorateKey(LAST_MIGRATION_KEY);
        Table defs = Table.open(Table.SYSTEM_TABLE);
        ColumnFamilyStore cfStore = defs.getColumnFamilyStore(SCHEMA_CF);
        QueryFilter filter = QueryFilter.getNamesFilter(dkey, new QueryPath(SCHEMA_CF), LAST_MIGRATION_KEY);
        ColumnFamily cf = cfStore.getColumnFamily(filter);
        if (cf == null || cf.getColumnNames().size() == 0)
            return null;
        else
            return UUIDGen.getUUID(cf.getColumn(LAST_MIGRATION_KEY).value());
    }
    abstract void applyModels() throws IOException;
    public abstract void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi);
    public abstract void subinflate(org.apache.cassandra.db.migration.avro.Migration mi);
    public UUID getVersion()
    {
        return newVersion;
    }
    static RowMutation makeDefinitionMutation(KSMetaData add, KSMetaData remove, UUID versionId) throws IOException
    {
        List<KSMetaData> ksms = new ArrayList<KSMetaData>();
        for (String tableName : DatabaseDescriptor.getNonSystemTables())
        {
            if (remove != null && remove.name.equals(tableName) || add != null && add.name.equals(tableName))
                continue;
            ksms.add(DatabaseDescriptor.getTableDefinition(tableName));
        }
        if (add != null)
            ksms.add(add);
        RowMutation rm = new RowMutation(Table.SYSTEM_TABLE, toUTF8Bytes(versionId));
        long now = System.currentTimeMillis();
        for (KSMetaData ksm : ksms)
            rm.add(new QueryPath(SCHEMA_CF, null, ByteBuffer.wrap(ksm.name.getBytes(UTF_8))), SerDeUtils.serialize(ksm.deflate()), now);
        rm.add(new QueryPath(SCHEMA_CF,
                             null,
                             DefsTable.DEFINITION_SCHEMA_COLUMN_NAME),
                             ByteBuffer.wrap(org.apache.cassandra.db.migration.avro.KsDef.SCHEMA$.toString().getBytes(UTF_8)),
                             now);
        return rm;
    }
    public ByteBuffer serialize() throws IOException
    {
        org.apache.cassandra.db.migration.avro.Migration mi = new org.apache.cassandra.db.migration.avro.Migration();
        mi.old_version = new org.apache.cassandra.utils.avro.UUID();
        mi.old_version.bytes(UUIDGen.decompose(lastVersion));
        mi.new_version = new org.apache.cassandra.utils.avro.UUID();
        mi.new_version.bytes(UUIDGen.decompose(newVersion));
        mi.classname = new org.apache.avro.util.Utf8(this.getClass().getName());
        DataOutputBuffer dob = new DataOutputBuffer();
        try
        {
            RowMutation.serializer().serialize(rm, dob);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        mi.row_mutation = ByteBuffer.wrap(dob.asByteArray());
        this.subdeflate(mi);
        return SerDeUtils.serializeWithSchema(mi);
    }
    public static Migration deserialize(ByteBuffer bytes) throws IOException
    {
        org.apache.cassandra.db.migration.avro.Migration mi = SerDeUtils.deserializeWithSchema(bytes, new org.apache.cassandra.db.migration.avro.Migration());
        Migration migration;
        try
        {
            Class migrationClass = Class.forName(mi.classname.toString());
            Constructor migrationConstructor = migrationClass.getDeclaredConstructor();
            migrationConstructor.setAccessible(true);
            migration = (Migration)migrationConstructor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Invalid migration class: " + mi.classname.toString(), e);
        }
        migration.lastVersion = UUIDGen.getUUID(ByteBuffer.wrap(mi.old_version.bytes()));
        migration.newVersion = UUIDGen.getUUID(ByteBuffer.wrap(mi.new_version.bytes()));
        try
        {
            migration.rm = RowMutation.serializer().deserialize(SerDeUtils.createDataInputStream(mi.row_mutation));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        migration.subinflate(mi);
        return migration;
    }
    public static Collection<IColumn> getLocalMigrations(UUID start, UUID end)
    {
        DecoratedKey dkey = StorageService.getPartitioner().decorateKey(MIGRATIONS_KEY);
        Table defs = Table.open(Table.SYSTEM_TABLE);
        ColumnFamilyStore cfStore = defs.getColumnFamilyStore(Migration.MIGRATIONS_CF);
        QueryFilter filter = QueryFilter.getSliceFilter(dkey, new QueryPath(MIGRATIONS_CF), ByteBuffer.wrap(UUIDGen.decompose(start)), ByteBuffer.wrap(UUIDGen.decompose(end)), false, 1000);   
        ColumnFamily cf = cfStore.getColumnFamily(filter);
        return cf.getSortedColumns();
    }
    public static ByteBuffer toUTF8Bytes(UUID version)
    {
        return ByteBuffer.wrap(version.toString().getBytes(UTF_8));
    }
    public static boolean isLegalName(String s)
    {
        return s.matches(Migration.NAME_VALIDATOR_REGEX);
    }
}
