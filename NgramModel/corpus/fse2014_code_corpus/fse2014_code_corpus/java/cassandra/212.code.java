package org.apache.cassandra.db.migration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
public class AddColumnFamily extends Migration
{
    private CFMetaData cfm;
    protected AddColumnFamily() {  }
    public AddColumnFamily(CFMetaData cfm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        this.cfm = cfm;
        KSMetaData ksm = DatabaseDescriptor.getTableDefinition(cfm.tableName);
        if (ksm == null)
            throw new ConfigurationException("Keyspace does not already exist.");
        else if (ksm.cfMetaData().containsKey(cfm.cfName))
            throw new ConfigurationException("CF is already defined in that keyspace.");
        else if (!Migration.isLegalName(cfm.cfName))
            throw new ConfigurationException("Invalid column family name: " + cfm.cfName);
        KSMetaData newKsm = makeNewKeyspaceDefinition(ksm);
        rm = Migration.makeDefinitionMutation(newKsm, null, newVersion);
    }
    private KSMetaData makeNewKeyspaceDefinition(KSMetaData ksm)
    {
        List<CFMetaData> newCfs = new ArrayList<CFMetaData>(ksm.cfMetaData().values());
        newCfs.add(cfm);
        return new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, ksm.replicationFactor, newCfs.toArray(new CFMetaData[newCfs.size()]));
    }
    public void applyModels() throws IOException
    {
        KSMetaData ksm = DatabaseDescriptor.getTableDefinition(cfm.tableName);
        ksm = makeNewKeyspaceDefinition(ksm);
        try
        {
            CFMetaData.map(cfm);
        }
        catch (ConfigurationException ex)
        {
            throw new IOException(ex);
        }
        Table.open(cfm.tableName); 
        DatabaseDescriptor.setTableDefinition(ksm, newVersion);
        CFMetaData.fixMaxId();
        if (!clientMode)
            Table.open(ksm.name).initCf(cfm.cfId, cfm.cfName);
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.AddColumnFamily acf = new org.apache.cassandra.db.migration.avro.AddColumnFamily();
        acf.cf = cfm.deflate();
        mi.migration = acf;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.AddColumnFamily acf = (org.apache.cassandra.db.migration.avro.AddColumnFamily)mi.migration;
        cfm = CFMetaData.inflate(acf.cf);
    }
}
