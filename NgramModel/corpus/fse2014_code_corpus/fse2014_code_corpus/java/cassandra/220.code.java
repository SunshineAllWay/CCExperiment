package org.apache.cassandra.db.migration;
import java.io.IOException;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
public class UpdateKeyspace extends Migration
{
    private KSMetaData newKsm;
    private KSMetaData oldKsm;
    protected UpdateKeyspace() { }
    public UpdateKeyspace(KSMetaData ksm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        assert ksm != null;
        assert ksm.cfMetaData() != null;
        if (ksm.cfMetaData().size() > 0)
            throw new ConfigurationException("Updated keyspace must not contain any column families");
        oldKsm = DatabaseDescriptor.getKSMetaData(ksm.name);
        if (oldKsm == null)
            throw new ConfigurationException(ksm.name + " cannot be updated because it doesn't exist.");
        this.newKsm = new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, ksm.replicationFactor, oldKsm.cfMetaData().values().toArray(new CFMetaData[]{}));
        rm = makeDefinitionMutation(newKsm, oldKsm, newVersion);
    }
    void applyModels() throws IOException
    {
        DatabaseDescriptor.clearTableDefinition(oldKsm, newVersion);
        DatabaseDescriptor.setTableDefinition(newKsm, newVersion);
        Table table = Table.open(newKsm.name);
        try
        {
            table.createReplicationStrategy(newKsm);
        }
        catch (ConfigurationException e)
        {
            throw new IOException(e);
        }
        logger.info("Keyspace updated. Please perform any manual operations.");
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.UpdateKeyspace uks = new org.apache.cassandra.db.migration.avro.UpdateKeyspace();
        uks.newKs = newKsm.deflate();
        uks.oldKs = oldKsm.deflate();
        mi.migration = uks;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.UpdateKeyspace uks = (org.apache.cassandra.db.migration.avro.UpdateKeyspace)mi.migration;
        newKsm = KSMetaData.inflate(uks.newKs);
        oldKsm = KSMetaData.inflate(uks.oldKs);
    }
}
