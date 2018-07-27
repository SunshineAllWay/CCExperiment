package org.apache.cassandra.db.migration;
import java.io.IOException;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
public class AddKeyspace extends Migration
{
    private KSMetaData ksm;
    protected AddKeyspace() {  }
    public AddKeyspace(KSMetaData ksm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        if (DatabaseDescriptor.getTableDefinition(ksm.name) != null)
            throw new ConfigurationException("Keyspace already exists.");
        if (!Migration.isLegalName(ksm.name))
            throw new ConfigurationException("Invalid keyspace name: " + ksm.name);
        for (CFMetaData cfm : ksm.cfMetaData().values())
            if (!Migration.isLegalName(cfm.cfName))
                throw new ConfigurationException("Invalid column family name: " + cfm.cfName);
        this.ksm = ksm;
        rm = makeDefinitionMutation(ksm, null, newVersion);
    }
    @Override
    public void applyModels() throws IOException
    {
        for (CFMetaData cfm : ksm.cfMetaData().values())
        {
            try
            {
                CFMetaData.map(cfm);
            }
            catch (ConfigurationException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        DatabaseDescriptor.setTableDefinition(ksm, newVersion);
        CFMetaData.fixMaxId();
        if (!clientMode)
        {
            Table.open(ksm.name);
        }
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.AddKeyspace aks = new org.apache.cassandra.db.migration.avro.AddKeyspace();
        aks.ks = ksm.deflate();
        mi.migration = aks;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.AddKeyspace aks = (org.apache.cassandra.db.migration.avro.AddKeyspace)mi.migration;
        ksm = KSMetaData.inflate(aks.ks);
    }
}
