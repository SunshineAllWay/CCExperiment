package org.apache.cassandra.hadoop.streaming;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hadoop.streaming.io.IdentifierResolver;
public class AvroResolver extends IdentifierResolver
{
    public static final String AVRO_ID = "cassandra_avro_output";
    @Override
    public void resolve(String identifier)
    {
        if (!identifier.equalsIgnoreCase(AVRO_ID))
        {
            super.resolve(identifier);
            return;
        }
        setInputWriterClass(null);
        setOutputReaderClass(AvroOutputReader.class);
        setOutputKeyClass(ByteBuffer.class);
        setOutputValueClass(List.class);
    }
}
