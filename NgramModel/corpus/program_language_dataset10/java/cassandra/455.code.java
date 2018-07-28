package org.apache.cassandra.config;
import static org.junit.Assert.assertNotNull;
import org.apache.avro.specific.SpecificRecord;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.db.migration.AddKeyspace;
import org.apache.cassandra.locator.SimpleStrategy;
import org.apache.cassandra.io.SerDeUtils;
import org.junit.Test;
import java.io.IOException;
import java.util.UUID;
public class DatabaseDescriptorTest
{
    protected <D extends SpecificRecord> D serDe(D record, D newInstance) throws IOException
    {
        D actual = SerDeUtils.deserialize(record.getSchema(),
                                              SerDeUtils.serialize(record),
                                              newInstance);
        assert actual.equals(record) : actual + " != " + record;
        return actual;
    }
    @Test
    public void testCFMetaDataSerialization() throws IOException, ConfigurationException
    {
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            for (CFMetaData cfm : DatabaseDescriptor.getTableMetaData(table).values())
            {
                CFMetaData cfmDupe = CFMetaData.inflate(serDe(cfm.deflate(), new org.apache.cassandra.db.migration.avro.CfDef()));
                assert cfmDupe != null;
                assert cfmDupe.equals(cfm);
            }
        }
    }
    @Test
    public void testKSMetaDataSerialization() throws IOException, ConfigurationException
    {
        for (KSMetaData ksm : DatabaseDescriptor.tables.values())
        {
            KSMetaData ksmDupe = KSMetaData.inflate(serDe(ksm.deflate(), new org.apache.cassandra.db.migration.avro.KsDef()));
            assert ksmDupe != null;
            assert ksmDupe.equals(ksm);
        }
    }
    @Test
    public void testTransKsMigration() throws IOException, ConfigurationException
    {
        CleanupHelper.cleanupAndLeaveDirs();
        DatabaseDescriptor.loadSchemas();
        assert DatabaseDescriptor.getNonSystemTables().size() == 0;
        AddKeyspace ks0 = new AddKeyspace(new KSMetaData("ks0", SimpleStrategy.class, null, 3));
        ks0.apply();
        AddKeyspace ks1 = new AddKeyspace(new KSMetaData("ks1", SimpleStrategy.class, null, 3));
        ks1.apply();
        assert DatabaseDescriptor.getTableDefinition("ks0") != null;
        assert DatabaseDescriptor.getTableDefinition("ks1") != null;
        DatabaseDescriptor.clearTableDefinition(DatabaseDescriptor.getTableDefinition("ks0"), new UUID(4096, 0));
        DatabaseDescriptor.clearTableDefinition(DatabaseDescriptor.getTableDefinition("ks1"), new UUID(4096, 0));
        assert DatabaseDescriptor.getTableDefinition("ks0") == null;
        assert DatabaseDescriptor.getTableDefinition("ks1") == null;
        DatabaseDescriptor.loadSchemas();
        assert DatabaseDescriptor.getTableDefinition("ks0") != null;
        assert DatabaseDescriptor.getTableDefinition("ks1") != null;
    }
}
