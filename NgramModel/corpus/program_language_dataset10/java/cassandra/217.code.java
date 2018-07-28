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
@Deprecated
public class RenameColumnFamily extends Migration
{
    private String tableName;
    private String oldName;
    private String newName;
    private Integer cfId;
    protected RenameColumnFamily() {  }
    public RenameColumnFamily(String tableName, String oldName, String newName) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        this.tableName = tableName;
        this.oldName = oldName;
        this.newName = newName;
        KSMetaData ksm = DatabaseDescriptor.getTableDefinition(tableName);
        if (ksm == null)
            throw new ConfigurationException("Keyspace does not already exist.");
        if (!ksm.cfMetaData().containsKey(oldName))
            throw new ConfigurationException("CF is not defined in that keyspace.");
        if (ksm.cfMetaData().containsKey(newName))
            throw new ConfigurationException("CF is already defined in that keyspace.");
        if (!Migration.isLegalName(newName))
            throw new ConfigurationException("Invalid column family name: " + newName);
        cfId = ksm.cfMetaData().get(oldName).cfId;
        KSMetaData newKsm = makeNewKeyspaceDefinition(ksm);
        rm = Migration.makeDefinitionMutation(newKsm, null, newVersion);
    }
    private KSMetaData makeNewKeyspaceDefinition(KSMetaData ksm)
    {
        CFMetaData oldCfm = ksm.cfMetaData().get(oldName);
        List<CFMetaData> newCfs = new ArrayList<CFMetaData>(ksm.cfMetaData().values());
        newCfs.remove(oldCfm);
        assert newCfs.size() == ksm.cfMetaData().size() - 1;
        CFMetaData newCfm = CFMetaData.rename(oldCfm, newName);
        newCfs.add(newCfm);
        return new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, ksm.replicationFactor, newCfs.toArray(new CFMetaData[newCfs.size()]));
    }
    @Override
    public void applyModels() throws IOException
    {
        KSMetaData oldKsm = DatabaseDescriptor.getTableDefinition(tableName);
        CFMetaData.purge(oldKsm.cfMetaData().get(oldName));
        KSMetaData ksm = makeNewKeyspaceDefinition(DatabaseDescriptor.getTableDefinition(tableName));
        try 
        {
            CFMetaData.map(ksm.cfMetaData().get(newName));
        }
        catch (ConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
        DatabaseDescriptor.setTableDefinition(ksm, newVersion);
        if (!clientMode)
        {
            Table.open(ksm.name).renameCf(cfId, newName);
        }
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.RenameColumnFamily rcf = new org.apache.cassandra.db.migration.avro.RenameColumnFamily();
        rcf.ksname = new org.apache.avro.util.Utf8(tableName);
        rcf.cfid = cfId;
        rcf.old_cfname = new org.apache.avro.util.Utf8(oldName);
        rcf.new_cfname = new org.apache.avro.util.Utf8(newName);
        mi.migration = rcf;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.RenameColumnFamily rcf = (org.apache.cassandra.db.migration.avro.RenameColumnFamily)mi.migration;
        tableName = rcf.ksname.toString();
        cfId = rcf.cfid;
        oldName = rcf.old_cfname.toString();
        newName = rcf.new_cfname.toString();
    }
}
