package org.apache.cassandra.db.migration;
import java.io.IOException;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.HintedHandOffManager;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
public class DropKeyspace extends Migration
{
    private String name;
    protected DropKeyspace() {  }
    public DropKeyspace(String name) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        this.name = name;
        KSMetaData ksm = DatabaseDescriptor.getTableDefinition(name);
        if (ksm == null)
            throw new ConfigurationException("Keyspace does not exist.");
        rm = makeDefinitionMutation(null, ksm, newVersion);
    }
    @Override
    public void beforeApplyModels()
    {
        if (!clientMode)
            Table.open(name).snapshot(null);
    }
    @Override
    public void applyModels() throws IOException
    {
        acquireLocks();
        try
        {
            KSMetaData ksm = DatabaseDescriptor.getTableDefinition(name);
            Table table = Table.clear(ksm.name);
            if (table == null)
                throw new IOException("Table is not active. " + ksm.name);
            for (CFMetaData cfm : ksm.cfMetaData().values())
            {
                CFMetaData.purge(cfm);
                if (!clientMode)
                {
                    table.dropCf(cfm.cfId);
                }
            }
            DatabaseDescriptor.clearTableDefinition(ksm, newVersion);
            if (!clientMode)
            {
                HintedHandOffManager.renameHints(name, null);
            }
        }
        finally
        {
            releaseLocks();
        }
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.DropKeyspace dks = new org.apache.cassandra.db.migration.avro.DropKeyspace();
        dks.ksname = new org.apache.avro.util.Utf8(name);
        mi.migration = dks;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.DropKeyspace dks = (org.apache.cassandra.db.migration.avro.DropKeyspace)mi.migration;
        name = dks.ksname.toString();
    }
}
