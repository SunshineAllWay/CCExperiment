package org.apache.cassandra.config;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ColumnDefinitionTest
{
    @Test
    public void testSerializeDeserialize() throws Exception
    {
        ColumnDefinition cd0 = new ColumnDefinition(ByteBufferUtil.bytes("TestColumnDefinitionName0"),
                                                    "BytesType",
                                                    IndexType.KEYS,
                                                    "random index name 0");
        ColumnDefinition cd1 = new ColumnDefinition(ByteBufferUtil.bytes("TestColumnDefinition1"),
                                                    "LongType",
                                                    null,
                                                    null);
        testSerializeDeserialize(cd0);
        testSerializeDeserialize(cd1);
    }
    protected void testSerializeDeserialize(ColumnDefinition cd) throws Exception
    {
        ColumnDefinition newCd = ColumnDefinition.inflate(cd.deflate());
        assert cd != newCd;
        assert cd.hashCode() == newCd.hashCode();
        assert cd.equals(newCd);
    }
}
