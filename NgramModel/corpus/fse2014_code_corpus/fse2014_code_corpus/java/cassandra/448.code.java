package org.apache.cassandra;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.junit.BeforeClass;
public class SchemaLoader
{
    @BeforeClass
    public static void loadSchemaFromYaml()
    {
        try
        {
            for (KSMetaData ksm : DatabaseDescriptor.readTablesFromYaml())
            {
                for (CFMetaData cfm : ksm.cfMetaData().values())
                    CFMetaData.map(cfm);
                DatabaseDescriptor.setTableDefinition(ksm, DatabaseDescriptor.getDefsVersion());
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
