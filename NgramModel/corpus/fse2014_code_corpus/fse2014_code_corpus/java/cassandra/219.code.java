package org.apache.cassandra.db.migration;
import java.io.IOException;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
public class UpdateColumnFamily extends Migration
{
    private CFMetaData metadata;
    protected UpdateColumnFamily() { }
    public UpdateColumnFamily(org.apache.cassandra.db.migration.avro.CfDef cf_def) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        KSMetaData ksm = DatabaseDescriptor.getTableDefinition(cf_def.keyspace.toString());
        if (ksm == null)
            throw new ConfigurationException("Keyspace does not already exist.");
        CFMetaData oldCfm = DatabaseDescriptor.getCFMetaData(CFMetaData.getId(cf_def.keyspace.toString(), cf_def.name.toString()));
        this.metadata = CFMetaData.inflate(oldCfm.deflate());
        this.metadata.apply(cf_def);
        KSMetaData newKsMeta = KSMetaData.inflate(ksm.deflate());
        newKsMeta.cfMetaData().get(cf_def.name.toString()).apply(cf_def);
        rm = Migration.makeDefinitionMutation(newKsMeta, null, newVersion);
    }
    public void beforeApplyModels()
    {
        if (clientMode)
            return;
        ColumnFamilyStore cfs = Table.open(metadata.tableName).getColumnFamilyStore(metadata.cfName);
        cfs.snapshot(Table.getTimestampedSnapshotName(null));
    }
    void applyModels() throws IOException
    {
        logger.debug("Updating " + DatabaseDescriptor.getCFMetaData(metadata.cfId) + " to " + metadata);
        try 
        {
            DatabaseDescriptor.getCFMetaData(metadata.cfId).apply(CFMetaData.convertToAvro(metadata));
        } 
        catch (ConfigurationException ex) 
        {
            throw new IOException(ex);
        }
        DatabaseDescriptor.setTableDefinition(null, newVersion);
        if (!clientMode)
        {
            Table table = Table.open(metadata.tableName);
            ColumnFamilyStore oldCfs = table.getColumnFamilyStore(metadata.cfName);
            oldCfs.reload();
        }
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.UpdateColumnFamily update = new org.apache.cassandra.db.migration.avro.UpdateColumnFamily();
        update.metadata = metadata.deflate();
        mi.migration = update;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.UpdateColumnFamily update = (org.apache.cassandra.db.migration.avro.UpdateColumnFamily)mi.migration;
        metadata = CFMetaData.inflate(update.metadata);
    }
}
