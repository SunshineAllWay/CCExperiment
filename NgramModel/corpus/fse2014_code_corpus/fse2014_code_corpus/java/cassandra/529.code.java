package org.apache.cassandra.streaming;
import static junit.framework.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.utils.Pair;
import java.util.Arrays;
import org.junit.Test;
public class BootstrapTest extends SchemaLoader
{
    @Test
    public void testGetNewNames() throws IOException
    {
        Descriptor desc = Descriptor.fromFilename(new File("Keyspace1", "Standard1-500-Data.db").toString());
        PendingFile inContext = new PendingFile(null, desc, "Data.db", Arrays.asList(new Pair<Long,Long>(0L, 1L)), OperationType.BOOTSTRAP);
        PendingFile outContext = StreamIn.getContextMapping(inContext);
        assert !inContext.getFilename().equals(outContext.getFilename());
        assertEquals(inContext.component, outContext.component);
        assertEquals(inContext.desc.ksname, outContext.desc.ksname);
        assertEquals(inContext.desc.cfname, outContext.desc.cfname);
    }
}
