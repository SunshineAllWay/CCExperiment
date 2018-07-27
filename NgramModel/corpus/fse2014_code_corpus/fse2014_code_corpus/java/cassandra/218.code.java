package org.apache.cassandra.db.migration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.HintedHandOffManager;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
@Deprecated
public class RenameKeyspace extends Migration
{
    private String oldName;
    private String newName;
    protected RenameKeyspace() {  }
    public RenameKeyspace(String oldName, String newName) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), DatabaseDescriptor.getDefsVersion());
        this.oldName = oldName;
        this.newName = newName;
        KSMetaData oldKsm = DatabaseDescriptor.getTableDefinition(oldName);
        if (oldKsm == null)
            throw new ConfigurationException("Keyspace either does not exist or does not match the one currently defined.");
        if (DatabaseDescriptor.getTableDefinition(newName) != null)
            throw new ConfigurationException("Keyspace already exists.");
        if (!Migration.isLegalName(newName))
            throw new ConfigurationException("Invalid keyspace name: " + newName);
        KSMetaData newKsm = rename(oldKsm, newName, false); 
        rm = makeDefinitionMutation(newKsm, oldKsm, newVersion);
    }
    private static KSMetaData rename(KSMetaData ksm, String newName, boolean purgeOldCfs)
    {
        List<CFMetaData> newCfs = new ArrayList<CFMetaData>(ksm.cfMetaData().size());
        for (CFMetaData oldCf : ksm.cfMetaData().values())
        {
            if (purgeOldCfs)
                CFMetaData.purge(oldCf);
            newCfs.add(CFMetaData.renameTable(oldCf, newName));
        }
        return new KSMetaData(newName, ksm.strategyClass, ksm.strategyOptions, ksm.replicationFactor, newCfs.toArray(new CFMetaData[newCfs.size()]));
    }
    @Override
    public void applyModels() throws IOException
    {
        if (!clientMode)
            renameKsStorageFiles(oldName, newName);
        KSMetaData oldKsm = DatabaseDescriptor.getTableDefinition(oldName);
        for (CFMetaData cfm : oldKsm.cfMetaData().values())
            CFMetaData.purge(cfm);
        KSMetaData newKsm = rename(oldKsm, newName, true);
        for (CFMetaData cfm : newKsm.cfMetaData().values())
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
        DatabaseDescriptor.clearTableDefinition(oldKsm, newVersion);
        DatabaseDescriptor.setTableDefinition(newKsm, newVersion);
        if (!clientMode)
        {
            Table.clear(oldKsm.name);
            Table.open(newName);
            HintedHandOffManager.renameHints(oldName, newName);
        }
    }
    private static void renameKsStorageFiles(String oldKs, String newKs) throws IOException
    {
        ArrayList<File> failed = new ArrayList<File>();
        for (String dataDir : DatabaseDescriptor.getAllDataFileLocations())
        {
            File ksDir = new File(dataDir, oldKs);
            if (ksDir.exists())
                if (!ksDir.renameTo(new File(dataDir, newKs)))
                    failed.add(ksDir);
        }
        if (!failed.isEmpty())
            throw new IOException("One or more problems encountered while renaming " + StringUtils.join(failed, ","));
    }
    public void subdeflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.RenameKeyspace rks = new org.apache.cassandra.db.migration.avro.RenameKeyspace();
        rks.old_ksname = new org.apache.avro.util.Utf8(oldName);
        rks.new_ksname = new org.apache.avro.util.Utf8(newName);
        mi.migration = rks;
    }
    public void subinflate(org.apache.cassandra.db.migration.avro.Migration mi)
    {
        org.apache.cassandra.db.migration.avro.RenameKeyspace rks = (org.apache.cassandra.db.migration.avro.RenameKeyspace)mi.migration;
        oldName = rks.old_ksname.toString();
        newName = rks.new_ksname.toString();
    }
}
